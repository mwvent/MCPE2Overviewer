package com.pythagdev.GUI;

import java.awt.*;

public class SaveConverterLayout implements LayoutManager
{
	public SaveConverterLayout(Container container, int size)
	{
		myContainer = container;
		positions = new double[2*size];
		sizeOffsets = new double[2*size];
	}
	
	public void add(Component component,
			double posX, double posY, double sizeOffsetX, double sizeOffsetY)
	{
		if(index >= positions.length)
		{
			System.out.println("Attempted to add too many items to SaveConverterLayout!");
			return;
		}
		
		//posY = posY * 0.9 + 0.1;
		
		myContainer.add(component);
		positions[index] = posX;
		positions[index+1] = posY;
		sizeOffsets[index] = sizeOffsetX;
		sizeOffsets[index+1] = sizeOffsetY;
		index += 2;
	}
	
	//empty
	@Override
	public void addLayoutComponent(String arg0, Component arg1)
	{
		
	}

	//empty
	@Override
	public void removeLayoutComponent(Component arg0)
	{
		
	}

	@Override
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		Dimension parentSize = parent.getSize();
		
		int containerWidth = parentSize.width - insets.left - insets.right;
		int containerHeight = parentSize.height - insets.top - insets.bottom;
		
		int num = parent.getComponentCount();
		for(int n = 0; n < num; ++n)
		{
			Component c = parent.getComponent(n);
			if(c.isVisible())
			{
				int n2 = n*2;
				
				Dimension size = c.getPreferredSize();
				
				double x1 = positions[n2] * containerWidth - size.width * sizeOffsets[n2];
				double y1 = positions[n2+1] * containerHeight - size.height * sizeOffsets[n2+1];
				
				c.setBounds((int)(x1) + insets.left, (int)(y1) + insets.top, size.width, size.height);
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container arg0)
	{
		return new Dimension(400, 200);
	}

	@Override
	public Dimension preferredLayoutSize(Container arg0)
	{
		return null;
	}
	
	//x, y top/left coordinates. 0.0 - 1.0
	private final double[] positions;
	/*{
		0.5, 0.0,//text1
		0.05, 0.25,//srcFormat
		0.5, 0.25,//srcFile
		0.925, 0.25,//srcChooser
		0.5, 0.5,//text2
		0.05, 0.725,//destFormat
		0.5, 0.725,//destFile
		0.925, 0.725,//destChooser
		0.5, 1.0,//go
	};*/
	
	//x, y size-usages. 0.0 - 1.0
	private final double[] sizeOffsets; /*= 
	{
		0.5, 0.0,//text1
		0.05, 0.5,//srcFormat
		0.4, 0.5,//srcFile
		0.5, 0.5,//srcChooser
		0.5, 0.5,//text2
		0.05, 0.5,//destFormat
		0.4, 0.5,//destFile
		0.5, 0.5,//destChooser
		0.5, 1.0,//go
	};*/
	
	public final Container myContainer;
	private int index = 0;
}
