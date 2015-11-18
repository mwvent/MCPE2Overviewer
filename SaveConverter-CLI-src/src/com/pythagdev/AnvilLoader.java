package com.pythagdev;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.pythagdev.GUI.Main;
import com.mojang.nbt.*;

/**AnvilLoader handles loading and saving Minecraft saves in the Anvil save format
 * (Minecraft 1.2.0 thru <unknown>).*/
public class AnvilLoader extends ChunkLoaderBase
{
	private AnvilSettings settings = new AnvilSettings();
    private File worldDir;


    public void setFile(File file)
    {
    	worldDir = file;
    }
    
    public AnvilLoader(File file, String option)
    {
        worldDir = file;
        
        settings.setOptions(option);
    }

    public Chunk loadChunk(int i, int j)
        throws IOException
    {
        DataInputStream datainputstream = RegionFileCacheAnvil.getChunkInputStream(worldDir, i, j);
        CompoundTag nbttagcompound;
        if(datainputstream != null)
        {
            nbttagcompound = NbtIo.read(datainputstream);
        } else
        {
            return null;
        }
        if(!nbttagcompound.contains("Level"))
        {
        	Main.println((new StringBuilder()).append("Chunk file at ")
        			.append(i).append(",").append(j)
        			.append(" is missing level data, skipping").toString());
            return null;
        }
        CompoundTag level = nbttagcompound.getCompound("Level");
        if(!level.contains("Sections"))
        {
        	Main.println((new StringBuilder()).append("Chunk file at ")
        			.append(i).append(",").append(j)
        			.append(" is missing section data, skipping").toString());
            return null;
        }
        Chunk chunk = loadChunkIntoWorldFromCompound(level, true);
        if(!chunk.isAtLocation(i, j))
        {
        	Main.println((new StringBuilder()).append("Chunk file at ")
        			.append(i).append(",").append(j)
        			.append(" is in the wrong location; relocating. (Got ")
        			.append(chunk.xPosition).append(", ").append(chunk.zPosition)
        			.append(")").toString());
            level.putInt("xPos", i);
            level.putInt("zPos", j);
            chunk = loadChunkIntoWorldFromCompound(level, true);
        }

        return chunk;
    }

    public void saveChunk(Chunk chunk)
        throws IOException
    {
    	if(chunk == null)
    	{return;}
    	
        try
        {
        	CompoundTag compound = new CompoundTag();
        	CompoundTag level = new CompoundTag();
            
            DataOutputStream dataoutputstream = RegionFileCacheAnvil.getChunkOutputStream(
            		worldDir, chunk.xPosition, chunk.zPosition);
            compound.putCompound("Level", level);

            //-----------------
            level.putInt("xPos", chunk.xPosition);
            level.putInt("zPos", chunk.zPosition);
            level.putLong("LastUpdate", chunk.lastSaveTime);
            level.putIntArray("HeightMap", chunk.heightMap);
            
            boolean isTerrainPopulated = true;
            ListTag<CompoundTag> sectionTags = new ListTag<CompoundTag>("Sections");
            ListTag<CompoundTag> entityTags = new ListTag<CompoundTag>("Entities");
            ListTag<CompoundTag> tileEntityTags = new ListTag<CompoundTag>("TileEntities");
            ListTag<CompoundTag> tileTickTags = new ListTag<CompoundTag>("TileTicks");
            
            for (int yBase = 0; yBase < highestUsableCube()/*(128 / 16)*/; yBase++)
            {
            	ChunkCube cube = chunk.cubeAtYIndex(yBase);
            	if(cube == null || cube.blocks.calculateIsAir())
            	{
            		continue;
            	}

                // build section
                byte[] blocks = new byte[16 * 16 * 16];
                DataLayer dataValues = new DataLayer(blocks.length, 4);
                DataLayer skyLight = new DataLayer(blocks.length, 4);
                DataLayer blockLight = new DataLayer(blocks.length, 4);
                
                boolean hasHigherIDs = false;//cube.blocks.getNumIDs() > 256;
                DataLayer addBlocks = new DataLayer(blocks.length, 4);

                for (int x = 0; x < 16; x++)
                {
                    for (int y = 0; y < 16; y++)
                    {
                        for (int z = 0; z < 16; z++)
                        {
                            int block = cube.blocks.getID(x, y, z);
                        	
                            if(block >= 256)
                            {
                            	hasHigherIDs = true;
                            	addBlocks.set(x, y, z, block >> 8);
                            }
                            blocks[(y << 8) | (z << 4) | x] = (byte) (block & 0xff);

                            dataValues.set(x, y, z, cube.blocks.getMeta(x, y, z));
                            skyLight.set(x, y, z, cube.skylightMap.get(x, y, z));
                            blockLight.set(x, y, z, cube.blocklightMap.get(x, y, z));
                        }
                    }
                }

                CompoundTag sectionTag = new CompoundTag();

                sectionTag.putByte("Y", (byte) (yBase & 0xff));
                sectionTag.putByteArray("Blocks", blocks);
                sectionTag.putByteArray("Data", dataValues.data);
                sectionTag.putByteArray("SkyLight", skyLight.data);
                sectionTag.putByteArray("BlockLight", blockLight.data);
                
                if(hasHigherIDs)
                {
                	sectionTag.putByteArray("Add", addBlocks.data);
                }

                sectionTags.add(sectionTag);
                
                
                isTerrainPopulated &= chunk.cubeAtYIndex(yBase).isTerrainPopulated;
                
                for(int n = 0; n < cube.entities.size(); ++n)
                {entityTags.add(cube.entities.get(n));}
                
                for(int n = 0; n < cube.chunkTileEntityMap.size(); ++n)
                {tileEntityTags.add(cube.chunkTileEntityMap.get(n));}
                
                for(int n = 0; n < cube.tileTicks.size(); ++n)
                {tileTickTags.add(cube.tileTicks.get(n));}
            }
            
            level.put("Sections", sectionTags);
            level.putBoolean("TerrainPopulated", isTerrainPopulated);

            
            /*// create biome array
            if (biomeSource != null)
            {
            	//TODO long-term: try to add Biome Source back in. Perhaps include
        		// multiple MC versions of Biome generation?
                byte[] biomes = new byte[16 * 16];
                for (int x = 0; x < 16; x++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        biomes[(z << 4) | x] = (byte) (biomeSource.getBiomeId(
                    		(chunk.xPosition << 4) | x, (chunk.zPosition << 4) | z) & 0xff);
                    }
                }
                level.putByteArray("Biomes", biomes);
            }*/

            level.put("Entities", entityTags);
            level.put("TileEntities", tileEntityTags);
            level.put("TileTicks", tileTickTags);
            
            //-----------------
            NbtIo.write(compound, dataoutputstream);
            dataoutputstream.close();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void saveExtraChunkData(Chunk chunk)
        throws IOException
    {
    }

    public void func_814_a()
    {
    }

    public void saveExtraData()
    {
    }
    
    public List<ChunkCoordIntPair> existingChunksIn(File rootFile)
    {
    	LinkedList<ChunkCoordIntPair> retVal = new LinkedList<ChunkCoordIntPair>();
    	
    	File regionDir = new File(rootFile, "region");
    	
    	File files[] = regionDir.listFiles();
    	
    	if(files == null)
    	{
    		Main.println("No files in region directory!");
    		return retVal;
    	}
    	
    	for(File file : files)
    	{
    		String fileName = file.getName();
    		if(fileName.startsWith("r.") && fileName.endsWith(RegionFile.ANVIL_EXTENSION))
    		{
    			int firstDot = 1;
    			int secondDot = fileName.indexOf(".", firstDot+1);
    			int thirdDot = fileName.indexOf(".", secondDot+1);
    			int fourthDot = fileName.indexOf(".", thirdDot+1);
    			
    			if(fourthDot == -1)
    			{
    				try
    				{
    					String xStr = fileName.substring(firstDot+1, secondDot);
    					String zStr = fileName.substring(secondDot+1, thirdDot);
	    				int x = Integer.parseInt(xStr);
	    				int z = Integer.parseInt(zStr);
	    				
	    				for(int subX = 0; subX < 32; ++subX)
	    				{
	    					for(int subZ = 0; subZ < 32; ++subZ)
		    				{
    		        			retVal.add(new ChunkCoordIntPair((x << 5) + subX, (z << 5) + subZ));
		    				}
	    				}
    				}
    				catch(Exception e)
    				{}
    			}
    		}
    	}
    	
    	return retVal;
    }

    
    public int lowestUsableCube()
    {
    	return 0;
    }
    
    public int highestUsableCube()
    {
    	return 8 << settings.extraBits;
    }

    //do nothing
	public void optimizeForDest(IChunkLoader destLoader)
	{}
	public void optimizeForSrc(IChunkLoader srcLoader)
	{}
}
