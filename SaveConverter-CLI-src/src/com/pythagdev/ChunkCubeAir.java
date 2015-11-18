package com.pythagdev;

public class ChunkCubeAir extends ChunkCube
{
	public ChunkCubeAir(Chunk ichunk, int y)
	{
		super(ichunk, y);
		
		if(Blocks12 == null)
		{
			Blocks12 = new CubeBlockData12Bit(4);
			//Data = new NibbleArray(Blocks.length);
		}
		
        blocks = Blocks12;
        //data = Data;
        skylightMap = Data;
        blocklightMap = Data;
	}
	
	public ChunkCubeAir(Chunk ichunk, int y, boolean extraIDs)
	{
		super(ichunk, y);
		
		if(extraIDs)
		{
			if(Blocks16 == null)
			{
				Blocks16 = new CubeBlockData16Bit(4);
			}
			
	        blocks = Blocks16;
		}
		else
		{
			if(Blocks12 == null)
			{
				Blocks12 = new CubeBlockData12Bit(4);
			}
			
	        blocks = Blocks12;
		}
		
        //data = Data;
        skylightMap = Data;
        blocklightMap = Data;
	}

	private static CubeBlockData12Bit Blocks12 = null;
	private static CubeBlockData16Bit Blocks16 = null;
	private static NibbleArray Data = null;
}
