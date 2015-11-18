package com.pythagdev.GUI;

import javax.swing.*;


/**Main Frame for the game*/
@SuppressWarnings("serial")
public class TextFrame extends JFrame
{
	public TextFrame()
	{
		setTitle("Minecraft Save Conversion Tool");
		
 
		setSize(WIDTH, HEIGHT);
		setResizable(true);
		
		add(new ToolPanel());
	}
	
	public static final int WIDTH = 500;
	public static final int HEIGHT = 400;
}