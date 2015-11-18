package com.pythagdev;

public class Cast
{
	public static int[] toIntArray(short[] arr)
	{
		int[] retVal = new int[arr.length];
		for(int n = 0; n < arr.length; ++n)
		{
			retVal[n] = arr[n];
		}
		return retVal;
	}

	public static short[] toShortArray(int[] arr)
	{
		short[] retVal = new short[arr.length];
		for(int n = 0; n < arr.length; ++n)
		{
			retVal[n] = (short) arr[n];
		}
		return retVal;
	}
}
