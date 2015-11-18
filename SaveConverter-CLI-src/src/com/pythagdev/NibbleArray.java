package com.pythagdev;

//Taken from Minecraft

public class NibbleArray implements INibbleArray
{
    public NibbleArray(int i)
    {
        data = new byte[i >> 1];
    }

    public NibbleArray(byte abyte0[])
    {
        data = abyte0;
    }
    
    public NibbleArray(DataLayer dat)
    {
    	this(4096);

    	for(int x = 0; x < 16; ++x)
    	{
    		for(int y = 0; y < 16; ++y)
        	{
        		for(int z = 0; z < 16; ++z)
        		{
        			set(x, y, z, dat.get(x, y, z));
        		}
        	}
    	}
    }

    public int get(int i, int j, int k)
    {
    	try//temp
    	{
	        int l = i << 8 | k << 4 | j;
	        int i1 = l >> 1;
	        int j1 = l & 1;
	        if(j1 == 0)
	        {
	            return data[i1] & 0xf;
	        } else
	        {
	            return data[i1] >> 4 & 0xf;
	        }
    	}
    	catch(ArrayIndexOutOfBoundsException e)
    	{
    		e.printStackTrace();
    		return 0;
    	}
    }

    public void set(int i, int j, int k, int l)
    {
        int i1 = i << 8 | k << 4 | j;
        int j1 = i1 >> 1;
        int k1 = i1 & 1;
        if(k1 == 0)
        {
            data[j1] = (byte)(data[j1] & 0xf0 | l & 0xf);
        } else
        {
            data[j1] = (byte)(data[j1] & 0xf | (l & 0xf) << 4);
        }
    }

    public boolean isValid()
    {
        return data != null;
    }

    public void setAll(int br)
    {
        byte val = (byte) ((br | (br << 4)) & 0xff);
        for (int i = 0; i < data.length; i++)
        {
            data[i] = val;
        }
    }

	public byte[] getData()
	{
		return data;
	}
    
    public DataLayer toAnvil()
    {
    	return new DataLayer(this, 4);
    }
    public NibbleArray toOld()
    {
    	return this;
    }

    public final byte data[];
}
