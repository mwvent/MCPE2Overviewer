package com.pythagdev;

import java.io.*;

public class RegionFileLarge extends RegionFile
{
    private static final int SECTOR_BYTES = 4096;
    
    
    public RegionFileLarge(File path)
    {
		super(path);
	}

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
