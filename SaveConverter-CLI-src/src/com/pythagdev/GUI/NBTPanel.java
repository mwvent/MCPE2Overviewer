package com.pythagdev.GUI;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import com.java.happycodings.MyComboBoxRenderer;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;
import com.pythagdev.nbtViewer.CompoundViewer;

import static com.pythagdev.GUI.Names.*;


/**The main panel. This takes care of Draw calls*/
public class NBTPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = -4290385490601775111L;
	
	public NBTPanel()
	{
		//init the File Chooser
		//String s1 = System.getProperty("user.home", ".");
		fileChooser = new JFileChooser(new File("."));
		
		//Init the Combo Box
		srcFormat = new JComboBox();
		for(String option : options)
		{
			srcFormat.addItem(option);
		}
		srcFormat.addActionListener(this);
		srcFormat.setSelectedIndex(defSelectedOption);
		
		srcFormat.setRenderer(new MyComboBoxRenderer(tooltips));
		srcFormat.setToolTipText(overallTip);
		
		//get the default directory
		File defaultDir = new File("%appdata%\\.minecraft");
		if(!defaultDir.exists())
		{
			defaultDir = new File("");
		}
		String name = defaultDir.getAbsolutePath();
		
		//init the directory fields
		srcDirectory = new JTextField(name);
		srcDirectory.setColumns(44);
		srcDirectory.addActionListener(this);
		
		//init the activate chooser buttons
		srcChooser = new JButton("...");
		srcChooser.addActionListener(this);
		
		//init Go button
		loadButton = new JButton("Load");
		loadButton.addActionListener(this);
		
		//panel for upper half of screen
		layout = new SaveConverterLayout(this, 10);
		setLayout(layout);

		layout.add(srcDirectory, 0.5, 0.01, 0.5, 0.0);
		layout.add(srcChooser, 0.01, 0.08999, 0.0, 0.0);
		layout.add(srcFormat, 0.5, 0.09, 0.5, 0.0);
		layout.add(loadButton, 0.99, 0.08999, 1.0, 0.0);
		
		//init viewer panels
		viewerPanel1 = new JPanel();
		viewerPanel1.setLayout(new GridLayout(1, 1));
		viewerPanel2 = new JPanel();
		viewerPanel2.setLayout(new GridLayout(1, 1));
		
		layout.add(viewerPanel1, 0.2, 0.22, 0.5, 0.0);
		layout.add(viewerPanel2, 0.7, 0.22, 0.5, 0.0);
		/*upperLayout.add(text1Panel, 0.5, 0.0, 0.5, 0.0);
		upperLayout.add(sourceFormatPanel, 0.15, 0.25, 0.5, 0.5);
		upperLayout.add(srcDirectory, 0.5, 0.25, 0.4, 0.5);
		upperLayout.add(srcChooser, 0.925, 0.25, 0.5, 0.5);
		upperLayout.add(text2Panel, 0.5, 0.45, 0.5, 0.5);
		upperLayout.add(destFormatPanel, 0.15, 0.65, 0.5, 0.5);
		upperLayout.add(destDirectory, 0.5, 0.65, 0.4, 0.5);
		upperLayout.add(destChooser, 0.925, 0.65, 0.5, 0.5);
		upperLayout.add(goButton, 0.4, 1.0, 0.5, 1.1);
		upperLayout.add(copyMinorFilesButton, 0.7, 0.85, 0.5, 0.5);*/
	}
	
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource(); 
		if(source == loadButton)
		{
			viewerPanel1.removeAll();
			viewerPanel2.removeAll();
			
			CompoundTag compound = new CompoundTag();
			
			switch(srcFormat.getSelectedIndex())
			{
			case(0): //Uncompressed NBT
			{
				File file = new File(srcDirectory.getText());
				try
				{
					compound = NbtIo.read(file);
				}
				catch (IOException e)
				{e.printStackTrace();}
			}break;
			case(1): //Compressed NBT (.dat)
			{
				File file = new File(srcDirectory.getText());
				if (file.exists())
				{
					try
					{
						FileInputStream dis = new FileInputStream(file);
				        try
				        {
				            compound = NbtIo.readCompressed(dis);
				        }
				        finally
				        {
				            dis.close();
				        }
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}break;
			case(2): //McRegion (.mcr, .mca)
			{
				//TODO:
			}break;
			}
			
			CompoundViewer viewer = new CompoundViewer(null, compound, viewerPanel2);
			viewerPanel1.add(viewer);
			validate();
			repaint();
		}
		else if(source == srcChooser)
		{
			int result = fileChooser.showOpenDialog(Main.window);
			if(result == JFileChooser.APPROVE_OPTION)
			{
				//set text
				File selectedFile = fileChooser.getSelectedFile();
				srcDirectory.setText(selectedFile.getPath());
				
				setDefaultFormatHelper(selectedFile.getName());
			}
		}
		else if(source == srcDirectory)
		{
			String text = srcDirectory.getText();
			int index = text.lastIndexOf("\\");
			setDefaultFormatHelper(text.substring(index+1));
		}
	}
	
	private void setDefaultFormatHelper(String fileName)
	{
		if(fileName.endsWith(".mcr") || fileName.endsWith(".mca"))
		{
			if(fileName.startsWith("r2."))
			{
				srcFormat.setSelectedItem(McRegion_Small);
			}
			else
			{
				srcFormat.setSelectedItem(McRegion);
			}
		}
		else if(fileName.endsWith(".dat") || fileName.endsWith(".dat_old"))
		{
			srcFormat.setSelectedItem(CompressedNBT);
		}
	}
	
	
	SaveConverterLayout layout;
	
	public JComboBox srcFormat;
	public JTextField srcDirectory;
	public JButton srcChooser;
	public JButton loadButton;
	public JPanel viewerPanel1;
	public JPanel viewerPanel2;
	
	private JFileChooser fileChooser;
	

	private static int defSelectedOption = 1;
	private static final String options[] =
	{
		RawNBT,
		CompressedNBT,
		McRegion + "<NOT_YET_SUPPORTED!>",
		McRegion_Small + "<NOT_YET_SUPPORTED!>"
	};
	private static final String tooltips[] =
	{
		"Raw NBT Data. Not sure if this will actually ever be used. Unzipped dat files?",
		"Compressed NBT Data. Used in .dat files.",
		"McRegion stored NBT Data. Used in .mcr and .mca files.",
		"McRegion stored NBT Data, but with smaller McRegion sectors. Used in CubicChunks  r2.*.*.*.mcr  files. Created by Robinton and Nocte."
	};
	private static final String overallTip = "The format of the file to be loaded. This will set itself if you use the file chooser or type in a file and press enter.";
}