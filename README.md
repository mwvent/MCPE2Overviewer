# MCPE2Dynmap
WARNING - This is not intended as a drop in script to magically make dynmap work for your MCPE server but as a help for server owners who wish to see how we are doing it on our server

The basic job of the script is to
copy the world(s) from pocketmine to a spigot server folder
use a convertor to change the maps to anvil (unfortunatley the only working tool I can find is a GUI hence the very dodgy method of launching the program in an empty X server and sending kepresses - if anyone has a cli tool plz plz let me know!)
read the differences between this copy and the copy made when the last time the script ran (if it hasnt ran before that will be everything)
Start the spigot server and via rcon get each changed map region updated
Stop the spigot server - copy the updated render onto the (live) webserver folder over the old one
take some snapshots of the rendered map (optional I am making a timelapse video of our server!)
copy the map that has just been rendered into another folder for a comparision to be made when the script runs next time

If you are thinking of doing this youself I would try the process manually first then customise the script. You will most likley end up with a conversion with bodged lighting. Dynmap can be configured to ingore this fortunatley - but you will need to configure dynmap to create a folder that can be dropped in a webserver without needing a running server - plenty of info on the dynmap website! 
