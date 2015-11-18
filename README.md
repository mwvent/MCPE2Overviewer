# MCPE2Dynmap
WARNING - This is not intended as a drop in script to magically make dynmap work for your MCPE server but as a help for server owners who wish to see how we are doing it on our server

The script(s) Wattz MCPE uses to convert the map to anvil and render it via Minecraft overviewer - feel free to copy and adapt but its not a drop in solution by any means

There are two conversion scripts

run.sh
	Copies world from pocketmine folder
	Compares timestamps against previous conversion
	Creates a folder with only the updated mcr files symlinked in
	Copies a generic level.dat in - pocketmine generated level.dat doesnt work with server
	symlinks that folder with only updates to world in a minecraft server folder
	runs the minecraft pc server to start converting the map
	monitors the server and output files - if server crashes while converting it removes the mcr that (probably) caused the crash and attempts to continue
	when conversion finsihed copies new mca files back to main copy with any older mca's
	copies the modification times from the mcr files to the mca files as they are lost in conversion and needed for overviewer to not re-render whole regions rather than chunks
	runs overviewer on new files

run-new.sh
	as above but uses a version of SaveConvertor.jar that I modified to allow cli only usage instead of the minecraft server and keep pocketmine level.dat . More reliable as does not stop on errors. But lighting is broken - not a problem if you use overviewers basic flat lighting model


To use this with the minecraft server conversion you will need to download one of course - I cannot supply it here
