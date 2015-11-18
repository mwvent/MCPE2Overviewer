package com.pythagdev;

public interface INibbleArray
{
    public int get(int x, int y, int z);

    public void set(int x, int y, int z, int val);

    public boolean isValid();

    public void setAll(int br);
    
    /**Gets the underlying data byte array*/
    public byte[] getData();

    /**Creates an Anvil version of this, and returns it.
     * If this is already Anvil, returns this.*/
    public INibbleArray toAnvil();
    /**Creates a pre-Anvil version of this, and returns it.
     * If this is already pre-Anvil, returns this.*/
    public INibbleArray toOld();
}
