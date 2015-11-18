package com.pythagdev;

public interface ICubeBlockData
{
	public abstract int getID(int offset);
	public abstract int getID(int x, int y, int z);

	public abstract void setID(int offset, int id);
	public abstract void setID(int x, int y, int z, int id);

	public abstract int getMeta(int offset);
	public abstract int getMeta(int x, int y, int z);

	public abstract void setMeta(int offset, int md);
	public abstract void setMeta(int x, int y, int z, int md);
	
	
	public abstract boolean isValid();

	public void storeInCompound(com.mojang.nbt.CompoundTag level);
	public boolean calculateIsAir();

	public static final int NUM_CUBE_BLOCKS = 16 * 16 * 16;

	public abstract int getNumIDs();
}
