package com.pythagdev;

public class CubeBlockDataAnvil implements ICubeBlockData
{
	public final int yShift;
	public final int zShift;
	
	public CubeBlockDataAnvil(int yBits)
	{
		blocks = new byte[BLOCKS_SIZE];
		addBl = null;
		meta = new byte[METAS_SIZE];
		
		zShift = yBits;//TODO: This is assumed to always be 4
		yShift = zShift+4;
	}

	public CubeBlockDataAnvil(byte[] Blocks, byte[] AddBl, byte[] Metas, int yBits)
	{
		blocks = Blocks;
		addBl = AddBl;
		meta = Metas;
		
		zShift = yBits;
		yShift = zShift+4;
	}
	
	
	public final int getID(int offset)
	{
		return (blocks[offset] & 0xff) | (getAddBl(offset) << 8);
	}
	public final int getID(int x, int y, int z)
	{
		return (blocks[y << yShift | z << zShift | x] & 0xff)
		 		| (getAddBl(x, y, z) << 8);
	}

	public final void setID(int offset, int id)
	{
		blocks[offset] = (byte) (id & 0xff);
		
		if(id >= 256)
		{setAddBl(offset, id >> 8);}
	}
	public final void setID(int x, int y, int z, int id)
	{
		blocks[y << yShift | z << zShift | x] = (byte) (id & 0xff);
		
		if(id >= 256)
		{setAddBl(x, y, z, id >> 8);}
	}
	
	
    public int getMeta(int x, int y, int z)
    {
        int l = y << yShift | z << zShift | x;
        int i1 = l >> 1;
        int j1 = l & 1;
        if(j1 == 0)
        {
            return meta[i1] & 0xf;
        } else
        {
            return meta[i1] >> 4 & 0xf;
        }
    }
    public int getMeta(int offset)
    {
        int i1 = offset >> 1;
        int j1 = offset & 1;
        if(j1 == 0)
        {
            return meta[i1] & 0xf;
        }
        else
        {
            return meta[i1] >> 4 & 0xf;
        }
    }

    public void setMeta(int x, int y, int z, int md)
    {
        int i1 = y << yShift | z << zShift | x;
        int j1 = i1 >> 1;
        int k1 = i1 & 1;
        if(k1 == 0)
        {
        	meta[j1] = (byte)(meta[j1] & 0xf0 | md & 0xf);
        }
        else
        {
        	meta[j1] = (byte)(meta[j1] & 0xf | (md & 0xf) << 4);
        }
    }
    public void setMeta(int offset, int md)
    {
        int j1 = offset >> 1;
        int k1 = offset & 1;
        if(k1 == 0)
        {
        	meta[j1] = (byte)(meta[j1] & 0xf0 | md & 0xf);
        }
        else
        {
        	meta[j1] = (byte)(meta[j1] & 0xf | (md & 0xf) << 4);
        }
    }
    
    private int getAddBl(int x, int y, int z)
    {
    	if(addBl == null)
    	{
    		return 0;
    	}
    	
        int l = y << yShift | z << zShift | x;
        int i1 = l >> 1;
        int j1 = l & 1;
        if(j1 == 0)
        {
            return addBl[i1] & 0xf;
        } else
        {
            return addBl[i1] >> 4 & 0xf;
        }
    }
    private int getAddBl(int offset)
    {
    	if(addBl == null)
    	{
    		return 0;
    	}
    	
        int i1 = offset >> 1;
        int j1 = offset & 1;
        if(j1 == 0)
        {
            return addBl[i1] & 0xf;
        }
        else
        {
            return addBl[i1] >> 4 & 0xf;
        }
    }

    private void setAddBl(int x, int y, int z, int md)
    {
    	if(addBl == null)
    	{
    		addBl = new byte[NUM_CUBE_BLOCKS >> 1];
    	}
    	
        int i1 = y << yShift | z << zShift | x;
        int j1 = i1 >> 1;
        int k1 = i1 & 1;
        if(k1 == 0)
        {
        	addBl[j1] = (byte)(addBl[j1] & 0xf0 | md & 0xf);
        }
        else
        {
        	addBl[j1] = (byte)(addBl[j1] & 0xf | (md & 0xf) << 4);
        }
    }
    private void setAddBl(int offset, int md)
    {
    	if(addBl == null)
    	{
    		addBl = new byte[NUM_CUBE_BLOCKS >> 1];
    	}
    	
        int j1 = offset >> 1;
        int k1 = offset & 1;
        if(k1 == 0)
        {
        	addBl[j1] = (byte)(addBl[j1] & 0xf0 | md & 0xf);
        }
        else
        {
        	addBl[j1] = (byte)(addBl[j1] & 0xf | (md & 0xf) << 4);
        }
    }

    
	public void storeInCompound(com.mojang.nbt.CompoundTag level)
	{
        level.putByteArray("Blocks", blocks);
        
        if(addBl != null)
        {level.putByteArray("Add", addBl);}
        
        level.putByteArray("Data", meta);
	}

	public boolean calculateIsAir()
	{
		for(byte b : blocks)
		{
			if(b != 0)
			{
				return false;
			}
		}
		if(addBl != null)
		{
			for(byte b : addBl)
			{
				if(b != 0)
				{
					return false;
				}
			}
		}
		return true;
	}

    
	public boolean isValid()
	{
		return blocks != null && blocks.length == BLOCKS_SIZE
			&& meta != null && meta.length == METAS_SIZE;
	}
	
	public byte blocks[];
	public byte addBl[];
	public byte meta[];
	
	public static final int BLOCKS_SIZE = NUM_CUBE_BLOCKS;
	public static final int METAS_SIZE = BLOCKS_SIZE>>1;
        	
	public static final int NUM_IDs = 1 << 12;

	public int getNumIDs()
	{return NUM_IDs;}
	
	public static final CubeBlockDataAnvil get(byte[] blocks, byte[] addBl,
			byte[] data, int yBits)
	{
		return new CubeBlockDataAnvil(blocks, addBl, data, yBits);
	}
	
	public static final CubeBlockDataAnvil get(short[] blocks2, int yBits)
	{
		CubeBlockDataAnvil retVal = new CubeBlockDataAnvil(yBits);
		CubeBlockData16Bit temp = new CubeBlockData16Bit(blocks2, yBits);
		
		for(int n = 0; n < NUM_CUBE_BLOCKS; ++n)
		{
			int id = temp.getID(n);

			retVal.setID(n, id);
			retVal.setMeta(n, temp.getMeta(n));
		}
		
		return retVal;
	}

	public static CubeBlockDataAnvil get(ICubeBlockData temp, int yBits)
	{
		CubeBlockDataAnvil retVal = new CubeBlockDataAnvil(yBits);
		
		for(int n = 0; n < NUM_CUBE_BLOCKS; ++n)
		{
			int id = temp.getID(n);

			retVal.setID(n, id);
			retVal.setMeta(n, temp.getMeta(n));
		}
		
		return retVal;
	}
}
