package com.pythagdev;

import com.mojang.nbt.*;

public class ChunkCube
{
    public ChunkCube(Chunk ichunk, int y)
    {
        //chunkTileEntityMap = new HashMap();
        //entities = new List[8];
        isTerrainPopulated = false;
        isModified = false;
        //hasEntities = false;
        lastSaveTime = 0L;
        chunk = ichunk;
        yPosition = y;
        //xPosition = i;
        //zPosition = j;
        //heightMap = new short[256];
        /*for(int k = 0; k < entities.length; k++)
        {
            entities[k] = new ArrayList();
        }*/

        //added
        /*blocks = new byte[1 << 12];
        data = new NibbleArray(blocks.length);
        skylightMap = new NibbleArray(blocks.length);
        blocklightMap = new NibbleArray(blocks.length);*/
    }

    public ChunkCube(Chunk ichunk, ICubeBlockData iblocks, int y)
    {
        this(ichunk, y);

    	//added
        /*isTerrainPopulated = false;
        isModified = false;
        hasEntities = false;
        lastSaveTime = 0L;
        chunk = ichunk;
        yPosition = y;*/
        
        blocks = iblocks;
        //data = new NibbleArray(iblocks.length);
        skylightMap = new NibbleArray(ICubeBlockData.NUM_CUBE_BLOCKS);
        blocklightMap = new NibbleArray(ICubeBlockData.NUM_CUBE_BLOCKS);
    }

    public ChunkCube(Chunk ichunk, ICubeBlockData blocks,
    		INibbleArray skyLight, INibbleArray blockLight, int y)
    {
        this(ichunk, y);

    	//added
        /*isTerrainPopulated = false;
        isModified = false;
        hasEntities = false;
        lastSaveTime = 0L;
        chunk = ichunk;
        yPosition = y;*/
        
        this.blocks = blocks;
        //data = new NibbleArray(iblocks.length);
        skylightMap = skyLight;
        blocklightMap = blockLight;
    }
    
    
	public boolean calculateIsAir()
	{
		if(entities.size() != 0)
		{
			isAir = false;
			return false;
		}
		if(chunkTileEntityMap.size() != 0)
		{
			isAir = false;
			return false;
		}
		
		if(!blocks.calculateIsAir())
		{
			isAir = false;
			return false;
		}

		isAir = true;
		return true;
	}
    
    public boolean isAtLocation(int x, int y, int z)
    {
    	//NOTE: yPosition?
        return x == chunk.xPosition && y == yPosition && z == chunk.zPosition;
    }

    public void setCubeModified()
    {
        isModified = true;
    }

    public void setChunkBlockTileEntity(int x, int y, int z, CompoundTag tileentity)
    {
    	chunkTileEntityMap.add(tileentity);
    }
    
    public static boolean isLit;
    public ICubeBlockData blocks;//consider making 1.5 bytes per block
    //public boolean isChunkLoaded;
    public Chunk chunk;
    //public NibbleArray data;
    public INibbleArray skylightMap;
    public INibbleArray blocklightMap;
    public final int yPosition;
    public boolean isTerrainPopulated;
    public boolean isModified;
    public boolean neverSave;
    //public boolean hasEntities;
    public long lastSaveTime;
    public ListTag<CompoundTag> chunkTileEntityMap = new ListTag<CompoundTag>();
    public ListTag<CompoundTag> entities = new ListTag<CompoundTag>();
    public ListTag<CompoundTag> tileTicks = new ListTag<CompoundTag>();
    /**set to true for all air chunks at generation, set false when block placed
     * don't bother to re-calculate*/
    public boolean isAir = false;
    
    public static final int XSIZE = 16;
    public static final int YSIZE = 16;
    public static final int ZSIZE = 16;
    public static final int XSHIFT = 8;
    public static final int ZSHIFT = 4;
    
}