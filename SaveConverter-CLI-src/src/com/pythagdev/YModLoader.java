package com.pythagdev;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.pythagdev.GUI.Main;
import com.mojang.nbt.*;

/*Obvious changes from old vanilla format:
 * Uses new NBTTagIntArray for HeightMap
 *  (very useful - consider borrowing for Cubic Chunks)
 * The various Byte and Nibble arrays are variable-sized
 * 
 * That seems to be all
 */

public class YModLoader extends ChunkLoaderBase
{
    public YModLoader(File file, String option)
    {
        saveDir = file;
        createIfNecessary = false;
    }

    private File chunkFileForXZ(int i, int j)
    {
        String s = (new StringBuilder()).append("c.").append(Integer.toString(i, 36)).append(".").append(Integer.toString(j, 36)).append(".dat").toString();
        String s1 = Integer.toString(i & 0x3f, 36);
        String s2 = Integer.toString(j & 0x3f, 36);
        File file = new File(saveDir, s1);
        if(!file.exists())
        {
            if(createIfNecessary)
            {
                file.mkdir();
            } else
            {
                return null;
            }
        }
        file = new File(file, s2);
        if(!file.exists())
        {
            if(createIfNecessary)
            {
                file.mkdir();
            } else
            {
                return null;
            }
        }
        file = new File(file, s);
        if(!file.exists() && !createIfNecessary)
        {
            return null;
        } else
        {
            return file;
        }
    }

    public Chunk loadChunk(int i, int j)
    {
    	createIfNecessary = false;
        File file = chunkFileForXZ(i, j);
        if(file != null && file.exists())
        {
            try
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                CompoundTag outer = NbtIo.readCompressed(new DataInputStream(fileinputstream));
                if(!outer.contains("Level"))
                {
                	Main.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing level data, skipping").toString());
                    return null;
                }
                CompoundTag level = outer.getCompound("Level");
                if(!level.contains("Blocks"))
                {
                	Main.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing block data, skipping").toString());
                    return null;
                }
                Chunk chunk = loadChunkIntoWorldFromCompound(level);
                if(!chunk.isAtLocation(i, j))
                {
                	Main.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is in the wrong location; relocating. (Expected ").append(i).append(", ").append(j).append(", got ").append(chunk.xPosition).append(", ").append(chunk.zPosition).append(")").toString());
                    level.putInt("xPos", i);
                    level.putInt("zPos", j);
                    chunk = loadChunkIntoWorldFromCompound(level);
                }
                return chunk;
            }
            catch(Exception exception)
            {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void saveChunk(Chunk chunk)
    {
    	if(chunk == null)
    	{return;}
    	
    	createIfNecessary = true;
    	
        File file = chunkFileForXZ(chunk.xPosition, chunk.zPosition);
        if(file.exists())
        {
            //world.sizeOnDisk -= file.length();
        }
        try
        {
            File file1 = new File(saveDir, "tmp_chunk.dat");
            FileOutputStream fileoutputstream = new FileOutputStream(file1);
            CompoundTag nbttagcompound = new CompoundTag();
            CompoundTag nbttagcompound1 = new CompoundTag();
            nbttagcompound.putCompound("Level", nbttagcompound1);
            storeChunkInCompound(chunk, nbttagcompound1);
            NbtIo.writeCompressed(nbttagcompound, new DataOutputStream(fileoutputstream));
            fileoutputstream.close();
            if(file.exists())
            {
                file.delete();
            }
            file1.renameTo(file);
            //world.sizeOnDisk += file.length();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void storeChunkInCompound(Chunk chunk, CompoundTag level)
    {
        level.putInt("xPos", chunk.xPosition);
        level.putInt("zPos", chunk.zPosition);
        
        MergedChunk merged = chunk.createMergedChunk(
        		lowestUsableCube(), highestUsableCube(),
        		3+settings.extraBits, false);
        
        level.putLong("LastUpdate", merged.lastUpdate);
        level.putByteArray("Blocks", ((CubeBlockData12Bit)merged.blocks).blocks);
        level.putByteArray("Data", ((CubeBlockData12Bit)merged.blocks).meta);
        level.putByteArray("SkyLight", merged.skylightMap);
        level.putByteArray("BlockLight", merged.blocklightMap);
        level.putIntArray("HeightMap", chunk.heightMap);
        level.putBoolean("TerrainPopulated", merged.isTerrainPopulated);

        level.put("Entities", merged.entities);
        level.put("TileEntities", merged.chunkTileEntityMap);
    }

    public static Chunk loadChunkIntoWorldFromCompound(CompoundTag level)
    {
        //int i = level.getInteger("xPos");
        //int j = level.getInteger("zPos");
        Chunk chunk = loadChunkIntoWorldFromCompound(level, false);
        /*chunk.blocks = new BlockIdArray(level.getByteArray("Blocks"));
        chunk.data = new NibbleArray(level.getByteArray("Data"));
        chunk.skylightMap = new NibbleArray(level.getByteArray("SkyLight"));
        chunk.blocklightMap = new NibbleArray(level.getByteArray("BlockLight"));*/
        //chunk.heightMap = level.getCastedIntArray("HeightMap");
        //chunk.isTerrainPopulated = level.getBoolean("TerrainPopulated");
        
        return chunk;
    }

    public void func_814_a()
    {
    }

    public void saveExtraData()
    {
    }

    public void saveExtraChunkData(Chunk chunk)
    {
    }

    
    public List<ChunkCoordIntPair> existingChunksIn(File rootFile)
    {
    	LinkedList<ChunkCoordIntPair> retVal = new LinkedList<ChunkCoordIntPair>();
    	
    	File folders[] = rootFile.listFiles();
    	
    	for(File file1 : folders)
    	{
    		if(!file1.isDirectory())
    		{
    			continue;
    		}
			if(file1.getName().equals("region"))
			{
				continue;
			}
    			
	    	File subFolders[] = file1.listFiles();
	    	
	    	for(File file2 : subFolders)
	    	{
	    		if(!file2.isDirectory())
	    		{
	    			continue;
	    		}
	    		
    	    	File files[] = file2.listFiles();
    	    	
    	    	for(File file : files)
    	    	{
    	    		if(!file.isFile())
    	    		{
    	    			continue;
    	    		}
    	    		
		    		String fileName = file.getName();
		    		
		    		if(!fileName.startsWith("c.") || !fileName.endsWith(".dat"))
		    		{
		    			continue;
		    		}
		    		
	    			int firstDot = 1;
	    			int secondDot = fileName.indexOf(".", firstDot+1);
	    			int thirdDot = fileName.indexOf(".", secondDot+1);

    				try
    				{
    					String xStr = fileName.substring(firstDot+1, secondDot);
    					String zStr = fileName.substring(secondDot+1, thirdDot);
	    				int x = Integer.parseInt(xStr, 36);
	    				int z = Integer.parseInt(zStr, 36);
	    				
	        			retVal.add(new ChunkCoordIntPair(x, z));
    				}
    				catch(Exception e)
    				{}
	    		}
    		}
    	}
    	
    	return retVal;
    }
    
    public int lowestUsableCube()
    {
    	return 0;
    }
    public int highestUsableCube()
    {
    	return 8 << settings.extraBits;
    }
    
    //No way to alter this, so no optimizing. On the other hand, doesn't need 
    // optimizing as badly as CC Loader
	public void optimizeForDest(IChunkLoader destLoader)
	{}
	public void optimizeForSrc(IChunkLoader srcLoader)
	{}
    
    public void setExtraBitsAdded(int num)
    {
    	settings.extraBits = num;
    }

    private File saveDir;
    private boolean createIfNecessary;
    
    //private int extraBitsAdded;
    private YModSettings settings = new YModSettings();

    public void setFile(File file)
    {
    	saveDir = file;
    }
}
