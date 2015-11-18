#!/bin/bash

pocketmine_folder="/home/minecraft"
script_folder="/home/minecraft/Scripts/mapmaker"
minecraft_server_folder="/home/minecraft/Scripts/mapmaker/mcserver"
www_map_folder="/home/www/mcpe/map-overviewer-test"
minecraft_server_script="nice -n 19 $minecraft_server_folder/run.sh"
minecraft_server_rcon_port="19140"
minecraft_server_rcon_password="yourpass"
mcrcon_binary="$minecraft_server_folder/mcrcon"

function pingMCServer() {
	serverping=$($mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password -c "save-off" 2>/dev/null | tr -d '\r' | tr -d '\n')
	server_ok=1
	test "$serverping" = "" && server_ok=0
	echo -n "$server_ok"
}

function stopMCServer() {
	$mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password -c "stop" 2>/dev/null
	if [ $# -eq 0 ]
	then
		# no PID supplied - find
		# relies on run.sh in minecraft server dir having an exec -a mapconvertor.jar launcher
		SERVER_PID=$(ps x | grep "mapconvertor.jar" | grep -v "grep" | awk '{print $1}');
	else
		SERVER_PID=$1
	fi
	
	# check server was ever running first
	if [ "$SERVER_PID" = "" ]; then
		SERVER_CLOSED=1
	else
		echo "Shutting down server"
		SERVER_CLOSED=0
	fi

	waitstart=$(date +%s)
	while [ $SERVER_CLOSED -eq 0 ]
	do
		if ps -p $SERVER_PID > /dev/null
		then
			# waiting for SERVER_CLOSED
			# timeout after 20 seconds send SIGINT
			if [ "$(echo $(date +%s) - $waitstart | bc -l)" -gt "20" ]
			then
				kill -9 $SERVER_PID
			fi
			sleep 1
		else
			SERVER_CLOSED=1
		fi
		
	done
}

function doCon() {
	# copy map
	mapname="$1"
	echo "Copying map $1"
	mkdir "$minecraft_server_folder/worlds" 2>/dev/null
	mkdir "$minecraft_server_folder/worlds/$mapname" 2>/dev/null
	rsync --times -r --exclude '*.mca' "$pocketmine_folder/worlds/$mapname/" "$minecraft_server_folder/worlds/$mapname"
	cd "$minecraft_server_folder/worlds/$mapname"
	# now keeps those nice converted mca files in case they dont need re-conversion
	#rm region/*.mca 2>/dev/null
	cp "$minecraft_server_folder/mcregion.level.dat" "$minecraft_server_folder/worlds/$mapname/level.dat"
	
	# create a folder when only new or updated regions and spawn are symlinked in
	# use the mtime of any previous anvil regions to see what needs to be re-converted
	# start by clearing any old symlinked worlds
	mkdir "$minecraft_server_folder/worlds-updates" 2>/dev/null
	rm -Rf "$minecraft_server_folder/worlds-updates/$mapname" 2>/dev/null
	mkdir "$minecraft_server_folder/worlds-updates/$mapname" 2>/dev/null
	# link base files - exclude region create an empty folder to symlink mcr's individualy
	ln -s "$minecraft_server_folder/worlds/$mapname/"* "$minecraft_server_folder/worlds-updates/$mapname/" 2>/dev/null
	rm "$minecraft_server_folder/worlds-updates/$mapname/region"
	mkdir "$minecraft_server_folder/worlds-updates/$mapname/region"
	# start symlinking updated regions
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
	
	# the following regions always need symlinking or the convertor will regen them
	if [ -a "r.0.0.mcr" ]; then
	    ln -s "$minecraft_server_folder/worlds/$mapname/region/r.0.0.mcr" "$minecraft_server_folder/worlds-updates/$mapname/region/r.0.0.mcr" 2>/dev/null
	fi
	if [ -a "r.0.-1.mcr" ]; then
	    ln -s "$minecraft_server_folder/worlds/$mapname/region/r.0.-1.mcr" "$minecraft_server_folder/worlds-updates/$mapname/region/r.0.-1.mcr" 2>/dev/null
	fi
	if [ -a "r.-1.0.mcr" ]; then
	    ln -s "$minecraft_server_folder/worlds/$mapname/region/r.-1.0.mcr" "$minecraft_server_folder/worlds-updates/$mapname/region/r.-1.0.mcr" 2>/dev/null
	fi
	if [ -a "r.-1.-1.mcr" ]; then
	    ln -s "$minecraft_server_folder/worlds/$mapname/region/r.-1.-1.mcr" "$minecraft_server_folder/worlds-updates/$mapname/region/r.-1.-1.mcr" 2>/dev/null
	fi	
	
	# link updates folder as curent world for java minecraft server to run conversion on
	rm "$minecraft_server_folder/world" 2>/dev/null
	ln -s "$minecraft_server_folder/worlds-updates/$mapname" "$minecraft_server_folder/world"
	
	# start server
	cd "$minecraft_server_folder"
	nohup $minecraft_server_script &
	SERVER_PID="$!"
	
	# loop until server startup has happened - once server starts accepting rcon connections
	# it indicates covnersion has finished
	# replay progress on console by counting how many mca regions have been created vs the
	# original mcr ones
	cd "$minecraft_server_folder/world/"
	CONV_FINISHED=0
	while [ $CONV_FINISHED -eq 0 ]
        do
		# check the process is still alive
		if ps -p $SERVER_PID > /dev/null
		then
			mcaCount=$(ls -l region/*.mca 2>/dev/null | wc -l)
			mcrCount=$(ls -l region/*.mcr 2>/dev/null | wc -l)
			donePercent=$(echo "scale=4; ( $mcaCount/$mcrCount ) * 100" | bc -l)
			printf "$donePercent%% complete \r"
			sleep 4
		else
			badregion=$(ls -lt "$minecraft_server_folder/world/region" | head -n 2 | tail -n 1 | cut -d ' ' -f 9 | cut -d 'm' -f 1 | xargs echo)
			printf "\nERROR Convertor died - attempt to remove region $badregion\n"
			rm "$minecraft_server_folder/world/region/$badregion"mca
			rm "$minecraft_server_folder/world/region/$badregion"mcr
			cd "$minecraft_server_folder"
			nohup $minecraft_server_script &
			SERVER_PID="$!"
			cd "$minecraft_server_folder/world/" 
		fi
		# check for sucessful rcon connection - server will be accepting rcon connections
		# once conversion has happened
		serverStatus="$(pingMCServer)"
		test "$serverStatus" = "1" && printf "\nConversion Complete\n"
		test "$serverStatus" = "1" && CONV_FINISHED=1
	done
	
	# stop server
	# its important the server is stopped before continuing so timestamps are left untouched
	stopMCServer $SERVER_PID
	
	# firstly copy the chunk mtimes from the old mcr files to the new mca ones
	# the format is the same so the php script will just replace bytes 4096-8191 in the mca with the same byte range 
	# from the mcr
	php "$script_folder/copyChunkMtimes.php" "$minecraft_server_folder/world/region" 
	
	# now mv the newly created mca files back out of the conversion folder overwriting any previous
	mv "$minecraft_server_folder/worlds-updates/$mapname/region/"*.mca "$minecraft_server_folder/worlds/$mapname/region/" 
	ln -s "$minecraft_server_folder/worlds-updates/$mapname" "$minecraft_server_folder/world"
	# .. and the new level.dat
	cp "$minecraft_server_folder/worlds-updates/$mapname/level.dat" "$minecraft_server_folder/worlds/$mapname/level.dat"

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
stopMCServer

# do the conversion from pocketmine mcr to anvil as mcoverviewer no longer works with mcr
# these worlds are mine and an example
doCon limbo
doCon lobby
doCon plotworld
doCon world

nice -19 ionice -c 3 overviewer.py --config=$script_folder/overviewer_config.rst &
oPID=$!
cpulimit -l 80 -p $oPID

totalTime=$(echo $(date +%s) - $startTime | bc -l)
echo "Finished map gen at $(date) - took $totalTime seconds"
