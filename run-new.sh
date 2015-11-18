#!/bin/bash

# requirements before running this script
pocketmine_folder="/home/minecraft"
#TODO - this script folder
script_folder="/home/minecraft/Scripts/mapmaker"
# we are using a standalone convertor rather than a minecraft server
# with this script but still using this var name from the old
# process where the working dir was in fact a minecraft server dir to run
# conversion with
minecraft_server_folder="/home/minecraft/Scripts/mapmaker/mcserver"
www_map_folder="/home/www/mcpe/map-overviewer-test"
convertor_jar="$script_folder/SaveConverter-cli.jar"

function doCon() {
	# copy map
	mapname="$1"
	echo "Copying map $1 to minecraft_server_folder/worlds"
	mkdir "$minecraft_server_folder/worlds" 2>/dev/null
	mkdir "$minecraft_server_folder/worlds/$mapname" 2>/dev/null
	rsync --times -r --exclude '*.mca' "$pocketmine_folder/worlds/$mapname/" "$minecraft_server_folder/worlds/$mapname"
	cd "$minecraft_server_folder/worlds/$mapname"
	# now keeps those nice converted mca files in case they dont need re-conversion
	#rm region/*.mca 2>/dev/null
	
	# TODO - we are keeping the level.dat that Pocketmine generates as Overviewer
	# seems to read it as being an anvil world anyway - should I not just replace
	# it with a generic anvil one in case future pocketmine versions create
	# one that tells overviewer it is an (incompatible) mcr world
	
	# create a folder when only new or updated regions and spawn are symlinked in
	# use the mtime of any previous anvil regions to see what needs to be re-converted
	# start by clearing any old symlinked worlds
	echo "Creating temp worlds-updates working folder"
	mkdir "$minecraft_server_folder/worlds-updates" 2>/dev/null
	rm -Rf "$minecraft_server_folder/worlds-updates/$mapname" 2>/dev/null
	mkdir "$minecraft_server_folder/worlds-updates/$mapname" 2>/dev/null
	# link base files - exclude region create an empty folder to symlink mcr's individualy
	ln -s "$minecraft_server_folder/worlds/$mapname/"* "$minecraft_server_folder/worlds-updates/$mapname/" 2>/dev/null
	rm "$minecraft_server_folder/worlds-updates/$mapname/region"
	mkdir "$minecraft_server_folder/worlds-updates/$mapname/region"
	
	# start symlinking updated regions
	echo "Symlinking region files requiring conversion to worlds-updates working folder"
    cd "$minecraft_server_folder/worlds/$mapname/region"
	find . -name "*.mcr" | while read fname; do 
	    region=$(basename "$fname" .mcr)
	    
	    # Ignore some regions pocketmine makes with ridiculous coords (just me?) 
	    # more than 500000 generates actual swearing from minecraft-overviewer!
	    x=$(echo $region | cut -d"." -f2)
	    z=$(echo $region | cut -d"." -f3)
	    if [ "$x" -lt "-500000" ] || [ "$x" -gt "500000" ] || [ "$z" -lt "-500000" ] || [ "$z" -gt "500000" ]
	    then
		    echo "Ignored region $region" 2>/dev/null
	    else
		this_mtime=$(stat $fname -c "%Y")
		this_filename=$(stat $fname -c "%n")
		last_filename="$minecraft_server_folder/worlds/$mapname/region/$region.mca"
		if [ -a "$last_filename" ]; then
		    # region has been converted before so check mtime to see if needs symlink
		    last_mtime=$(stat "$last_filename" -c "%Y")
		    if [ "$last_mtime" != "$this_mtime" ]; then
			# region mtime indicates updated
			ln -s "$minecraft_server_folder/worlds/$mapname/region/$this_filename" "$minecraft_server_folder/worlds-updates/$mapname/region/$this_filename"
		    fi
		else
		    # region is new no conversion yet exists
		    ln -s "$minecraft_server_folder/worlds/$mapname/region/$this_filename" "$minecraft_server_folder/worlds-updates/$mapname/region/$this_filename"
		fi
	    fi
	done
	# always include spawn
	if [ -a "r.0.0.mcr" ]; then
	    ln -s "$minecraft_server_folder/worlds/$mapname/region/r.0.0.mcr" "$minecraft_server_folder/worlds-updates/$mapname/region/r.0.0.mcr" 2>/dev/null
	fi
	
	# link a world folder - we are using a standalone convertor in this script but
	# keep it consistent with the paths used when using an actual minecraft
	# server to do it as in the old script
	rm "$minecraft_server_folder/world" 2>/dev/null
	ln -s "$minecraft_server_folder/worlds-updates/$mapname" "$minecraft_server_folder/world"
	
	# run conversion using convertor rather than mincraft server
	echo "Running conversion"
	java -jar "$convertor_jar" "$minecraft_server_folder/world" "$minecraft_server_folder/world" mcregion anvil
	
	# firstly copy the chunk mtimes from the old mcr files to the new mca ones
	# the format is the same so the php script will just replace bytes 4096-8191 in the mca with the same byte range 
	# from the mcr
	php "$script_folder/copyChunkMtimes.php" "$minecraft_server_folder/world/region" 
	
	# now mv the newly created mca files back out of the conversion folder overwriting any previous
	echo "Copying new mca files to minecraft_server_folder/worlds"
	mv "$minecraft_server_folder/worlds-updates/$mapname/region/"*.mca "$minecraft_server_folder/worlds/$mapname/region/" 
	ln -s "$minecraft_server_folder/worlds-updates/$mapname" "$minecraft_server_folder/world"

	# change new mca region files modification timestamps to match the original mcr ones & remove bad files
	echo "Syncing file mtimes"
	cd "$minecraft_server_folder/worlds/$mapname/region"
	find . -name "*.mca" | while read fname; do 
		region=$(basename "$fname" .mca)
		touch -d "$(date -r "$region".mcr)" "$region".mca
		
		# Delete mcas where there is no original mcr (conversion has created new regions with its own mapgen)
		if [ ! -f "$region".mcr ]
		then
			rm "$region".mca
		fi
		
		# Delete mcas and mcrs that were not originally present (the convertor has added a new mcr AND mca)
		if [ ! -f "$pocketmine_folder/worlds/$mapname/region/$region".mcr ]
		then
			rm "$region".mca
			rm "$region".mcr
		fi
		
		# Delete some regions pocketmine makes with ridiculous coords (just me?) 
		# more than 500000 generates actual swearing from minecraft-overviewer!
		x=$(echo $region | cut -d"." -f2)
		z=$(echo $region | cut -d"." -f3)
		if [ "$x" -lt "-500000" ] || [ "$x" -gt "500000" ] || [ "$z" -lt "-500000" ] || [ "$z" -gt "500000" ]
		then
			rm "$region".mca
			rm "$region".mcr
		fi
	done
	
}

echo "Starting map gen at $(date)"
startTime=$(date +%s)

# do the conversion from pocketmine mcr to anvil as mcoverviewer no longer works with mcr
# these are my worlds - just an example
doCon lobby
doCon plotworld
doCon world

nice -19 ionice -c 3 overviewer.py --config=$script_folder/overviewer_config.rst &
oPID=$!
cpulimit -l 80 -p $oPID

totalTime=$(echo $(date +%s) - $startTime | bc -l)
echo "Finished map gen at $(date) - took $totalTime seconds"
