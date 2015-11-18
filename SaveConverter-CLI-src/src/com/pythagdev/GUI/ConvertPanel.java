package com.pythagdev.GUI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;

import javax.swing.*;

import com.java.happycodings.MyComboBoxRenderer;
import com.pythagdev.*;


/**The main panel. This takes care of Draw calls*/
public class ConvertPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = -6069670565571668904L;
	
	public ConvertPanel()
	{
		//init the File Chooser
		//String s1 = System.getProperty("user.home", ".");
		fileChooser = new JFileChooser(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		//Init the Combo Boxes
		srcFormat = new JComboBox();
		destFormat = new JComboBox();
		for(IFormatOption option : options)
		{
			srcFormat.addItem(option.getFormatName());
			destFormat.addItem(option.getFormatName());
		}
		srcFormat.addActionListener(this);
		destFormat.addActionListener(this);
		
		  //(create tooltips)
		String tooltips[] = new String[options.length];
		for(int n = 0; n < options.length; ++n)
		{tooltips[n] = options[n].getFormatTooltip();}
		srcFormat.setRenderer(new MyComboBoxRenderer(tooltips));
		destFormat.setRenderer(new MyComboBoxRenderer(tooltips));
		
		IFormatOption defOption = options[defSelectedOption];
		srcFormatOption = new JComboBox(defOption.getOptions());
		srcFormatOption.setMaximumRowCount(10);
		destFormatOption = new JComboBox(defOption.getOptions());
		destFormatOption.setMaximumRowCount(10);

		srcFormatOption.setRenderer(new MyComboBoxRenderer(defOption.getTips()));
		destFormatOption.setRenderer(new MyComboBoxRenderer(defOption.getTips()));
		
		//set selected Format. Wait until the Options are inited to prevent error
		// and auto-set selected option index.
		srcFormat.setSelectedIndex(defSelectedOption);
		destFormat.setSelectedIndex(defSelectedOption);
		
		JPanel sourceFormatPanel = new JPanel();
		JPanel destFormatPanel = new JPanel();
		
		GridLayout formatPanelLayout = new GridLayout(2, 1);
		sourceFormatPanel.setLayout(formatPanelLayout);
		destFormatPanel.setLayout(formatPanelLayout);
		
		sourceFormatPanel.add(srcFormat);
		sourceFormatPanel.add(srcFormatOption);
		destFormatPanel.add(destFormat);
		destFormatPanel.add(destFormatOption);
		
		//get the default directory
		//TODO: get this to default-select Minecraft
		File defaultDir = new File("%appdata%/.minecraft");
		if(!defaultDir.exists())
		{
			defaultDir = new File("");
		}
		String name = defaultDir.getAbsolutePath();
		
		//init the directory fields
		srcDirectory = new JTextField(name);
		destDirectory = new JTextField(name);
		
		//set the directory fields' sizes
		srcDirectory.setColumns(25);
		destDirectory.setColumns(25);
		
		//init the activate chooser buttons
		srcChooser = new JButton("...");
		srcChooser.addActionListener(this);
		destChooser = new JButton("...");
		destChooser.addActionListener(this);
		
		
		//init Convert From label
		JPanel text1Panel = new JPanel();
		text1Panel.add(new JLabel("Convert from "));

		//init To label
		JPanel text2Panel = new JPanel();
		text2Panel.add(new JLabel(" to "));
		
		//init output Text Area
		output = new JTextArea();
		output.setEditable(false);
		output.setMinimumSize(new Dimension(300, 100));
		
		//init Go button
		goButton = new JButton("Convert!");
		goButton.addActionListener(this);
		
		//init copyMinorFilesButton 
		copyMinorFilesButton = new JCheckBox("Copy Misc. Files", true);
		
		//panel for upper half of screen
		JPanel upperPanel = new JPanel();
		SaveConverterLayout upperLayout = new SaveConverterLayout(upperPanel, 10);
		upperPanel.setLayout(upperLayout);

		upperLayout.add(text1Panel, 0.5, 0.0, 0.5, 0.0);
		upperLayout.add(sourceFormatPanel, 0.15, 0.25, 0.5, 0.5);
		upperLayout.add(srcDirectory, 0.5, 0.25, 0.4, 0.5);
		upperLayout.add(srcChooser, 0.925, 0.25, 0.5, 0.5);
		upperLayout.add(text2Panel, 0.5, 0.45, 0.5, 0.5);
		upperLayout.add(destFormatPanel, 0.15, 0.65, 0.5, 0.5);
		upperLayout.add(destDirectory, 0.5, 0.65, 0.4, 0.5);
		upperLayout.add(destChooser, 0.925, 0.65, 0.5, 0.5);
		upperLayout.add(goButton, 0.4, 1.0, 0.5, 1.1);
		upperLayout.add(copyMinorFilesButton, 0.7, 0.85, 0.5, 0.5);
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
		
		//set layout for whole screen
		GridLayout layout = new GridLayout(2, 1);
		setLayout(layout);
		
		//add all items to whole screen
		add(upperPanel);
		add(new JScrollPane(output));
	}
	
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource(); 
		if(source == goButton)
		{
			convertOnAnotherThread.schedule(new Convert(this), 0);
		}
		else if(source == srcChooser)
		{
			int result = fileChooser.showOpenDialog(Main.window);
			if(result == JFileChooser.APPROVE_OPTION)
			{
				srcDirectory.setText(fileChooser.getSelectedFile().getPath());
			}
		}
		else if(source == destChooser)
		{
			int result = fileChooser.showSaveDialog(Main.window);
			if(result == JFileChooser.APPROVE_OPTION)
			{
				destDirectory.setText(fileChooser.getSelectedFile().getPath());
			}
		}
		else if(source == srcFormat)
		{
			formatChangeHelper(srcFormatOption, srcFormat.getSelectedIndex());
		}
		else if(source == destFormat)
		{
			formatChangeHelper(destFormatOption, destFormat.getSelectedIndex());
		}
	}
	
	private void formatChangeHelper(JComboBox toUpdate, int selected)
	{
		toUpdate.removeAllItems();
		
		IFormatOption option = options[selected];
		for(int n = 0; n < option.getOptions().length; ++n)
		{
			toUpdate.addItem(new SettingsLabel(option.getOptions()[n], option.getTips()[n]));
		}
		
		((MyComboBoxRenderer)toUpdate.getRenderer()).tooltips = option.getTips();
		
		toUpdate.setSelectedIndex(option.getDefaultIndex());
	}
	
	public JComboBox srcFormat;
	public JComboBox destFormat;
	public JComboBox srcFormatOption;
	public JComboBox destFormatOption;
	public JTextField srcDirectory;
	public JTextField destDirectory;
	public JButton srcChooser;
	public JButton destChooser;
	public JButton goButton;
	public JToggleButton copyMinorFilesButton;
	public JTextArea output;
	
	private JFileChooser fileChooser;
	
	private Timer convertOnAnotherThread = new Timer();

	private static int defSelectedOption = 1;//ScaevolusLoader by default
	private static final IFormatOption options[] =
	{
		new AnvilSettings(),
		new ScaevolusSettings(),
		new CubicSettings(),
		new YModSettings()
	};
}