package com.pythagdev;

public class BedrockRemover
{

    public static void convert(byte abyte0[])
    {
        for(int i = 0; i < abyte0.length; i++)
        {
            abyte0[i] = convTable[abyte0[i] & 0xff];
        }

    }

    private static byte convTable[];

    static 
    {
        convTable = new byte[256];
        try
        {
            convTable[0] = 0;
            
            for(int n = 1; n < 256; n++)
            {
                byte id = (byte)n;
                
                convTable[n] = id;
            }

            convTable[7] = 1;//turn bedrock to smooth-stone
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
