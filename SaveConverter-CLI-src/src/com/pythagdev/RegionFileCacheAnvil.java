package com.pythagdev;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegionFileCacheAnvil
{

    private RegionFileCacheAnvil()
    {
    }

    public static synchronized RegionFile func_22193_a(File file, int x, int y, int z,
    		boolean create)
    {
    	return getFileHelper(file, (new StringBuilder())
    			.append(x >> 5).append(".").append(y).append(".").append(z >> 5)
    			.append(RegionFile.ANVIL_EXTENSION).toString(), create);
    }

    public static synchronized RegionFile func_22193_a(File file, int i, int j)
    {
    	return getFileHelper(file, (new StringBuilder())
    			.append(i >> 5).append(".").append(j >> 5)
    				.append(RegionFile.ANVIL_EXTENSION).toString(), true);
    }
    
    private static synchronized RegionFile getFileHelper(File rootDir,
    		String file2PartName, boolean create)
    {
    	String file2Name = "r." + file2PartName;
    	
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
        
        if(cache.size() >= 256)//?
        {
            func_22192_a();
        }
        RegionFile newFile = new RegionFileLarge(regionFile);
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

    private static final Map<File, Reference<RegionFile> >  cache = new HashMap<File, Reference<RegionFile> > ();
}
