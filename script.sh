#!/bin/bash

# requirements before running this script
# install xdotool
# download SaveConverter.jar from http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1273601-1-0-0-robintons-mods
# download mcrcon from http://sourceforge.net/projects/mcrcon/
# change all the paths to your own at the top of this script
# set your dynmap up separatley (set resolutions world names etc )
# also dont forget to set up dynmap so its web folder can be copied and run without a running 
# dynmap server - see https://github.com/webbukkit/dynmap/wiki/Setting-up-without-the-Internal-Web-Server
# dont forget to enable rcon on the minecraft pc server and setup the port
# another tip -
# at the time of writing none of the vnc servers packaged with ubuntu 15 worked with xdotool - I used TurboVNC

pocketmine_folder="/home/minecraft/pocketmine"
minecraft_server_folder="/home/minecraft/pocketmine-pc"
render_complete_map_comparisons_folder="$minecraft_server_folder"/completeworlds
www_map_folder="/home/www/mcpe/map"
minecraft_server_script="nice -n 19 cpulimit -l 50 $minecraft_server_folder/run.sh"
minecraft_server_rcon_port="19140"
minecraft_server_rcon_password="yourpassword"
minecraft_server_generated_www="$minecraft_server_folder/plugins/dynmap/web"
saveconvertor_jar_path="$minecraft_server_folder/SaveConverter.jar"
mcrcon_binary="$minecraft_server_folder/mcrcon"
vnc_server_binary="/opt/TurboVNC/bin/vncserver"
X11_display=":101"

function doCon() {
	mapname="$1"
	echo "Copying map $1"
	rm -R "$minecraft_server_folder/$mapname"
	cp -R "$pocketmine_folder/worlds/$mapname" "$minecraft_server_folder/"
	cd "$minecraft_server_folder/$mapname"
	echo "Opening convertor"
	java -jar "$saveconvertor_jar_path" &
	sleep 4
	PID="$!"
	echo "Convertor started PID $PID"
	#WID=`xdotool search --name "Minecraft Save Conversion Tool" | head -1`
	echo "Starting Conversion"
	sleep 5
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Down; sleep 1
	xdotool key Up; sleep 1
	xdotool key Return; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key Tab; sleep 1
	xdotool key space; sleep 1
	CONV_FINISHED=0
	while [ $CONV_FINISHED -eq 0 ]
        do
		# if the count of mca's >= mcr's CONV_FINSIHED will no longer be 0 (bash math always does integers)
		CONV_FINISHED=$(($(ls -l region/*.mca 2>/dev/null | wc -l)/$(ls -l region/*.mcr 2>/dev/null | wc -l)))
		# check the process is still alive
		if ps -p $PID > /dev/null
		then
			donetext=$(echo "scale=4; ( $(ls -l region/*.mca 2>/dev/null | wc -l)/$(ls -l region/*.mcr 2>/dev/null | wc -l) ) * 100" | bc -l)
			printf "$donetext%% complete  \r"
			sleep 4
		else
			printf "\nERROR Convertor died\n"
			exit
		fi
		# check for timeout
		# get the age in seconds of the newest mca file
		newestFileAge=$(stat -c "%Y %n" -- region/*.mca | sort -rn | awk -v d=$(date +%s) 'NR==1 {print (d-$1), $2; exit}' | cut -d " " -f1)
		if [ "$newestFileAge" -gt "30" ]; 
		then
			printf "\nTimeout - assuming conversion is complete with less mca files than mcr ones\n";
			CONV_FINISHED=1
		fi
	done
	printf "\nFinished Closing Convertor\n"
	#xdotool windowactivate $WID
	kill $PID
}

function pingMCServer() {
	serverping=$($mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password -c "save-off" 2>/dev/null | tr -d '\r' | tr -d '\n')
	server_ok=1
	test "$serverping" = "" && server_ok=0
	echo -n "$server_ok"
}

function startMCServer() {
	echo "Starting minecraft server"
	test $(pingMCServer) = "1" && echo "Server already running"
	test $(pingMCServer) = "1" && return
	nohup $minecraft_server_script &
	#SERVER_PID="$!"
	SERVER_UP=0
	FAILED_ATTEMPTS=0
	while [ $SERVER_UP -eq 0 ]
        do
		if [ $FAILED_ATTEMPTS -eq 40 ]
		then
			echo "fatal - Minecraft server did not start"
			exit
		fi
		sleep 1
		test $(pingMCServer) = "1" && SERVER_UP=1
		FAILED_ATTEMPTS=$(($FAILED_ATTEMPTS+1))
	done
	echo "Startup complete"	
}

function stopMCServer() {
	$mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password -c "stop" 2>/dev/null
}

function doRender() {
	world="$1"
	dynmap_command="$2"
	#echo $dynmap_command
	$mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password -c "$dynmap_command" &>/dev/null
	# TODO get feedback from above command and repeat if it failed!
	finished=1
	server_ok=1
	while [ "$finished" -eq 1 ]
	do
		sleep 10
		serverStatus="$(pingMCServer)"
		test "$serverStatus" = "0" && printf "\nServer has gone down - restarting\n"
		test "$serverStatus" = "0" && startMCServer
		test "$serverStatus" = "0" && $mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password -c "$dynmap_command"
		renderstats=$($mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password  -c "dynmap stats" | grep "Active" | tr -d '\r' | tr -d '\n')
		# if no text after active render jobns then render is complete
		test "$renderstats" = "  Active render jobs: " && finished=2
		if [ "$finished" -eq 1 ]
		then
			RENDER_PROGRESS=$($mcrcon_binary -H localhost -P $minecraft_server_rcon_port -p $minecraft_server_rcon_password -c "dynmap stats" | grep TOTALS)
			#printf "Render in progress $RENDER_PROGRESS   \r"
		fi	
	done
	#printf "\nRender Complete!\n"
}

function snapshot() {
	loaddelay=30000
	worldname="$1"
	mapname="$2"
	outfolder="/home/www/mcpe/maprenders"
	mkdir $outfolder/$worldname 2> /dev/null
	mkdir "$outfolder/$worldname/$mapname" 2> /dev/null
	outfile="$outfolder/$worldname/$mapname/$(date).jpg"
	outfile_x=4000
	outfile_y=4000
	url="https://wattz.org.uk/mcpe/map/index-mini.html?worldname=$worldname&mapname=$mapname&zoom=4&x=55&y=64&z=70"
	echo "Creating Snapshot $worldname $mapname in $outfile"
	echo "xvfb-run --auto-servernum --server-args=\"-screen 0, 4000x4000x24\" cutycapt --url=\"$url\" --out=\"$outfile\" --delay=$loaddelay --min-width=$outfile_x --min-height=$outfile_y"
	xvfb-run --auto-servernum --server-args="-screen 0, 4000x4000x24" cutycapt --url="$url" --out="$outfile" --delay=$loaddelay --min-width=$outfile_x --min-height=$outfile_y
	ls -1trQ "$outfolder/$worldname/$mapname/"*.jpg | xargs cat | ffmpeg  -framerate 5 -i - -vf scale=-1:1080 -y "$outfolder/$worldname/$mapname/video.mp4"
}

function getDynmapRenderCommandForRegionFile() {
	# put filename checks here
	# TODO spaces in folder names will probably make this function sick
	world="$1"
	filename="$2"
	#get region co-ords from filename
	regionx=$(echo "$filename" | cut -d"." -f2)
	regionz=$(echo "$filename" | cut -d"." -f3)
	
	# bit shift region to get chunk co-ords
	chunkx_min=$regionx; let "chunkx_min <<= 5"
	chunkz_min=$regionz; let "chunkz_min <<= 5"
	chunkx_max=$(($regionx + 1)); let "chunkx_max <<= 5"; chunkx_max=$(($chunkx_max - 1));
	chunkz_max=$(($regionz + 1)); let "chunkz_max <<= 5"; chunkz_max=$(($chunkz_max - 1));
	
	# bit shift chunks to get block co-ords
	blockx_min=$chunkx_min; let "blockx_min <<= 4"
	blockz_min=$chunkz_min; let "blockz_min <<= 4"
	blockx_max=$(($chunkx_max + 1)); let "blockx_max <<= 4"; blockx_max=$(($blockx_max - 1));
	blockz_max=$(($chunkz_max + 1)); let "blockz_max <<= 4"; blockz_max=$(($blockz_max - 1));
	
	# get center of region
	rendercenterx=$(( ($blockx_min + $blockx_max) / 2 ))
	rendercenterz=$(( ($blockz_min + $blockz_max) / 2 ))
	
	# radius is always 256 (half of 512 region size) (i think lol)
	renderradius=256
	
	# output the command for dynmap to render the area
	dynmapcommand="dynmap radiusrender $world $rendercenterx $rendercenterz $renderradius"
	echo "$dynmapcommand"
}

function doRenderRegion() {
	world="$1"
	filename="$2"
	currenttotal="$3"
	grandtotal="$4"
	perccomplete=$(echo "scale=2; $currenttotal*100/$grandtotal" | bc)
	printf "Rendering $world $currenttotal/$grandtotal $perccomplete%% complete  \r"
	command=$(getDynmapRenderCommandForRegionFile $1 $2)
	doRender "$world" "$command"
}

function getDynmapUpdateCommands() {
	world=$1
	sourceregiondir="$minecraft_server_folder/$world/region"
	destregiondir="$render_complete_map_comparisons_folder/$world/region"
	echo "$(date) Render for $world Begins"
	# create the comparison folder if not exists
	mkdir $render_complete_map_comparisons_folder 2> /dev/null
	mkdir $render_complete_map_comparisons_folder/$world 2> /dev/null
	mkdir $render_complete_map_comparisons_folder/$world/region 2> /dev/null
	# get totals
	changedregioncount=$(diff -r $sourceregiondir $destregiondir | grep ".mcr" | grep " differ" | cut -d" " -f3 | wc -l)
	newregioncount=$(diff -r $sourceregiondir $destregiondir | grep "Only in $sourceregiondir" | grep ".mcr" | cut -d":" -f2 | cut -d" " -f2 |  wc -l)
	let "grandtotal = $changedregioncount + $newregioncount"
	# for changed regions
	diff -r $sourceregiondir $destregiondir | grep ".mcr" | grep " differ" | cut -d" " -f3 | while read REGION; do let "currenttotal++"; doRenderRegion $world $REGION $currenttotal $grandtotal; done
	# for new regions
	diff -r $sourceregiondir $destregiondir | grep "Only in $sourceregiondir" | grep ".mcr" | cut -d":" -f2 | cut -d" " -f2 |  while read REGION; do let "currenttotal++"; doRenderRegion $world $REGION $currenttotal $grandtotal; done
}

function saveRenderedMap() {
	# TODO HANDLE FOLDERS WITH SPACES IN NAME!
	world=$1
	mkdir $render_complete_map_comparisons_folder 2> /dev/null
	rm -Rf $render_complete_map_comparisons_folder/$world
	cp -R $minecraft_server_folder/$world $render_complete_map_comparisons_folder/
}


echo "Starting map gen at $(date)"

#save snapshot time to web folder
date > "$minecraft_server_generated_www/rendertimes.txt"

#stop server if running
stopMCServer

# do the map copy and conversion
$vnc_server_binary -kill $X11_display &>/dev/null
$vnc_server_binary $X11_display &>/dev/null
export DISPLAY=$X11_display
doCon "world"
doCon "plotworld"
doCon "limbo"
$vnc_server_binary -kill $X11_display &>/dev/null
echo "Done convesion"

# start spigot server
startMCServer

# do renders
sleep 20
getDynmapUpdateCommands "world"
getDynmapUpdateCommands "plotworld"
getDynmapUpdateCommands "limbo"

# end server
stopMCServer

# update plotworld area markers
echo "Updating plotworld area markers"
php $minecraft_server_folder/dynmap_plot_markers.php

# update rendertimes with finished time
date >> "$minecraft_server_generated_www/rendertimes.txt"

# copy to website
echo "Copying map to website folder"
mkdir "$www_map_folder" 2>/dev/null
rm "$minecraft_server_generated_www/index.html" 2>/dev/null
cp -R "$minecraft_server_generated_www/"* "$www_map_folder/"
rm "$www_map_folder/index.html"
touch "$www_map_folder/lastupdatetime"

# create new snapshots
snapshot "world" "SE"
snapshot "world" "NW"
snapshot "plotworld" "SE"
snapshot "plotworld" "NW"

# save the version of the map that created this successful render to use for comparision on next run
echo "Saving sucessfully rendered mc worlds"
saveRenderedMap "world"
saveRenderedMap "plotworld"
saveRenderedMap "limbo"

echo "Completed map gen at $(date)"







