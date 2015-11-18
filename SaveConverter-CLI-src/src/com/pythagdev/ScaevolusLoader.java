package com.pythagdev;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.pythagdev.GUI.*;
import com.mojang.nbt.*;

/**ScaevolusLoader handles loading and saving Minecraft worlds in the original
 * Scaevolus save format (Minecraft Beta thru Minecraft 1.1.0). It includes
 * Vanilla-extended-height-mod save formats, but not Anvil.*/
public class ScaevolusLoader extends ChunkLoaderBase
{
	ScaevolusSettings settings = new ScaevolusSettings();
	
    public ScaevolusLoader(File file, String option)
    {
        worldDir = file;
        settings.setOptions(option);
    }

    public Chunk loadChunk(int i, int j)
        throws IOException
    {
        java.io.DataInputStream datainputstream = RegionFileCacheOld.getChunkInputStream(worldDir, i, j);
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
        	Main.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing level data, skipping").toString());
            return null;
        }
        CompoundTag level = nbttagcompound.getCompound("Level");
        if(!level.contains("Blocks"))
        {
        	Main.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing block data, skipping").toString());
            return null;
        }
        Chunk chunk = loadChunkIntoWorldFromCompound(level, settings.extraIDs);
        if(!chunk.isAtLocation(i, j))
        {
        	Main.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is in the wrong location; relocating. (Expected ").append(i).append(", ").append(j).append(", got ").append(chunk.xPosition).append(", ").append(chunk.zPosition).append(")").toString());
            level.putInt("xPos", i);
            level.putInt("zPos", j);
            chunk = loadChunkIntoWorldFromCompound(level, settings.extraIDs);
        }

        return chunk;
    }
    
    //added
    /*public void saveConvertedCube(NBTTagCompound outer, int x, int y, int z)
    {
        try
        {
    		DataOutputStream dataoutputstream = RegionFileCache.getCubeOutputStream(
    				worldDir, x, y, z);
    		
			CompressedStreamTools.func_1139_a(outer, dataoutputstream);
	        dataoutputstream.close();
		}
        catch (Exception e)
        {
			e.printStackTrace();
		}
    }*/

    public void saveChunk(Chunk chunk)
        throws IOException
    {
    	/*
        nbttagcompound.setInteger("xPos", chunk.xPosition);//
        nbttagcompound.setInteger("zPos", chunk.zPosition);//
        nbttagcompound.setLong("LastUpdate", world.getWorldTime());
        nbttagcompound.setByteArray("Blocks", chunk.blocks);
        nbttagcompound.setByteArray("Data", chunk.data.data);
        nbttagcompound.setByteArray("SkyLight", chunk.skylightMap.data);
        nbttagcompound.setByteArray("BlockLight", chunk.blocklightMap.data);
        nbttagcompound.setByteArray("HeightMap", chunk.heightMap);//
        nbttagcompound.setBoolean("TerrainPopulated", chunk.isTerrainPopulated);
        chunk.hasEntities = false;
	    */
    	
    	if(chunk == null)
    	{return;}
    	
        try
        {
        	CompoundTag nbttagcompound = new CompoundTag();
        	CompoundTag level = new CompoundTag();
            
            DataOutputStream dataoutputstream = RegionFileCacheOld.getChunkOutputStream(worldDir, chunk.xPosition, chunk.zPosition);
            nbttagcompound.putCompound("Level", level);

            //-----------------
            level.putInt("xPos", chunk.xPosition);
            level.putInt("zPos", chunk.zPosition);
            
            byte[] heightMap = new byte[chunk.heightMap.length];
            for(int n = 0; n < chunk.heightMap.length; ++n)
            {
        		heightMap[n] = (byte) (chunk.heightMap[n] & 0x00FF);
            }
            
            level.putByteArray("HeightMap", heightMap);

            chunk.hasEntities = false;
            
            //-----------------
            MergedChunk merged = chunk.createMergedChunk(lowestUsableCube(), highestUsableCube(),
            		3+settings.extraBits, settings.extraIDs);

            level.putLong("LastUpdate", merged.lastUpdate);
            
            merged.blocks.storeInCompound(level);
            level.putByteArray("SkyLight", merged.skylightMap);//TODO: someday, force light regen
            level.putByteArray("BlockLight", merged.blocklightMap);
            level.putBoolean("TerrainPopulated", merged.isTerrainPopulated);

            level.put("Entities", merged.entities);
            level.put("TileEntities", merged.chunkTileEntityMap);
            level.put("TileTicks", merged.tileTicks);
            
            chunk.hasEntities = false;
            
            //-----------------
            NbtIo.write(nbttagcompound, dataoutputstream);
            dataoutputstream.close();
            
            //TODO: fix, and add occasional resets, or remove entirely, since Minecraft
            // no longer supports tracking Save File Size...
            //WorldInfo worldinfo = world.getWorldInfo();
            //worldinfo.setSizeOnDisk(worldinfo.getSizeOnDisk() + (long)RegionFileCache.getSizeDelta(worldDir, chunk.xPosition, chunk.zPosition));
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
    		if(fileName.startsWith("r.") && fileName.endsWith(".mcr"))
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


    public void setFile(File file)
    {
    	worldDir = file;
    }

    private File worldDir;

	public void setOptions(String option)
	{
		settings.setOptions(option);
	}
}
