package com.pythagdev;
//Taken from Minecraft

public class ChunkCoordIntPair
{

    public ChunkCoordIntPair(int i, int j)
    {
        chunkXPos = i;
        chunkZPos = j;
    }

    public static int chunkXZ2Int(int i, int j)
    {
        return (i >= 0 ? 0 : 0x80000000) | (i & 0x7fff) << 16 | (j >= 0 ? 0 : 0x8000) | j & 0x7fff;
    }

    public int hashCode()
    {
        return chunkXZ2Int(chunkXPos, chunkZPos);
    }

    public boolean equals(Object obj)
    {
        ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)obj;
        return chunkcoordintpair.chunkXPos == chunkXPos && chunkcoordintpair.chunkZPos == chunkZPos;
    }

    public final int chunkXPos;
    public final int chunkZPos;
}
