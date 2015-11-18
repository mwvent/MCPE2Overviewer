package com.pythagdev.GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ToolPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 104157308322562545L;
	
	private ConvertPanel convertPanel;
	private NBTPanel nbtPanel;
	private JComponent helpPanel;
	
	private JPanel menuPanel;
	private JToggleButton convertButton;
	private JToggleButton nbtButton;
	private JToggleButton helpButton;
	
	public ToolPanel()
	{
		//set preferred size to avoid crash
		Dimension size = new Dimension(TextFrame.WIDTH, TextFrame.HEIGHT-50);
		setPreferredSize(size);
		
		//init layout manager
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		//create menu
		menuPanel = new JPanel();
		
		convertButton = new JToggleButton("SaveConverter");
		nbtButton = new JToggleButton("NBTViewer");
		helpButton = new JToggleButton("Help");
		
		convertButton.addActionListener(this);
		nbtButton.addActionListener(this);
		helpButton.addActionListener(this);
		
		menuPanel.add(convertButton);
		menuPanel.add(nbtButton);
		menuPanel.add(helpButton);

		GridBagConstraints menuConstraints = new GridBagConstraints();
		menuConstraints.weighty = 0;
		add(menuPanel, menuConstraints);
		
		//init lower panel constraints
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.gridy = 2;
		panelConstraints.weighty = 50;
		panelConstraints.weightx = 1;
		panelConstraints.fill = GridBagConstraints.BOTH;
		
		//init convert panel, and display
		convertPanel = new ConvertPanel();
		convertPanel.setPreferredSize(size);
		add(convertPanel, panelConstraints);
		
		//init nbt viewer panel, and set invisible
		nbtPanel = new NBTPanel();
		nbtPanel.setPreferredSize(size);
		add(nbtPanel, panelConstraints);
		
		//init help panel, and set invisible
		helpPanel = HelpPanel.get();
		helpPanel.setPreferredSize(size);
		add(helpPanel, panelConstraints);
		
		//init visible tool:
		setStatesHelper(convertButton);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();

		setStatesHelper(source);
	}
	
	private void setStatesHelper(Object source)
	{
		boolean convertSelected = source == convertButton;
		convertPanel.setVisible(convertSelected);
		convertButton.setSelected(convertSelected);

		boolean nbtSelected = source == nbtButton;
		nbtPanel.setVisible(nbtSelected);
		nbtButton.setSelected(nbtSelected);

		boolean helpSelected = source == helpButton;
		helpPanel.setVisible(helpSelected);
		helpButton.setSelected(helpSelected);
	}
}
