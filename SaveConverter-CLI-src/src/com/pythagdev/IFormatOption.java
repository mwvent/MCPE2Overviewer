package com.pythagdev;

//import com.pythagdev.GUI.Names;

public interface IFormatOption
{
	/*public final String formatName;
	public final String[] optionNames;
	public String[] optionTips;
	public final int defSelected;
	
	public FormatOption(String str1, String[] str2, String[] str3, int def)
	{
		formatName = str1;
		optionNames = str2;
		optionTips = str3;
		defSelected = def;
	}*/
	public String getFormatName();
	public String[] getOptions();
	public String[] getTips();
	public int getDefaultIndex();
	public String getFormatTooltip();
}
