package com.pythagdev.nbtViewer;

import static com.pythagdev.GUI.Names.Back;

import java.util.*;

import javax.swing.JPanel;

import com.mojang.nbt.*;

public class CompoundViewer extends AbstractListViewer
{
	private static final long serialVersionUID = 3476656851584842233L;
	
	private final CompoundTag compound;
	
	public CompoundViewer(TagViewer prev, CompoundTag compound, JPanel panel2)
	{
		super(prev, panel2);
		this.compound = compound;
		initList(getOptions());
	}
	
	protected Object[] getOptions()
	{
		ArrayList<String> options = new ArrayList<String>();
		
		if(previous != null)
		{
			options.add(Back);
		}
		
		Collection<Tag> tags = compound.getAllTags();
		for(Tag tag : tags)
		{
			StringBuilder option = new StringBuilder()
				  .append("<").append(tag.getClass().getSimpleName())
				  .append("> ")
				  .append(tag.getName());
			options.add(option.toString());
		}
		
		return options.toArray();
	}

	@Override
	protected Tag getSubTag(String option)
	{
		int indexOfSeparator = option.indexOf("> ");
		String name = option.substring(indexOfSeparator + 2);
		
		if(!compound.contains(name))
		{return null;}
		
		return compound.get(name);
	}
}
