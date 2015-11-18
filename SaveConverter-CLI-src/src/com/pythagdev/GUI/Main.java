package com.pythagdev.GUI;

import java.awt.*;
import java.io.*;

import javax.swing.*;

public class Main
{
	/**
	 * @param args {SrcWorldFolderName, DestWorldFolderName, SrcFormat, DestFormat}
	 */
	public static void main(String[] args)
	{
		try
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					//Turn off metal's use of bold fonts
	                UIManager.put("swing.boldMetal", Boolean.FALSE); 
	                
					//display screen
					window = new TextFrame();
					
					window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					window.setVisible(true);
				}
				
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	
	public static void println(String str)
	{
		if(output != null)
		{
			output.append(str + '\n');
		}
	}
	
	public static void println()
	{
		if(output != null)
		{
			output.append("\n");
		}
	}
	
	public static void print(Throwable throwable)
	{
		if(output != null && throwable != null)
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			PrintStream stream = new PrintStream(out);
			throwable.printStackTrace(stream);
			stream.flush();
			stream.close();
			
			output.append(out.toString());
			output.append("\n");
		}
	}
	
	public static void scrollToEnd()
	{
		if(output != null)
		{
			output.scrollRectToVisible(new Rectangle(5, output.getHeight(), 5, 5));
		}
	}

	public static JTextArea output;
	public static JFrame window;
}


