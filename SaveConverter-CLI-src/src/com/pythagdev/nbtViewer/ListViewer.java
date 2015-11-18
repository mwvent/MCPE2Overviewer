package com.pythagdev.nbtViewer;

import static com.pythagdev.GUI.Names.Back;

import java.util.*;

import javax.swing.JPanel;

import com.mojang.nbt.*;

public class ListViewer extends AbstractListViewer
{
	private static final long serialVersionUID = 5452959564056322524L;
	
	private final ListTag<?> list;
	
	public ListViewer(TagViewer prev, ListTag<?> list, JPanel panel2)
	{
		super(prev, panel2);
		this.list = list;
		initList(getOptions());
	}
	
	protected Object[] getOptions()
	{
		ArrayList<String> options = new ArrayList<String>();
		
		if(previous != null)
		{
			options.add(Back);
		}
		
		for(int n = 0; n < list.size(); ++n)
		{
			Tag tag = list.get(n);
			StringBuilder option = new StringBuilder()
				  .append(n)
				  .append(": <").append(tag.getClass().getSimpleName())
				  .append("> ")
				  .append(tag.getName());
			options.add(option.toString());
		}
		
		return options.toArray();
	}

	@Override
	protected Tag getSubTag(String option)
	{
		int indexOfSeparator = option.indexOf(": <");
		String name = option.substring(0, indexOfSeparator);
		int index = Integer.parseInt(name);
		
		if(index < 0 || index >= list.size())
		{return null;}
		
		return list.get(index);
	}
}
