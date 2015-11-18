package com.pythagdev;

import com.mojang.nbt.*;

public class MergedChunk
{
	/*public byte[] blocks;
	public byte[] data;*/
	public ICubeBlockData blocks;
	public long lastUpdate;
	public boolean isTerrainPopulated;
	
	public byte[] skylightMap;
	public byte[] blocklightMap;
	
    public ListTag<CompoundTag> chunkTileEntityMap;// = new NBTTagList();
    public ListTag<CompoundTag> entities;// = new NBTTagList();
    public ListTag<CompoundTag> tileTicks;
}
