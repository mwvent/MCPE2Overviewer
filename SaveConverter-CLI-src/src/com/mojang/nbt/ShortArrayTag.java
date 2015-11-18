package com.mojang.nbt;

/**
 * Copyright Robinton. (Even though it's in com.mojang.nbt)
 * 
 * Don't do evil.
 * 
 * Feel free to base your works on this class. Just give me credit. ;)
 */

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortArrayTag extends Tag
{
    public ShortArrayTag(String s)
    {
        super(s);
    }
    
    public ShortArrayTag(String s, short ai[])
    {
        super(s);
        data = ai;
    }

    void write(DataOutput dataoutput)
        throws IOException
    {
        dataoutput.writeInt(data.length);
        for(int i = 0; i < data.length; i++)
        {
            dataoutput.writeShort(data[i]);
        }
    }

    void load(DataInput datainput)
        throws IOException
    {
        int len = datainput.readInt();
        
        data = new short[len];
        
        for(int n = 0; n < data.length; n++)
        {
        	data[n] = datainput.readShort();
        }
    }

    public byte getId()
    {
        return 12;
    }

    public String toString()
    {
        return (new StringBuilder()).append("[").append(data.length).append(" shorts]").toString();
    }

    public Tag copy()
    {
        return new ShortArrayTag(getName(), data);
    }

    public short data[];
}
