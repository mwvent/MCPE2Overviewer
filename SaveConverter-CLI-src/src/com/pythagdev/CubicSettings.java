package com.pythagdev;

import java.io.*;

import com.mojang.nbt.CompoundTag;
import com.pythagdev.GUI.Names;

public class CubicSettings implements IFormatOption
{
	//actually possible cases: useOld; none; air; air|IDs; air|IDs|intH;
	protected int state = noExtraIDs;
	public static final int oldFileFormat = 0;
	public static final int oldAirTracking = 1;
	public static final int noExtraIDs = 2;
	public static final int noExtraHeight = 3;
	public static final int CCforMC120 = 4;
    /*public boolean useOldFormat = false;
    public boolean tracksAir = true;
    public boolean extraIDs = false;
    public boolean storeHeightMapAsIntArray = true;*/

	//getters 
	public boolean ExtraIDs()
	{
		return state > noExtraIDs;
	}

	public boolean TracksAir()
	{
		return state > oldAirTracking;
	}

	//setOptions
	public void setOptions(String option)
	{
		if(option.equals(Names.V100))
		{
			state = oldFileFormat;
		}
		else if(option.equals(Names.V131))
		{
			state = oldAirTracking;
		}
		else if(option.equals(Names.V152))
		{
			state = noExtraIDs;
		}
		if(option.equals(Names.V152_IDs))
		{
			state = noExtraHeight;
		}
		else if(option.equals(Names.V160))
		{
			state = CCforMC120;
		}
	}

	
	//get option choices
	public String getFormatName()
	{
		return Names.cubicChunks;
	}

	public String getFormatTooltip()
	{
		return Names.cubicChunks_TIP;
	}

	public final String[] OPTIONS = new String[]
		{
			Names.V160,
			Names.V152_IDs,
			Names.V152,
			Names.V131,
			Names.V100
		};
	public String[] getOptions()
	{return OPTIONS;}
	
	public final String[] TIPS = new String[]
	   		{
	   			Names.V160_TIP,
	   			Names.V152_IDs_TIP,
	   			Names.V152_TIP,
	   			Names.V131_TIP,
	   			Names.V100_TIP
	   		};
	public String[] getTips()
	{return TIPS;}

	public int getDefaultIndex()
	{
		return 2;
	}
	
	
	
	//settings result functions
	public String regionFileStartString()
	{
		return state == oldFileFormat ? "r." : "r2.";
	}

	public void setCubeAirOr0(File saveDir, int x, int y, int z)
	{
		if(state <= oldAirTracking)
		{
			RegionFileCache.setCube0(saveDir, x, y, z);
		}
		else
		{
			RegionFileCache.setCubeAir(saveDir, x, y, z);
		}
	}

	public DataOutputStream getChunkOutputStream(File saveDir, int x, int z)
	{
		return (state == oldFileFormat) ? 
    		RegionFileCacheOld.getChunkOutputStream(saveDir, x, z) :
    		RegionFileCache.getChunkOutputStream(saveDir, x, z);
	}

	public DataOutputStream getCubeOutputStream(File saveDir, int x, int y, int z)
	{
		return (state == oldFileFormat) ? 
			RegionFileCacheOld.getCubeOutputStream(saveDir, x, y, z) :
			RegionFileCache.getCubeOutputStream(saveDir, x, y, z);
	}

	public DataInputStream getChunkInputStream(File saveDir, int x, int z)
	{
		return (state == oldFileFormat) ? 
        		RegionFileCacheOld.getChunkInputStream(saveDir, x, z) : 
    			RegionFileCache.getChunkInputStream(saveDir, x, z);
	}

	public DataInputStream getCubeInputStream(File saveDir, int x, int y, int z)
	{
		return (state == oldFileFormat) ? 
    		RegionFileCacheOld.getCubeInputStream(saveDir, x, y, z) : 
			RegionFileCache.getCubeInputStream(saveDir, x, y, z);
	}

	public void handleExtraIDsAndStackShift(ChunkCube cube)
	{
		if(state >= noExtraHeight)
		{
			if(cube.blocks instanceof CubeBlockData12Bit)
			{
				cube.blocks = CubeBlockData16Bit.get(cube.blocks, 4);
				CubicLoader.stackShift = 1;
			}
			else
			{
				CubicLoader.stackShift = 0;
			}
		}
		else//if(state <= noExtraIDs)
		{
			if(cube.blocks instanceof CubeBlockData16Bit)
			{
				cube.blocks = CubeBlockData12Bit.get(cube.blocks, 4);
				CubicLoader.stackShift = -1;
			}
			else
			{
				CubicLoader.stackShift = 0;
			}
		}
	}

	public void putHeightMapToCompound(CompoundTag compound, int[] heightMap)
	{
        if(state >= CCforMC120)
        {
        	compound.putIntArray("HeightMap", heightMap);
        }
        else
        {
        	compound.putShortArray("HeightMap", Cast.toShortArray(heightMap));
        }
	}

	public ICubeBlockData getCubeBlockData()
	{
		if(ExtraIDs())
		{
			return new CubeBlockData16Bit(4);
		}
		else
		{
			return new CubeBlockData12Bit(4);
		}
	}
}
