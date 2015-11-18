package com.pythagdev;

public class CubeBlockData12Bit implements ICubeBlockData
{
	public final int xShift;
	public final int zShift;
	
	public CubeBlockData12Bit(int yBits)
	{
		zShift = yBits;
		xShift = zShift+4;
		
		blocks = new byte[BLOCKS_SIZE()];
		meta = new byte[METAS_SIZE()];
	}

	public CubeBlockData12Bit(byte[] Blocks, byte[] Metas, int yBits)
	{
		blocks = Blocks;
		meta = Metas;
		
		zShift = yBits;
		xShift = zShift+4;
	}
	
	
	public final int getID(int offset)
	{
		return blocks[offset] & 0xff;
	}
	public final int getID(int x, int y, int z)
	{
		return blocks[x << xShift | z << zShift | y] & 0xff;
	}

	public final void setID(int offset, int id)
	{
		blocks[offset] = (byte) (id & 0xff);
	}
	public final void setID(int x, int y, int z, int id)
	{
		blocks[x << xShift | z << zShift | y] = (byte) (id & 0xff);
	}
	
	
    public int getMeta(int x, int y, int z)
    {
        int l = x << xShift | z << zShift | y;
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
        int i1 = x << xShift | z << zShift | y;
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

    
	public void storeInCompound(com.mojang.nbt.CompoundTag level)
	{
        level.putByteArray("Blocks", blocks);
        level.putByteArray("Data", meta);
	}

	public boolean calculateIsAir()
	{
		for(byte b : blocks)//TODO: may change
		{
			if(b != 0)
			{
				return false;
			}
		}
		return true;
	}

    
	public boolean isValid()
	{
		return blocks != null && blocks.length == BLOCKS_SIZE()
			&& meta != null && meta.length == METAS_SIZE();
	}
	
	public byte blocks[];
	public byte meta[];
	
	//public static final int BLOCKS_SIZE = NUM_BLOCKS;
	//public static final int METAS_SIZE = BLOCKS_SIZE>>1;
	public final int BLOCKS_SIZE()
	{return 256 << zShift;} 
	public final int METAS_SIZE()
	{return 128 << zShift;}
        	
	public static final int NUM_IDs = 1 << 8;

	public int getNumIDs()
	{return NUM_IDs;}
	
	public static final CubeBlockData12Bit get(byte[] blocks, byte[] data, int yBits)
	{
		return new CubeBlockData12Bit(blocks, data, yBits);
	}
	
	public static final CubeBlockData12Bit get(short[] blocks2, int yBits)
	{
		CubeBlockData12Bit retVal = new CubeBlockData12Bit(yBits);
		CubeBlockData16Bit temp = new CubeBlockData16Bit(blocks2, yBits);

		int blocksLength = 256 << yBits;
		for(int n = 0; n < blocksLength; ++n)
		{
			int id = temp.getID(n);
			if(id < 256)
			{
				retVal.setID(n, id);
				retVal.setMeta(n, temp.getMeta(n));
			}
		}
		
		return retVal;
	}

	public static CubeBlockData12Bit get(ICubeBlockData temp, int yBits)
	{
		CubeBlockData12Bit retVal = new CubeBlockData12Bit(yBits);
		
		int blocksLength = 256 << yBits;
		for(int n = 0; n < blocksLength; ++n)
		{
			int id = temp.getID(n);
			if(id < 256)
			{
				retVal.setID(n, id);
				retVal.setMeta(n, temp.getMeta(n));
			}
		}
		
		return retVal;
	}
}
