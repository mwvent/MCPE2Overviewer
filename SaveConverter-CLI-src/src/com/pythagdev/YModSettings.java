package com.pythagdev;

import com.pythagdev.GUI.Names;

/**YModSettings handles settings modifying the old YMod save format.*/
public class YModSettings implements IFormatOption
{
	protected int extraBits = 0;
	
	//get option choices
	public String getFormatName()
	{
		return Names.yMod;
	}

	public String getFormatTooltip()
	{
		return Names.yMod_TIP;
	}

	public final String[] OPTIONS = new String[]
        {
			Names.s128,
			Names.s256,
			Names.s512,
			Names.s1024,
			Names.s2048,
			Names.s4096
		};
	public String[] getOptions()
	{return OPTIONS;}
	
	public final String[] TIPS = new String[]
		{
			Names.s128_TIP,
			Names.s256_TIP,
			Names.s512_TIP,
			Names.s1024_TIP,
			Names.s2048_TIP,
			Names.s4096_TIP
		};
	public String[] getTips()
	{return TIPS;}

	public int getDefaultIndex()
	{
		return 1;
	}

	//setOptions
	public void setOptions(String option)
	{
	}
}
