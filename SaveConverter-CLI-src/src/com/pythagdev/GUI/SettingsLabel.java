package com.pythagdev.GUI;

import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JToolTip;


/**Class to display a string, plus a popup tooltip.*/
public class SettingsLabel extends JLabel
{
	private static final long serialVersionUID = 1359068613576063593L;
	
	private String tipText;
	
	public SettingsLabel(String text, String tipText)
	{
		super(text);
		this.tipText = tipText;
	}
	
	protected void processFocusEvent(FocusEvent event)
	{
		JToolTip tip = new JToolTip();
		tip.setTipText(tipText);
		tip.setVisible(true);
	}
	
	public String toString()
	{return super.getText();}
}