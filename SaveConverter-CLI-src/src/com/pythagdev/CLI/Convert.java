package com.pythagdev.CLI;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.TimerTask;

import com.pythagdev.*;

public class Convert
{
	public static void main(String[] args)
	{
		/*
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
		*/
		  for(int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
		if(args.length != 4) {
			System.out.println("usage java -jar cliconvertor.jar <sourceFolder> <destFolder> <sourceFormat> <destFormat> : Format can be anvil, mcregion, cubicChunks, yMod");
		}
		String sourceFolder = args[0];
		String destFolder = args[1];
		String sourceFormat = args[2];
		String destFormat = args[3];
		String formatOption = "128";
		Boolean copyMinorFiles = true;
		convert(sourceFolder, destFolder, sourceFormat, formatOption, destFormat, formatOption, copyMinorFiles);
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
			System.out.println("Error: Src Directory does not exist");
			return;
		}
		
		File destfile = new File(destFileName);
		destfile.mkdirs();
		
		IChunkLoader srcLoader = getChunkLoader(srcFormat, srcFormatOption, srcfile);
		IChunkLoader destLoader = getChunkLoader(destFormat, destFormatOption, destfile);
		
		if(srcLoader == null || destLoader == null)
		{
			System.out.println("Error: did not recognize format specifier");
			System.out.println("Format can be anvil, mcregion, cubicChunks, yMod");
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
			System.out.println("Error: chunk search threw an exception: ");
			System.out.print(e);
			return;
		}
		
		if(chunks.isEmpty())
		{
			System.out.println("Error: could not find files!");
			return;
		}

		System.out.println("Num Chunks in world: " + chunks.size());
		
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
				System.out.println("Chunks processed: " + n);
			}
		}
		
		File netherFile = new File(srcfile, "DIM-1");
		if(netherFile.exists())
		{
			File netherDestFile = new File(destfile, "DIM-1");
			netherDestFile.mkdirs();
			
			chunks = srcLoader.existingChunksIn(netherFile);

			System.out.println("Num Chunks in Nether: " + chunks.size());
			
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
					System.out.println("Chunks processed: " + n);
				}
			}
		}

		System.out.println("All chunks processed.");
		
		
		//copy minor files
		if(copyMinorFiles && !srcfile.equals(destfile))
		{
			System.out.println("Copying misc. files.");
		
			copyFile(srcfile, destfile, "level.dat");
			copyFile(srcfile, destfile, "level.dat_old");
			copyFile(srcfile, destfile, "session.lock");
			
			File srcDataFolder = new File(srcfile, "data");
			File destDataFolder = new File(destfile, "data");
			
			srcDataFolder.mkdirs();
			
			File files[] = srcDataFolder.listFiles();
	    	if(files == null)
	    	{
	    		System.out.println("Could not find map data. Skipping.");
	    	}
	    	else
	    	{
		    	for(File file : files)
		    	{
		    		String fileName = file.getName();
					copyFile(srcDataFolder, destDataFolder, fileName);
		    	}
	    	}

			System.out.println("Misc. files copied.");
		}
		
		System.out.println("Conversion complete.");
		
		return;
	}
	
	public static IChunkLoader getChunkLoader(String name, String option, File file)
	{
		if(name.equals("anvil"))
		{
			AnvilLoader loader = new AnvilLoader(file, option);
			
			return loader;
		}
		else if(name.equals("mcregion"))
		{
			ScaevolusLoader loader = new ScaevolusLoader(file, option);
			
			return loader;
		}
		else if(name.equals("cubicChunks"))
		{
			CubicLoader loader = new CubicLoader(file, option);
			
			return loader;
		}
		else if(name.equals("yMod"))
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
