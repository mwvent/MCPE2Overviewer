package com.pythagdev;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IChunkLoader
{
    public abstract Chunk loadChunk(int i, int j)
        throws IOException;

    public abstract void saveChunk(Chunk chunk)
        throws IOException;

    public abstract void saveExtraChunkData(Chunk chunk)
        throws IOException;

    public abstract void func_814_a();

    public abstract void saveExtraData();
    
    public abstract List<ChunkCoordIntPair> existingChunksIn(File file);
    
    public abstract int lowestUsableCube();
    public abstract int highestUsableCube();
    
    public abstract void setFile(File file);

	public abstract void optimizeForDest(IChunkLoader destLoader);
	public abstract void optimizeForSrc(IChunkLoader srcLoader);
	
	
}
