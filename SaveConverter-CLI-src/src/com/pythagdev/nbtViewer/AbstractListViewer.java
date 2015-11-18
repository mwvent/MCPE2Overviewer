package com.pythagdev.nbtViewer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import com.mojang.nbt.*;

import static com.pythagdev.GUI.Names.*;
import static com.mojang.nbt.Tag.*;

public abstract class AbstractListViewer extends TagViewer
{
	private static final long serialVersionUID = -5689638119678339770L;

	protected JList list;
	protected final JPanel panel2;
	protected Component viewed = null;
	
	public AbstractListViewer(TagViewer prev, JPanel panel2)
	{
		super(prev);
		this.panel2 = panel2;
	}
	
	protected void initList(Object[] options)
	{
		list = new JList(options);
		
		MouseListener mouseListener = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() < 3)
				{
					int index = list.locationToIndex(e.getPoint());
					onClicked(index, e.getClickCount());
				}
			}
		};
		list.addMouseListener(mouseListener);

		add(new JScrollPane(list));
	}

	protected void onClicked(int index, int clickCount)
	{
		Object object = list.getSelectedValue();
		
		if(object == null || !(object instanceof String))
		{
			return;
		}
		
		String option = (String) object;
		if(clickCount == 2 && option.equals(Back) && index == 0 && previous != null)
		{
			nullifyViewed();
			
			Container parent = getParent();
			parent.remove(this);
			parent.add(previous);
			parent.getParent().validate();
			previous.repaint();
		}
		else
		{
			if(option.equals(Back))
			{
				nullifyViewed();
				return;
			}
			
			Tag tag = getSubTag(option);
			
			if(tag == null)
			{
				return;
			}
			
			int id = tag.getId();
			
			if(id == TAG_End)
			{
				return;
			}
			
			nullifyViewed();
			
			if(clickCount == 2 && id == TAG_Compound)
			{
				CompoundTag tagC = (CompoundTag)tag;
				
				Container parent = getParent();
				parent.remove(this);
				parent.add(new CompoundViewer(this, tagC, panel2));
				parent.getParent().validate();
				parent.repaint();
			}
			else if(clickCount == 2 && id == TAG_List)
			{
				ListTag<?> tagL = (ListTag<?>)tag;
				
				Container parent = getParent();
				parent.remove(this);
				parent.add(new ListViewer(this, tagL, panel2));
				parent.getParent().validate();
				parent.repaint();
			}
			else
			{
				viewed = new JTextArea(getInfoString(tag, id));
				((JTextArea)viewed).setColumns(25);
				((JTextArea)viewed).setRows(15);
				panel2.add(new JScrollPane(viewed));
				panel2.getParent().validate();
				panel2.repaint();
			}
		}
	}

	protected String getInfoString(Tag tag, int id)
	{
		switch(id)
		{
			/*case(TAG_End):
			{
			}break;*/
			case(TAG_Byte):
			case(TAG_Short):
			case(TAG_Int):
			case(TAG_Long):
			case(TAG_Float):
			case(TAG_Double):
			case(TAG_String):
			{
				return tag.toString();
			}
			case(TAG_Byte_Array):
			{
				StringBuilder builder = new StringBuilder();
				ByteArrayTag arr = (ByteArrayTag)tag;
				byte[] data = arr.data;
				
				for(int n = 0; n < data.length; ++n)
				{
					builder.append(data[n]);
					if(n % 16 == 15)
					{
						builder.append("\n");
					}
					else
					{
						builder.append(", ");
					}
				}
				return builder.toString();
			}
			case(TAG_List):
			{
				ListTag<?> list = (ListTag<?>)tag;
				StringBuilder builder = new StringBuilder();
				for(int n = 0; n < list.size(); ++n)
				{
					Tag tag2 = list.get(n);
					builder.append(n)
						  .append(": <").append(tag2.getClass().getSimpleName())
						  .append("> ")
						  .append(tag2.getName())
						  .append(" = ")
						  .append(tag2.toString())
						  .append("\n");
				}
				return builder.toString();
			}
			case(TAG_Compound):
			{
				CompoundTag compound = (CompoundTag)tag;
				Collection<Tag> list = compound.getAllTags();
				StringBuilder builder = new StringBuilder();
				
				int n = 0;
				for(Tag tag2 : list)
				{
					builder.append(n)
						  .append(": <").append(tag2.getClass().getSimpleName())
						  .append("> ")
						  .append(tag2.getName())
						  .append(" = ")
						  .append(tag2.toString())
						  .append("\n");
					++n;
				}
				return builder.toString();
			}
			case(TAG_Int_Array):
			{
				StringBuilder builder = new StringBuilder();
				IntArrayTag arr = (IntArrayTag)tag;
				int[] data = arr.data;
				
				for(int n = 0; n < data.length; ++n)
				{
					builder.append(data[n]);
					if((n & 0xf) == 15)
					{
						builder.append("\n");
					}
					else
					{
						builder.append(", ");
					}
				}
				return builder.toString();
			}
			case(12)://Tag_Short_Array
			{
				StringBuilder builder = new StringBuilder();
				ShortArrayTag arr = (ShortArrayTag)tag;
				short[] data = arr.data;
				
				for(int n = 0; n < data.length; ++n)
				{
					builder.append(data[n]);
					if(n % 16 == 15)
					{
						builder.append("\n");
					}
					else
					{
						builder.append(", ");
					}
				}
				return builder.toString();
			}
		}
		
		return "";//default
	}
	
	protected void nullifyViewed()
	{
		if(viewed != null)
		{
			panel2.removeAll();
			viewed = null;
			panel2.repaint();
		}
	}

	protected abstract Tag getSubTag(String name);
}
