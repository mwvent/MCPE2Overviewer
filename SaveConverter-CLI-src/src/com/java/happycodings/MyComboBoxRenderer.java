/* Based on code copied from http://www.java.happycodings.com/Java_Swing/code17.html
 *  on February 19, 2012.
 * No obvious license agreements apply; if any are found that conflict with
 *  this code's usage in the SaveConverter, please rewrite.
 *  
 * Do good not evil.*/
package com.java.happycodings;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;


public class MyComboBoxRenderer extends BasicComboBoxRenderer
{
	private static final long serialVersionUID = -2954846877309626696L;
	
	public MyComboBoxRenderer()
	{
		tooltips = new String[0];
	}
	
	public MyComboBoxRenderer(String[] tooltips)
	{
		this.tooltips = tooltips;
	}
	
	public String[] tooltips;

	public Component getListCellRendererComponent(JList list, Object value, 
			int index, boolean isSelected, boolean cellHasFocus)
	{
        if (isSelected)
        {
	        setBackground(list.getSelectionBackground());
	        setForeground(list.getSelectionForeground());
            if (index >= 0 && index < tooltips.length)
            {
            	list.setToolTipText(tooltips[index]);
            }
        }
        else
        {
	        setBackground(list.getBackground());
	        setForeground(list.getForeground());
	    }
	        
	    setFont(list.getFont());
	    setText((value == null) ? "" : value.toString());
	    return this;
	}
}
