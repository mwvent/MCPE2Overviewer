package com.pythagdev;

import com.pythagdev.GUI.Names;
import static com.pythagdev.GUI.Names.*;

/**ScaevolusSettings handles settings modifying the original Scaevolus save format
 * (Minecraft Beta thru Minecraft 1.1.0). It includes Vanilla-extended-height-mod
 * save formats, but not Anvil.*/
public class ScaevolusSettings implements IFormatOption
{
	protected int extraBits = 0;
	protected boolean extraIDs = false;
	
	//get option choices
	public String getFormatName()
	{
		return Names.scaevolus;
	}

	public String getFormatTooltip()
	{
		return Names.scaevolus_TIP;
	}

	public final String[] OPTIONS = new String[]
        {
			Names.s32,
			Names.s64,
			Names.s128,
			Names.s256,
			Names.s512,
			Names.s1024,
			Names.s2048,
			Names.s4096,
			Names.s4096IDs
		};
	public String[] getOptions()
	{return OPTIONS;}
	
	public final String[] TIPS = new String[]
		{
			Names.s32_TIP,
			Names.s64_TIP,
			Names.s128_TIP,
			Names.s256_TIP,
			Names.s512_TIP,
			Names.s1024_TIP,
			Names.s2048_TIP,
			Names.s4096_TIP,
			Names.s4096IDs_TIP
		};
	public String[] getTips()
	{return TIPS;}

	public int getDefaultIndex()
	{
		return 2;
	}

	//setOptions
	public void setOptions(String option)
	{
		int bits = 0;//default
		boolean ids = false;
		
		if(option.equals(s32))
		{
			bits = -2;
		}
		else if(option.equals(s64))
		{
			bits = -1;
		}
		else if(option.equals(s256))
		{
			bits = 1;
		}
		else if(option.equals(s512))
		{
			bits = 2;
		}
		else if(option.equals(s1024))
		{
			bits = 3;
		}
		else if(option.equals(s2048))
		{
			bits = 4;
		}
		else if(option.equals(s4096))
		{
			bits = 5;
		}
		else if(option.equals(s4096IDs))
		{
			ids = true;
		}
		
		extraBits = bits;
		extraIDs = ids;
	}
}
