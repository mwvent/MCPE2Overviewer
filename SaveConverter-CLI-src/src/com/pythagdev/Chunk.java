package com.pythagdev;

import com.pythagdev.GUI.Main;

import com.mojang.nbt.*;

public class Chunk
{
    public Chunk(int i, int j)
    {
        //chunkTileEntityMap = new HashMap();
        //entities = new List[8];
        //isTerrainPopulated = false;
        isModified = false;
        hasEntities = false;
        lastSaveTime = 0L;
        xPosition = i;
        zPosition = j;
        heightMap = new int[256];
        /*for(int k = 0; k < entities.length; k++)
        {
            entities[k] = new ArrayList();
        }*/

    }

    public Chunk(ChunkCube[] icubes, int i, int j)
    {
        this(i, j);
        cubes = icubes;
        //blocks = iblocks;
        //data = new NibbleArray(iblocks.length);
        //skylightMap = new NibbleArray(iblocks.length);
        //blocklightMap = new NibbleArray(iblocks.length);
    }

    public Chunk(byte[] data, byte[] meta, int i, int j, boolean extraIDs)
    {
    	this(data, meta, i, j, false, 3, extraIDs);
    }

    //for constructing from file with 8-bit BlockIDs
    public Chunk(byte[] data, byte[] md,
    		int i, int j,
    		boolean isTerrainPopulated, int extra16,
    		boolean extraIDs)
    {
        this(i, j);
        
        
        int numCubes = 1<<extra16;
        
        if(numCubes > cubes.length)
        {
        	numCubes = cubes.length;
        }
        
        byte subData[][] = new byte[numCubes][1 << 12];
        byte subMeta[][] = new byte[numCubes][1 << 11];
        
        int xShift = (8+extra16);
        int zShift = (4+extra16);
        int yMax = numCubes*16;
        
        for(int x = 0; x < 16; ++x)
        {
        	for(int z = 0; z < 16; ++z)
        	{
                for(int y = 0; y < yMax; ++y)
                {
                	//x << 8 | z << 4 | y
                	int srcOffset = x << xShift | z << zShift | y;
                	int destOffset = x << 8 | z << 4 | (y & 0xF);
                	
            		subData[(y >> 4)][destOffset] = data[srcOffset];
                }
                
                for(int y = 0; y < yMax; y += 2)//faster to copy by twos
                {
                	//x << 8 | z << 4 | y
                	int srcOffset = (x << xShift | z << zShift | y) >> 1;
                	int destOffset = (x << 8 | z << 4 | (y & 0xF)) >> 1;

                	subMeta[(y >> 4)][destOffset] = md[srcOffset];
                }
        	}
        }
        
        for(int n = 0; n < numCubes; ++n)
        {
        	cubes[n+Chunk.cubesOffset] = 
        		new ChunkCube(this,
        				extraIDs ? CubeBlockData16Bit.get(subData[n], subMeta[n], 4) :
        						   CubeBlockData12Bit.get(subData[n], subMeta[n], 4)
        				, n);
        	//cubes[n+Chunk.cubesOffset].isChunkLoaded = true;
        	cubes[n+Chunk.cubesOffset].isTerrainPopulated = isTerrainPopulated;
        }
    }

    //For constructing from file with 12-bit BlockIDs. Currently unused.
    public Chunk(short[] data,
    		int i, int j,
    		boolean isTerrainPopulated, int extra16,
    		boolean extraIDs)
    {
        this(i, j);
        
        int numCubes = 1<<extra16;
        
        if(numCubes > cubes.length)
        {
        	numCubes = cubes.length;
        }
        
        short subData[][] = new short[numCubes][1 << 12];
        
        int xShift = (8+extra16);
        int zShift = (4+extra16);
        int yMax = numCubes*16;
        
        for(int x = 0; x < 16; ++x)
        {
        	for(int z = 0; z < 16; ++z)
        	{
                for(int y = 0; y < yMax; ++y)
                {
                	//x << 8 | z << 4 | y
                	int srcOffset = x << xShift | z << zShift | y;
                	int destOffset = x << 8 | z << 4 | (y & 0xF);
                	
            		subData[(y >> 4)][destOffset] = data[srcOffset];
                }
        	}
        }
        
        for(int n = 0; n < numCubes; ++n)
        {
        	cubes[n+Chunk.cubesOffset] = 
        		new ChunkCube(this,
        				extraIDs ? CubeBlockData16Bit.get(subData[n], 4) :
        						   CubeBlockData12Bit.get(subData[n], 4)
        				, n);
        	//cubes[n+Chunk.cubesOffset].isChunkLoaded = true;
        	cubes[n+Chunk.cubesOffset].isTerrainPopulated = isTerrainPopulated;
        }
    }
    
    /**This should be called by ChunkLoader directly after the 5-arg ctor*/
    public void setSkylight(byte[] md, int extra16)
    {
        int xShift = (8+extra16);
        int zShift = (4+extra16);
        int yMax = 16<<extra16;
        
        for(int x = 0; x < 16; ++x)
        {
        	for(int z = 0; z < 16; ++z)
        	{
                for(int y = 0; y < yMax; ++y)//faster to copy by twos
                {
                	//x << 8 | z << 4 | y
                	int srcOffset = (x << xShift | z << zShift | y) >> 1;
                	//int destOffset = (x << 8 | z << 4 | (y & 0xF)) >> 1;

                	cubes[(y>>4)+cubesOffset].skylightMap.set(x, y, z, md[srcOffset]);
                }
        	}
        }
    }
    /**This should be called by ChunkLoader directly after the 5-arg ctor*/
    public void setBlockLight(byte[] md, int extra16)
    {
        int xShift = (8+extra16);
        int zShift = (4+extra16);
        int yMax = 16<<extra16;
        
        for(int x = 0; x < 16; ++x)
        {
        	for(int z = 0; z < 16; ++z)
        	{
                for(int y = 0; y < yMax; y += 2)//faster to copy by twos
                {
                	//x << 8 | z << 4 | y
                	int srcOffset = (x << xShift | z << zShift | y) >> 1;
                	//int destOffset = (x << 8 | z << 4 | (y & 0xF)) >> 1;

                	cubes[(y>>4)+cubesOffset].blocklightMap.set(x, y, z, md[srcOffset]);
                }
        	}
        }
    }
    
    public MergedChunk createMergedChunk(int startY, int endY, int finalPow, boolean extraIDs)
    {
    	startY += cubesOffset;
    	endY += cubesOffset;
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
    	int numChunks = endY - startY;
    	int xShift = 8 + finalPow;
    	int zShift = 4 + finalPow;
    	MergedChunk retVal = new MergedChunk();
		retVal.blocks = extraIDs ?
				new CubeBlockData16Bit(zShift) :
				new CubeBlockData12Bit(zShift);//new byte[16 * 16 * 16 * numChunks];
		//retVal.data = new byte[16 * 16 * 8 * numChunks];
		retVal.skylightMap = new byte[16 * 16 * 8 * numChunks];
		retVal.blocklightMap = new byte[16 * 16 * 8 * numChunks];
		retVal.isTerrainPopulated = true;

		retVal.chunkTileEntityMap = new ListTag<CompoundTag>();
		retVal.entities = new ListTag<CompoundTag>();
		retVal.tileTicks = new ListTag<CompoundTag>();
    	
		for(int n = startY; n < endY; ++n)
		{
			ChunkCube cube = cubes[n];
			
			if(cube != null && /*cube.isChunkLoaded &&*/ !cube.isAir)
			{
				for(int x = 0; x < 16; ++x)
				{
					for(int z = 0; z < 16; ++z)
					{
						for(int y = 0; y < 16; ++y)
						{
					        int offset = x << xShift | z << zShift
					        		| ((n - startY) << 4) | y;
					        int cubeOffset = x << 8 | z << 4 | y;

							retVal.blocks.setID(offset, cube.blocks.getID(cubeOffset));
							retVal.blocks.setMeta(offset, cube.blocks.getMeta(cubeOffset));
					        
							setNibble(retVal.skylightMap, offset, cube.skylightMap.get(x, y, z));
							setNibble(retVal.blocklightMap, offset, cube.blocklightMap.get(x, y, z));
						}
					}
				}

		        if(n == startY || 
		        		retVal.lastUpdate > cube.lastSaveTime)
		        {
		        	retVal.lastUpdate = cube.lastSaveTime;
		        }
		        
		        //NOTE: may create double-populate on some chunks
		        if(!cubes[n].isTerrainPopulated)
		        {
		        	retVal.isTerrainPopulated = false;
		        }
		        
		        for(int n2 = 0; n2 < cube.entities.size(); ++n2)
		        {
			        retVal.entities.add(cube.entities.get(n2));
		        }

		        for(int n2 = 0; n2 < cube.chunkTileEntityMap.size(); ++n2)
		        {
			        retVal.chunkTileEntityMap.add(
			        		cube.chunkTileEntityMap.get(n2));
		        }
		        
		        for(int n2 = 0; n2 < cube.tileTicks.size(); ++n2)
		        {
			        retVal.tileTicks.add(cube.tileTicks.get(n2));
		        }
			}
		}
		
		//TODO: set 0 layer to bedrock, or at least add an option to do so
		
    	return retVal;
    }
    
    public void setNibble(byte[] array, int offset, int val)
    {
		//the following is taken from NibbleArray
        int oHalf = offset >> 1;
        int wh = offset & 1;
        if(wh == 0)
        {
        	array[oHalf] = (byte)
        		(array[oHalf] & 0xf0 | val & 0xf);
        }
        else
        {
        	array[oHalf] = (byte)
        		(array[oHalf] & 0xf | (val & 0xf) << 4);
        }
    }

    public boolean isAtLocation(int i, int j)
    {
        return i == xPosition && j == zPosition;
    }

    public int getHeightValue(int i, int j)
    {
        return heightMap[j << 4 | i];// & 0xff;
    }

    public void func_1014_a()
    {
    }
    
    public void addEntity(CompoundTag entity)
    {
        hasEntities = true;
        @SuppressWarnings("unchecked")
		ListTag<DoubleTag> list = (ListTag<DoubleTag>) entity.getList("Pos");
        int x = MathHelper.floor_double(((DoubleTag)list.get(0)).data / 16D);
        int z = MathHelper.floor_double(((DoubleTag)list.get(2)).data / 16D);
        if(x != xPosition || z != zPosition)
        {
        	Main.println((new StringBuilder()).append("Entity at wrong location! ").append(entity).toString());
            //Thread.dumpStack();
            return;
        }
        
        int yChunk = MathHelper.floor_double(((DoubleTag)list.get(1)).data / 16D);

        if(cubes[yChunk+cubesOffset] != null)
        {
        	cubes[yChunk+cubesOffset].entities.add(entity);
        }
    }

    public void addTileEntity(CompoundTag tileentity)
    {
        int i = tileentity.getInt("x") - xPosition * 16;
        int j = tileentity.getInt("y");
        int k = tileentity.getInt("z") - zPosition * 16;
        setChunkBlockTileEntity(i, j, k, tileentity);
    }

    public void setChunkBlockTileEntity(int i, int j, int k, CompoundTag tileentity)
    {
        ChunkCube cube = cubes[(j >> 4)+cubesOffset];
        if(cube == null)
        {
        	Main.println("Attempted to place a tile entity in an unloaded ChunkCube!");
        	return;
    	}

        cube.setChunkBlockTileEntity(i, j & 0xf, k, tileentity);
    }
    
	public void addTileTick(CompoundTag tileentity)
	{
        int y = tileentity.getInt("y");

        ChunkCube cube = cubes[(y >> 4) + cubesOffset];
        if(cube == null)
        {
        	return;
    	}

        cube.tileTicks.add(tileentity);
	}

    public void setChunkModified()
    {
        isModified = true;
    }
    
    public ChunkCube cubeAtYIndex(int y)//TODO: add check for array overrun
    {return cubes[y+cubesOffset];}

    public void setCubeAtYIndex(int y, ChunkCube cube)
    {cubes[y+cubesOffset] = cube;}

    public static boolean isLit;
    //public byte blocks[];
    public ChunkCube[] cubes = new ChunkCube[cubesLength];
    
    //TODO: CubicChunks supports saving the precipitation height map (though
    //it does not require it to be saved). Someday, add SaveConverter support. 
    //public short precipitationHeightMap[] = null;
    
    public boolean isChunkLoaded;
    //public NibbleArray data;
    //public NibbleArray skylightMap;
    //public NibbleArray blocklightMap;
    public int heightMap[];
    public int lowestBlockHeight;
    public final int xPosition;
    public final int zPosition;
    //public Map chunkTileEntityMap;
    //public List entities[];
    //public boolean isTerrainPopulated;
    public boolean isModified;
    public boolean neverSave;
    public boolean hasEntities;
    public long lastSaveTime;
	public boolean isTerrainFullyGenerated = true;
    
    
    
    public boolean cubeExists(int y)
    {
    	int cube = (y >> 4) + cubesOffset;
    	return cubes[cube] != null;
    }
    
    public static final int cubesLength = 4094;//(2<<16)-2
    public static final int cubesOffset = cubesLength>>1;
    public static final short cubesYOffset = (short) (cubesOffset * ChunkCube.YSIZE);
    
    public static final int maxPossibleY = (cubesLength-cubesOffset) * ChunkCube.YSIZE;
	public static final int minPossibleY = -cubesOffset * ChunkCube.YSIZE;
	
	public static final int MAX_TERRAIN_CUBE = 256 - 96;
}
