package com.pythagdev;
//Taken from Minecraft

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;

// Referenced classes of package net.minecraft.src:
//            RegionFile

public abstract class RegionFileCache
{

	private RegionFileCache()
    {
    }

    public static synchronized RegionFile func_22193_a(File file, int x, int y, int z,
    		boolean create)
    {
    	return getFileHelper(file, (new StringBuilder())
    			.append(x >> 5).append(".").append(y)
    				.append(".").append(z >> 5).append(".mcr").toString(), create);
    }

    public static synchronized RegionFile func_22193_a(File file, int i, int j)
    {
    	return getFileHelper(file, (new StringBuilder())
    			.append(i >> 5).append(".").append(j >> 5)
    				.append(".mcr").toString(), true);
    }
    
    private static synchronized RegionFile getFileHelper(File rootDir,
    		String file2PartName, boolean create)
    {
    	String file2Name = "r2." + file2PartName;
    	
        File regionDir = new File(rootDir, "region");
        File regionFile = new File(regionDir, file2Name);
    	
        Reference<RegionFile> reference = cache.get(regionFile);
        if(reference != null)
        {
            RegionFile regionfile = reference.get();
            if(regionfile != null)
            {
                return regionfile;
            }
        }
        
        if(!regionDir.exists())
        {
            if(create)
        	{
            	regionDir.mkdirs();
        	}
            else
            {
            	return null;
            }
        }
        
        if(!create && !regionFile.exists())
        {
        	return null;
        }
        
        if(cache.size() >= 512)//?
        {
            func_22192_a();
        }
        RegionFile newFile = new RegionFileSmall(regionFile);
        cache.put(regionFile, new SoftReference<RegionFile>(newFile));
    	
        return newFile;
    }


    public static synchronized void func_22192_a()
    {
        Iterator<Reference<RegionFile> > iterator = cache.values().iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            Reference<RegionFile> reference = iterator.next();
            try
            {
                RegionFile regionfile = reference.get();
                if(regionfile != null)
                {
                    regionfile.close();
                }
            }
            catch(IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        } while(true);
        cache.clear();
    }

    public static int getSizeDelta(File file, int i, int j)
    {
        RegionFile regionfile = func_22193_a(file, i, j);
        return regionfile.getSizeDelta();
    }

    public static DataInputStream getChunkInputStream(File file, int i, int j)
    {
        RegionFile regionfile = func_22193_a(file, i, j);
        return regionfile.getChunkDataInputStream(i & 0x1f, j & 0x1f);
    }

    public static DataOutputStream getChunkOutputStream(File file, int i, int j)
    {
        RegionFile regionfile = func_22193_a(file, i, j);
        return regionfile.getChunkDataOutputStream(i & 0x1f, j & 0x1f);
    }

    public static DataInputStream getCubeInputStream(File file, int x, int y, int z)
    {
        RegionFile regionfile = func_22193_a(file, x, y, z, false);
        if(regionfile == null)
        {return null;}
        //TODO: later, consider making this include y val 
        return regionfile.getChunkDataInputStream(x & 0x1f, z & 0x1f);
    }

    public static DataOutputStream getCubeOutputStream(File file, int x, int y, int z)
    {
        RegionFile regionfile = func_22193_a(file, x, y, z, true);
        return regionfile.getChunkDataOutputStream(x & 0x1f, z & 0x1f);
    }

    private static final Map<File, Reference<RegionFile> >  cache = new HashMap<File, Reference<RegionFile> > ();

	public static void setCubeAir(File file, int x, int y, int z)
	{
        RegionFile regionfile = func_22193_a(file, x, y, z, true);//true?
		
		if(regionfile != null)
		{
			regionfile.setCubeAirOr0(x & 0x1f, z & 0x1f, -1);
		}
	}

	public static boolean isCubeAir(File file, int x, int y, int z)
	{
        RegionFile regionfile = func_22193_a(file, x, y, z, false);
		if(regionfile == null)
		{
			return false;
		}
		return regionfile.chunkIsAir(x & 0x1f, z & 0x1f);
	}

	public static void setCube0(File file, int x, int y, int z)
	{
        RegionFile regionfile = func_22193_a(file, x, y, z, false);
		
		if(regionfile != null)
		{
			regionfile.setCubeAirOr0(x & 0x1f, z & 0x1f, 0);
		}
	}
}