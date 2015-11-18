package com.pythagdev.GUI;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.TimerTask;

import com.pythagdev.*;

public class Convert extends TimerTask
{
	private ConvertPanel panel;
	public Convert(ConvertPanel textPanel)
	{
		panel = textPanel;
	}

	public void run()
	{
		Main.output = panel.output;
		
		convert(panel.srcDirectory.getText(),
				panel.destDirectory.getText(),
				panel.srcFormat.getSelectedItem().toString(),
				panel.srcFormatOption.getSelectedItem().toString(),
				panel.destFormat.getSelectedItem().toString(),
				panel.destFormatOption.getSelectedItem().toString(),
				panel.copyMinorFilesButton.isSelected());
		
		Main.println();
		Main.scrollToEnd();
	}
	
	public static void convert(
			String srcFileName, String destFileName,
			String srcFormat, String srcFormatOption,
			String destFormat, String destFormatOption,
			boolean copyMinorFiles)
	{
		File srcfile = new File(srcFileName);
		if(!srcfile.exists() || !srcfile.isDirectory())
		{
			Main.println("Error: Src Directory does not exist");
			Main.scrollToEnd();
			return;
		}
		
		File destfile = new File(destFileName);
		destfile.mkdirs();
		
		IChunkLoader srcLoader = getChunkLoader(srcFormat, srcFormatOption, srcfile);
		IChunkLoader destLoader = getChunkLoader(destFormat, destFormatOption, destfile);
		
		if(srcLoader == null || destLoader == null)
		{
			Main.println("Error: did not recognize format specifier");
			Main.println(" Caused by: either internal error or hacked textbox");
			Main.scrollToEnd();
			return;
		}

		srcLoader.optimizeForDest(destLoader);
		destLoader.optimizeForSrc(srcLoader);
		
		List<ChunkCoordIntPair> chunks;
		try
		{
			chunks = srcLoader.existingChunksIn(srcfile);
		}
		catch(Exception e)
		{
			Main.println("Error: chunk search threw an exception: ");
			Main.print(e);
			Main.scrollToEnd();
			return;
		}
		
		if(chunks.isEmpty())
		{
			Main.println("Error: could not find files!");
			Main.scrollToEnd();
			return;
		}

		Main.println("Num Chunks in world: " + chunks.size());
		Main.scrollToEnd();
		
		int n = 0;
		
		for(ChunkCoordIntPair pos : chunks)
		{
			try
			{
				destLoader.saveChunk(srcLoader.loadChunk(pos.chunkXPos, pos.chunkZPos));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			++n;
			if((n % 1000) == 0)
			{
				Main.println("Chunks processed: " + n);
			}
		}
		
		File netherFile = new File(srcfile, "DIM-1");
		if(netherFile.exists())
		{
			File netherDestFile = new File(destfile, "DIM-1");
			netherDestFile.mkdirs();
			
			chunks = srcLoader.existingChunksIn(netherFile);

			Main.println("Num Chunks in Nether: " + chunks.size());
			Main.scrollToEnd();
			
			srcLoader.setFile(netherFile);
			destLoader.setFile(netherDestFile);
			
			n = 0;
			
			for(ChunkCoordIntPair pos : chunks)
			{
				try
				{
					destLoader.saveChunk(srcLoader.loadChunk(pos.chunkXPos, pos.chunkZPos));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				++n;
				if((n % 1000) == 0)
				{
					Main.println("Chunks processed: " + n);
				}
			}
		}

		Main.println("All chunks processed.");
		
		
		//copy minor files
		if(copyMinorFiles && !srcfile.equals(destfile))
		{
			Main.println("Copying misc. files.");
		
			copyFile(srcfile, destfile, "level.dat");
			copyFile(srcfile, destfile, "level.dat_old");
			copyFile(srcfile, destfile, "session.lock");
			
			File srcDataFolder = new File(srcfile, "data");
			File destDataFolder = new File(destfile, "data");
			
			srcDataFolder.mkdirs();
			
			File files[] = srcDataFolder.listFiles();
	    	if(files == null)
	    	{
	    		Main.println("Could not find map data. Skipping.");
	    	}
	    	else
	    	{
		    	for(File file : files)
		    	{
		    		String fileName = file.getName();
					copyFile(srcDataFolder, destDataFolder, fileName);
		    	}
	    	}

			Main.println("Misc. files copied.");
		}
		
		Main.println("Conversion complete.");
		
		return;
	}
	
	public static IChunkLoader getChunkLoader(String name, String option, File file)
	{
		if(name.equals(Names.anvil))
		{
			AnvilLoader loader = new AnvilLoader(file, option);
			
			return loader;
		}
		else if(name.equals(Names.scaevolus))
		{
			ScaevolusLoader loader = new ScaevolusLoader(file, option);
			
			return loader;
		}
		else if(name.equals(Names.cubicChunks))
		{
			CubicLoader loader = new CubicLoader(file, option);
			
			return loader;
		}
		else if(name.equals(Names.yMod))
		{
			YModLoader loader = new YModLoader(file, option);
			
			int extraBitsAdded = 2;//default
			
			if(option.equals("256"))
			{
				extraBitsAdded = 1;
			}
			else if(option.equals("1024"))
			{
				extraBitsAdded = 3;
			}
			else if(option.equals("2048"))
			{
				extraBitsAdded = 4;
			}
			else if(option.equals("4096"))
			{
				extraBitsAdded = 5;
			}
			
			loader.setExtraBitsAdded(extraBitsAdded);
			
			return loader;
		}
		return null;
	}
	
	public static void copyFile(File sourceFolder, File destFolder, String fileName)
	{
		File source = new File(sourceFolder, fileName);
		if(!source.exists())
		{
			return;
		}
		
		File dest = new File(destFolder, fileName);
		dest.getParentFile().mkdirs();
		
		try
		{
			RandomAccessFile srcFile = new RandomAccessFile(source, "r");
			RandomAccessFile destFile = new RandomAccessFile(dest, "rw");
			
			for(int offset = 0; offset < srcFile.length(); ++offset)
			{
				destFile.write(srcFile.readByte());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
