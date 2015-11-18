package com.pythagdev;

import java.io.*;
import java.util.*;

import com.pythagdev.GUI.Main;
import com.mojang.nbt.*;

public class CubicLoader extends ChunkLoaderBase
{
    public CubicLoader(File file, String option)
    {
        saveDir = file;
		settings.setOptions(option);
    }

    public void saveChunk(Chunk chunk)
        throws IOException
    {
    	if(chunk == null)
    	{return;}
    	
        try
        {
            CompoundTag tag = new CompoundTag();
            CompoundTag level = new CompoundTag();
            
            DataOutputStream dataoutputstream = settings.getChunkOutputStream(
            		saveDir, chunk.xPosition, chunk.zPosition);
            tag.putCompound("Level", level);
            
            storeChunkInCompound(chunk, level);
            
            NbtIo.write(tag, dataoutputstream);
            dataoutputstream.close();
            
            level = new CompoundTag();
            
            //boolean addAirCubes = terrainColumnsAreFullyGenerated && 
            //					chunk.cubes[Chunk.cubesOffset] != null;
            
            for(int n = 0; n < chunk.cubes.length; ++n)
            {
            	if(chunk.cubes[n] != null)
            	{
            		boolean isAir = chunk.cubes[n].calculateIsAir();
            		
            		if(isAir)
            		{
            			settings.setCubeAirOr0(saveDir, chunk.xPosition, n - Chunk.cubesOffset, chunk.zPosition);
            		}
            		else
            		{
	            		dataoutputstream = settings.getCubeOutputStream(saveDir, chunk.xPosition, n - Chunk.cubesOffset, chunk.zPosition);
	            		
	                    tag.putCompound("Level", level);
	                    storeCubeInCompound(chunk.cubes[n], level);
	                    NbtIo.write(tag, dataoutputstream);
	                    dataoutputstream.close();
            		}
            	}
            	/*else if(addAirCubes &&
        			n > Chunk.cubesOffset && n < Chunk.cubesOffset + Chunk.MAX_TERRAIN_CUBE &&
        			chunk.cubes[n] == null)
            	{
            		RegionFileCache.setCubeAir(saveDir, chunk.xPosition, n - Chunk.cubesOffset, chunk.zPosition);
            	}*/
            }
            
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

    public Chunk loadChunk(int x, int z)
        throws IOException
    {
        java.io.DataInputStream datainputstream = settings.getChunkInputStream(saveDir, x, z);
        		
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
        	Main.println((new StringBuilder()).append("Chunk file at ").append(x).append(",").append(z).append(" is missing level data, skipping").toString());
            return null;
        }
        CompoundTag chunkLevel = nbttagcompound.getCompound("Level");

        Chunk chunk = loadChunkIntoWorldFromCompound(chunkLevel, settings.ExtraIDs());
        if(!chunk.isAtLocation(x, z))
        {
        	Main.println((new StringBuilder()).append("Chunk file at ").append(x).append(",").append(z).append(" is in the wrong location; relocating. (Expected ").append(x).append(", ").append(z).append(", got ").append(chunk.xPosition).append(", ").append(chunk.zPosition).append(")").toString());
            chunkLevel.putInt("xPos", x);
            chunkLevel.putInt("zPos", z);
            chunk = loadChunkIntoWorldFromCompound(chunkLevel, settings.ExtraIDs());
        }
        
        //load cubes
        for(int y = lowestCube; y < highestCube; ++y)
        {
        	if(settings.TracksAir())
        	{
        		if(RegionFileCache.isCubeAir(saveDir, x, y, z))
    			{
        			chunk.setCubeAtYIndex(y, new ChunkCubeAir(chunk, y, settings.ExtraIDs()));
        			continue;
    			}
        	}
        	
	        java.io.DataInputStream cubeStream = settings.getCubeInputStream(saveDir, x, y, z);
	        
	        CompoundTag cubeCompound;
	        if(cubeStream != null)
	        {
	            cubeCompound = NbtIo.read(cubeStream);
	        } else
	        {
	            continue;
	        }
	        if(!cubeCompound.contains("Level"))
	        {
	        	Main.println((new StringBuilder()).append("Cube file at ")
	            		.append(x).append(",").append(y).append(",").append(z)
	            		.append(" is missing level data, skipping").toString());
	            continue;
	        }
	        CompoundTag level = cubeCompound.getCompound("Level");
	        if(!level.contains("Blocks"))
	        {
	        	Main.println((new StringBuilder()).append("Cube file at ")
	            		.append(x).append(",").append(y).append(",").append(z)
	            		.append(" is missing block data, skipping").toString());
	            continue;
	        }
	
	        loadCubeIntoWorldFromCompound(level, chunk, x, y, z);
        }
        
        return chunk;
    }

    public void storeCubeInCompound(ChunkCube cube, CompoundTag level)
    {
        level.putInt("yPos", cube.yPosition);

        settings.handleExtraIDsAndStackShift(cube);
		
        cube.blocks.storeInCompound(level);
        level.putByteArray("SkyLight", cube.skylightMap.toOld().getData());
        level.putByteArray("BlockLight", cube.blocklightMap.toOld().getData());
        level.putBoolean("TerrainPopulated", cube.isTerrainPopulated);

        ListTag<CompoundTag> entities = cube.entities;
        checkForItemShifting(entities);
        level.put("Entities", entities);
		
		ListTag<CompoundTag> tileEntities = cube.chunkTileEntityMap;
        checkForItemShifting(tileEntities);
		level.put("TileEntities", tileEntities);
		
		level.put("TileTicks", cube.tileTicks);
    }

    public void storeChunkInCompound(Chunk chunk, CompoundTag compound)
    {
        //world.checkSessionLock();
        compound.putInt("xPos", chunk.xPosition);
        compound.putInt("zPos", chunk.zPosition);
        
        settings.putHeightMapToCompound(compound, chunk.heightMap);
        
        chunk.hasEntities = false;
        
        compound.putBoolean("isTerrainFullyGenerated", chunk.isTerrainFullyGenerated);
    }

    public ChunkCube loadCubeIntoWorldFromCompound(
    		CompoundTag cubeTag, Chunk chunk, int x, int y, int z)
    {
    	int yStored = cubeTag.getInt("yPos");
    	if(yStored != y)
        {
    		Main.println((new StringBuilder()).append("Cube file at ")
            		.append(x).append(",").append(y).append(",").append(z)
            		.append(" is in the wrong location; relocating. (Expected yPosition: ")
            		.append(y)
            		.append(", got ").append(yStored).append(")").toString());
        }
        
		ChunkCube cube = chunk.cubes[y+Chunk.cubesOffset] = new ChunkCube(chunk, y);
		
		if(!cubeTag.contains("Blocks"))//error
		{
			cube.blocks = settings.getCubeBlockData();
			cube.isTerrainPopulated = false;
		}
		else if(cubeTag.getIsType("Blocks", ByteArrayTag.class))//8-bits per blockID
		{
			cube.blocks = new CubeBlockData12Bit(
					cubeTag.getByteArray("Blocks"), 
					cubeTag.getByteArray("Data"), 4);
		}
		else if(cubeTag.getIsType("Blocks", ShortArrayTag.class))//12-bits per blockID
		{
			cube.blocks = new CubeBlockData16Bit(
					cubeTag.getShortArray("Blocks"), 4);
		}
		
		cube.skylightMap = new NibbleArray(cubeTag.getByteArray("SkyLight"));
		cube.blocklightMap = new NibbleArray(cubeTag.getByteArray("BlockLight"));
		cube.isTerrainPopulated = cubeTag.getBoolean("TerrainPopulated");

        if(!cube.blocks.isValid())
        {
			cube.blocks = settings.getCubeBlockData();
			cube.isTerrainPopulated = false;
        }
        /*if(!cube.blocklightMap.isValid())
        {
        	cube.blocklightMap = new NibbleArray(cube.blocks.length);
            chunk.func_1014_a();
        }
        if(!cube.skylightMap.isValid())
        {
        	cube.skylightMap = new NibbleArray(cube.blocks.length);
            chunk.func_1024_c();
        }*/
        
    	loadEntities(cubeTag, chunk);
        loadTileEntities(cubeTag, chunk);
        
        return cube;
    }


    public void func_814_a()
    {
    }

    public void saveExtraData()
    {
    }

    public void saveExtraChunkData(Chunk chunk)
        throws IOException
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
    	
    	String start = settings.regionFileStartString();
    	
    	for(File file : files)
    	{
    		String fileName = file.getName();
    		if(fileName.startsWith(start) && fileName.endsWith(".mcr"))
    		{
    			int firstDot = fileName.indexOf(".");
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

    private File saveDir;
    //private boolean createIfNecessary;
    //public static VanillaLoader callbackMCRegion;
    

    public int lowestUsableCube()
    {
    	return -Chunk.cubesOffset;
    }
    
    public int highestUsableCube()
    {
    	return Chunk.cubesOffset;
    }
    

	public void optimizeForDest(IChunkLoader destLoader)
	{
    	highestCube = destLoader.highestUsableCube();
    	lowestCube = destLoader.lowestUsableCube();
	}
	
	public void optimizeForSrc(IChunkLoader srcLoader)
	{
	}

    public void setFile(File file)
    {
    	saveDir = file;
    }
    
    
    public int lowestCube = -Chunk.cubesOffset;
    public int highestCube = Chunk.cubesOffset;

    CubicSettings settings = new CubicSettings();
    //public boolean terrainColumnsAreFullyGenerated = true;
}
