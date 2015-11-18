package com.pythagdev;

import java.util.Collection;

import com.mojang.nbt.*;

public abstract class ChunkLoaderBase implements IChunkLoader
{
    public static Chunk loadChunkIntoWorldFromCompound(CompoundTag compound,
    		boolean extraIDs)
    {
        int x = compound.getInt("xPos");
        int z = compound.getInt("zPos");
        	
        Chunk chunk = new Chunk(x, z);
        
        boolean regenBlocklightMap = false;
        boolean regenSkylightMap = false;
        
        boolean isUpdatedCC = false;

    	if(compound.contains("Cubes"))//import ChunkCubes from old save version
    	{
        	CompoundTag cubesTag = compound.getCompound("Cubes");

	        for(int n = 0; n <= 255; ++n)
	        {
	        	String cubeKey = "Ch"+n;
	        	if(cubesTag.contains(cubeKey))
	        	{
        			//used to be Chunk.cubesOffset, but locked non-constant because
	        		//that's what it was in the older version this is converting from
	        		int y = n-128;
	        		
		        	CompoundTag cubeTag = cubesTag.getCompound(cubeKey);
		        	
	        		ChunkCube cube = chunk.cubes[n] = new ChunkCube(chunk, y);
    				cube.blocks = new CubeBlockData12Bit(
    						cubeTag.getByteArray("Blocks"), 
    						cubeTag.getByteArray("Data"), 4);
    				
	        		cube.skylightMap = new NibbleArray(cubeTag.getByteArray("SkyLight"));
	        		cube.blocklightMap = new NibbleArray(cubeTag.getByteArray("BlockLight"));
	        		cube.isTerrainPopulated = cubeTag.getBoolean("TerrainPopulated");
	        		
	        		if(!cube.blocks.isValid())
	                {
	        			if(extraIDs)
	        			{
	        				cube.blocks = new CubeBlockData16Bit(4);
	        			}
	        			else
	        			{
	        				cube.blocks = new CubeBlockData12Bit(4);
	        			}
	        			cube.isTerrainPopulated = false;
	                }
	        		
			        if(!cube.blocklightMap.isValid())
			        {
			        	regenBlocklightMap = true;
			        	cube.blocklightMap = new NibbleArray(ICubeBlockData.NUM_CUBE_BLOCKS);
			        }
			        if(!cube.skylightMap.isValid())
			        {
			        	regenSkylightMap = true;
			        	cube.skylightMap = new NibbleArray(ICubeBlockData.NUM_CUBE_BLOCKS);
			        }
			        
			    	loadEntities(cubeTag, chunk);
			        loadTileEntities(cubeTag, chunk);
	        	}
	        }
    	}
    	else if(compound.contains("Sections"))//load Sections from Anvil
    	{
			ListTag<? extends Tag> sections = compound.getList("Sections");
    		
    		for(int n = 0; n < sections.size(); ++n)
    		{
    			Tag tag = sections.get(n);
    			if(!(tag instanceof CompoundTag))
    			{continue;}
    			CompoundTag section = (CompoundTag)tag;
    			
    			int y = section.getByte("Y") & 0xff;
                byte[] blocks = section.getByteArray("Blocks");
                byte[] metas = section.getByteArray("Data");
                byte[] skyLight = section.getByteArray("SkyLight");
                byte[] blockLight = section.getByteArray("BlockLight");
                byte[] addBlocks = null;
                
                if(section.contains("AddBlocks"))
                {
                    addBlocks = section.getByteArray("AddBlocks");
                }
                else if(section.contains("Add"))
                {
                    addBlocks = section.getByteArray("Add");
                }
    			
                CubeBlockDataAnvil blockData = new CubeBlockDataAnvil(blocks, addBlocks, metas, 4);
    			ChunkCube cube = new ChunkCube(chunk, blockData,
    					new DataLayer(skyLight, 4), new DataLayer(blockLight, 4), y);
    			chunk.cubes[y+Chunk.cubesOffset] = cube;
    		}
    	}
    	else if(compound.contains("Blocks"))//load Blocks from Vanilla/YMod
    	{
    		byte[] blocks = compound.getByteArray("Blocks");
    		if(blocks.length != 0)
    		{
    			int len = 3;
    			if(blocks.length == 1 << 15)
    			{/*len=3;*/}
    			else if(blocks.length == 1 << 13)
    			{len=1;}
    			else if(blocks.length == 1 << 14)
    			{len=2;}
    			else if(blocks.length == 1 << 16)
    			{len=4;}
    			else if(blocks.length == 1 << 17)
    			{len=5;}
    			else if(blocks.length == 1 << 18)
    			{len=6;}
    			else if(blocks.length == 1 << 19)//maybe unused, but better safe than sorry
    			{len=7;}

				byte[] md;
				if(compound.contains("Data"))
				{
					md = compound.getByteArray("Data");
				}
				else
				{
					md = new byte[1<<11];
				}
				
    			chunk = new Chunk(blocks, md, x, z, true, len, extraIDs);
    			
    			if(compound.contains("BlockLight"))
    			{
    				byte[] light = compound.getByteArray("BlockLight");
    				
    				chunk.setBlockLight(light, len);
    			}
    			if(compound.contains("SkyLight"))
    			{
    				byte[] light = compound.getByteArray("SkyLight");
    				
    				chunk.setSkylight(light, len);
    			}
    			
	        	regenBlocklightMap = true;
	        	regenSkylightMap = true;
	        }
    	}
        
        //assume true unless CC says false
        chunk.isTerrainFullyGenerated = true;

    	//either vanilla or old CC
    	if(!compound.contains("HeightMap"))
    	{
			chunk.heightMap = new int[256];
            //chunk.func_1024_c();
    	}
    	//Updated CC
		else if(compound.getIsType("HeightMap", ShortArrayTag.class))
		{
	        chunk.heightMap = Cast.toIntArray(compound.getShortArray("HeightMap"));
	        isUpdatedCC = true;
	        if(compound.contains("isTerrainFullyGenerated"))
	        {
	        	chunk.isTerrainFullyGenerated
	        		= compound.getBoolean("isTerrainFullyGenerated");
	        }
		}
    	else if(compound.getIsType("HeightMap", ByteArrayTag.class))
		{
			byte[] heightMap = compound.getByteArray("HeightMap");

			chunk.heightMap = new int[256];
			if(heightMap == null || heightMap.length == 0 || regenSkylightMap)
			{
	            //chunk.func_1024_c();
			}
			else
			{
				if(heightMap.length == 512)//old CC
				{
					for(int n = 0; n < chunk.heightMap.length; ++n)
					{
						chunk.heightMap[n] = (heightMap[n << 1] << 8);
						chunk.heightMap[n] |= heightMap[(n << 1) + 1];
					}
				}
				else//Vanilla
				{
					for(int n = 0; n < heightMap.length; ++n)
					{
						chunk.heightMap[n] = heightMap[n];
					}
				}
			}
		}
    	//YMod, Anvil, or CC 1.6
		else if(compound.getIsType("HeightMap", IntArrayTag.class))
		{
	        chunk.heightMap = compound.getIntArray("HeightMap");
		}
    	
    	if(regenBlocklightMap)
    	{
            //chunk.func_1014_a();
    	}
        
    	if(!isUpdatedCC)
    	{
	    	loadEntities(compound, chunk);
	        loadTileEntities(compound, chunk);
	        loadTileTicks(compound, chunk);
    	}

        return chunk;
    }

    public static void loadEntities(CompoundTag tag, Chunk chunk)
    {
        @SuppressWarnings("unchecked")
		ListTag<CompoundTag> entities = (ListTag<CompoundTag>) tag.getList("Entities");
        if(entities != null)
        {
            for(int k = 0; k < entities.size(); k++)
            {
                CompoundTag entity = (CompoundTag)entities.get(k);
                chunk.hasEntities = true;
                if(entity != null)
                {
                    chunk.addEntity(entity);
                }
            }
        }
    }

	public static void loadTileEntities(CompoundTag tag, Chunk chunk)
    {
        @SuppressWarnings("unchecked")
		ListTag<CompoundTag> tileEntities = (ListTag<CompoundTag>) tag.getList("TileEntities");
        if(tileEntities != null)
        {
            for(int l = 0; l < tileEntities.size(); l++)
            {
                CompoundTag tileentity = (CompoundTag)tileEntities.get(l);
                
                if(tileentity != null)
                {
                    chunk.addTileEntity(tileentity);
                }
            }
        }
    }
    
	protected static void checkForItemShifting(ListTag<? extends Tag> list)
    {
        for(int n = 0; n < list.size(); ++n)
        {
        	Tag item = list.get(n);
        	if(item instanceof CompoundTag)
        	{
        		checkForItemShifting((CompoundTag) item);
        	}
        }
    }
    
    protected static void checkForItemShifting(CompoundTag compound)
    {
    	Collection<Tag> values = compound.getAllTags();
    	
    	for(Tag nbt : values)
    	{
    		String key = nbt.getName();
    		
    		if(compound.getIsType(key, CompoundTag.class))
    		{
    			CompoundTag compound2 = compound.getCompound(key);
    			
    			if(compound2.getName().equals("Item"))
    			{
    				shiftItem(compound2);
    			}
    			else
    			{
    				checkForItemShifting(compound2);
    			}
    		}
    		else if(compound.getIsType(key, ListTag.class))
    		{
    			if(key.equals("Items") || key.equals("Inventory"))
    	    	{
        			ListTag<? extends Tag> tagList = compound.getList(key);
    	    		
    	    		for(int n = 0; n < tagList.size(); ++n)
    	    		{
    	    			Tag item = tagList.get(n);
    	    			
    	    			if(item instanceof CompoundTag)
    	    			{
    	    				shiftItem( (CompoundTag) item);
    	    			}
    	    		}
    	    	}
    			else
    			{
        			ListTag<? extends Tag> tagList = compound.getList(key);
    	    		
    	    		for(int n = 0; n < tagList.size(); ++n)
    	    		{
    	    			Tag compound2 = tagList.get(n);
    	    			
    	    			if(compound2 instanceof CompoundTag)
    	    			{
    	    				checkForItemShifting((CompoundTag) compound2);
    	    			}
    	    			else if(compound2 instanceof ListTag<?>)
    	    			{
    	    				checkForItemShifting((ListTag<?>) compound2);
    	    			}
    	    		}
    			}
    		}
    	}
	}
    
    protected static void shiftItem(CompoundTag item)
    {
    	short itemID = item.getShort("id");
        
        if(stackShift == 1)
        {
        	if(itemID >= smallBlockSize)
        	{
        		itemID += blockSizeOffset;
        	}
        }
        else if(stackShift == -1)
        {
        	if(itemID >= largeBlockSize)
        	{
        		itemID -= blockSizeOffset;
        	}
        }
        
        item.putShort("id", itemID);
    }

	public static void loadTileTicks(CompoundTag tag, Chunk chunk)
    {
        @SuppressWarnings("unchecked")
		ListTag<CompoundTag> tileTicks = (ListTag<CompoundTag>) tag.getList("TileTicks");

        for(int l = 0; l < tileTicks.size(); l++)
        {
            CompoundTag tileentity = (CompoundTag)tileTicks.get(l);
            
            if(tileentity != null)
            {
                chunk.addTileTick(tileentity);
            }
        }
    }
    
    public static byte stackShift = 0;
    
    public static final int largeBlockSize = 4096;
    public static final int smallBlockSize = 256;
    public static final int blockSizeOffset = 4096 - 256;
}
