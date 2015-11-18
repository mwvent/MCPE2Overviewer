package com.pythagdev;

public class CubeBlockData16Bit implements ICubeBlockData
{
	public final int xShift;
	public final int zShift;
	
	public CubeBlockData16Bit(int yBits)
	{
		zShift = yBits;
		xShift = zShift+4;
		
		blocks = new short[BLOCKS_SIZE()];
	}

	public CubeBlockData16Bit(short[] Data, int yBits)
	{
		blocks = Data;
		
		zShift = yBits;
		xShift = zShift+4;
	}
	

	public final int getID(int offset)
	{
		return blocks[offset] & 0x0fff;
	}
	public final int getID(int x, int y, int z)
	{
		return getID(x << xShift | z << zShift | y);
	}

	public final void setID(int offset, int id)
	{
		blocks[offset] = (short) ((id & 0x0fff) | (blocks[offset] & 0xf000));
	}
	public final void setID(int x, int y, int z, int id)
	{
		setID(x << xShift | z << zShift | y, id);
	}
	

	public final int getMeta(int offset)
	{
		return (blocks[offset] & 0xf000) >> 12;
	}
	public final int getMeta(int x, int y, int z)
	{
		return getMeta(x << xShift | z << zShift | y);
	}

	public final void setMeta(int offset, int md)
	{
		blocks[offset] = (short) (((md<<12) & 0xf000) | (blocks[offset] & 0x0fff));
	}
	public final void setMeta(int x, int y, int z, int md)
	{
		setMeta(x << xShift | z << zShift | y, md);
	}


	public void storeInCompound(com.mojang.nbt.CompoundTag level)
	{
        level.putShortArray("Blocks", blocks);
	}

	public boolean calculateIsAir()
	{
		for(short b : blocks)//TODO: may change
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
		return blocks != null && blocks.length == BLOCKS_SIZE();
	}
	

	public final int BLOCKS_SIZE()
	{return 256 << zShift;} 
	public final int METAS_SIZE()
	{return 128 << zShift;}
	
	
	public short[] blocks;
	
	public static final int NUM_IDs = 1 << 12;

	public int getNumIDs()
	{return NUM_IDs;}
	
	public static final CubeBlockData16Bit get(byte[] blocks, byte[] data, int yBits)
	{
		CubeBlockData16Bit retVal = new CubeBlockData16Bit(yBits);
		CubeBlockData12Bit temp = new CubeBlockData12Bit(blocks, data, yBits);
		
		int blocksLength = 256 << yBits;
		for(int n = 0; n < blocksLength; ++n)
		{
			retVal.setID(n, temp.getID(n));
			retVal.setMeta(n, temp.getMeta(n));
		}
		
		return retVal;
	}
	
	public static final CubeBlockData16Bit get(short[] data, int yBits)
	{
		return new CubeBlockData16Bit(data, yBits);
	}

	public static CubeBlockData16Bit get(ICubeBlockData temp, int yBits)
	{
		CubeBlockData16Bit retVal = new CubeBlockData16Bit(yBits);

		int blocksLength = 256 << yBits;
		for(int n = 0; n < blocksLength; ++n)
		{
			retVal.setID(n, temp.getID(n));
			retVal.setMeta(n, temp.getMeta(n));
		}
		
		return retVal;
	}
}
