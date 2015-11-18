package com.pythagdev.GUI;

import java.io.*;

import javax.swing.*;


/**The main panel. This takes care of Draw calls*/
public class HelpPanel
{
	public static JComponent get()
	{
		InputStream input = HelpPanel.class.getResourceAsStream("README.txt");
		StringBuilder readme = new StringBuilder();
		
		try
		{
			int ch = input.read();
			while(ch > 0)
			{
				readme.append((char)ch);
				ch = input.read();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		JTextArea text = new JTextArea(readme.toString());
		text.setEditable(false);
		text.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(text);
		return scroll;
	}
	
	/*JTextArea text;
	JScrollPane scroll;*/
}