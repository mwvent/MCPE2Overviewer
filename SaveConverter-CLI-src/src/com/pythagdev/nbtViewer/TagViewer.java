package com.pythagdev.nbtViewer;

import javax.swing.*;

public abstract class TagViewer extends JPanel
{
	private static final long serialVersionUID = 8790911054292980005L;

	protected final TagViewer previous;
	
	public TagViewer(TagViewer prev)
	{
		previous = prev;
	}
}
