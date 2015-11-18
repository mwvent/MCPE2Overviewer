package com.pythagdev;

import com.pythagdev.GUI.Names;

/**AnvilSettings handles settings modifying Anvil save format
 * (Minecraft 1.2.0 thru <unknown>).*/
public class AnvilSettings implements IFormatOption
{
	protected int extraBits = 1;

	//setOptions
	public void setOptions(String option)
	{
		if(option.equals("32"))
		{
			extraBits = -2;
		}
		else if(option.equals("64"))
		{
			extraBits = -1;
		}
		else if(option.equals("128"))
		{
			extraBits = 0;
		}
		/*else if(option.equals("256"))
		{
			extraBits = 1;
		}*/
		else if(option.equals("512"))
		{
			extraBits = 2;
		}
		else if(option.equals("1024"))
		{
			extraBits = 3;
		}
		else if(option.equals("2048"))
		{
			extraBits = 4;
		}
		else if(option.equals("4096"))
		{
			extraBits = 5;
		}
	}

	//get option choices
	public String getFormatName()
	{
		return Names.anvil;
	}

	public String getFormatTooltip()
	{
		return Names.anvil_TIP;
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
			Names.s4096
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
			Names.s4096_TIP
			/*Names.s32_Anvil_TIP,
			Names.s64_Anvil_TIP,
			Names.s128_Anvil_TIP,
			Names.s256_Anvil_TIP,
			Names.s512_Anvil_TIP,
			Names.s1024_Anvil_TIP,
			Names.s2048_Anvil_TIP,
			Names.s4096_Anvil_TIP*/
		};
	public String[] getTips()
	{return TIPS;}

	public int getDefaultIndex()
	{
		return 3;
	}
}
