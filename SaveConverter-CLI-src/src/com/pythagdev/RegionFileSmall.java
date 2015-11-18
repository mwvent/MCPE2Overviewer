package com.pythagdev;

import java.io.*;

public class RegionFileSmall extends RegionFile
{
    public RegionFileSmall(File path)
    {
		super(path);
	}

	private static final int SECTOR_BYTES = 256;

    private static final byte emptySector[] = new byte[SECTOR_BYTES];
    protected byte[] getEmptySector()
    {
    	return emptySector;
    }
    
    public int SectorBytes()
    {
    	return SECTOR_BYTES;
    }
}
