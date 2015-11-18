--------------------------------------SETUP--------------------------------------
Step by step instructions:
1. Unzip the .zip file
2. Run the .jar file
3. That's all there is to it!







--------------------------------------SaveConverter--------------------------------------
Step by step instructions:
1. Make sure the "SaveConverter" button is selected. Click it if it isn't.
2. If you can't see the two text boxes, resize (probably expand) the program's window.
3. Select the formats and file locations (hint: expand the list-box and hover your mouse over an option for several seconds to show a tip)
4. Click Run
5. Wait for a while...
6. If you deselected "Copy Minor Files", when it says "All chunks processed", copy the level.dat, level.dat_old, and session.lock files from your old world to your new world.
7. Run Minecraft... Profit!


DETAILS:

There are two dropdown box save format selectors. The top one controls the overall format: "CubicChunks", "YMod", "Scaevolus" (which was called "Vanilla" in previous SaveConverter versions), and "Anvil". The lower one controls specifics, like version or world height.
"CubicChunks" has four versions (see the tooltips for specifics). "Scaevolus" has sizes, to handle Vanilla-Extended-Height Mods, several of which already exist. "128" is the default, as it is the only truly Vanilla size. "McRegion" (or "Scaevolus", depending on the version) also has the option "128 +4096IDs" for converting to Robinton's Standalone 4096IDs Mod save-format.
"YMod" has various sizes. The default size is "512", but "256", "1024", "2048", and "4096" are also supported.
"Anvil" adds support for Jeb's new Anvil format. Its options do little, except set a cut-off point when converting to or from CubicChunks format. It has not been much tested, so please send me any bug reports!

A "PreScaevolus" overall format may be added later.

SaveConverter.jar supports YMod V0.15, and probably also V0.1. YMod V0.19 conversion is untested.

SaveConverter.jar only converts ".region" and ".dat" files and folders. Other files and folders do not need conversion, but must be copied over between directories. The "Copy Misc. Files" option automatically does this if selected.
When converting from YMod to other formats, Minecraft will say the world needs to be updated, then spend about 0 seconds updating. This is because it updates the level.dat file, but all other files have already been updated.

SaveConverter.jar has not been thoroughly tested for converting within a single directory. It has been more thoroughly tested for simultaneously converting and copying between directories.



KNOWN BUGS/PROBLEMS:

Recently converted YMod worlds may move quite slowly for the first several seconds, due to conversion and lighting updates.

Temporary lighting glitches may occur when truncating world height (CC to anything else, and YMod to shorter YMod or Vanilla).







--------------------------------------NBTViewer--------------------------------------
This is mostly a developer's tool. It is built to allow you to open various Minecraft NBT save files, and read their contents. It is very much a work in progress, and it currently only supports ".dat" format files. I'll add McRegion file support soon. Someday, I will probably also add editing features, so you can edit your Minecraft save files however you wish.

Step by step instructions:
1. Make sure the "NBTViewer" button is selected. Click it if it isn't.
2. If you can't see the two text boxes, resize (probably expand) the program's window.
3. Select the file location (the "..." button opens a file chooser).
4. If the list box is not on the correct format, choose the correct format (hint: expand the list-box and hover your mouse over an option for several seconds to show a tip)
5. Click "Load".
6. Click to view NBT Tags; double-click to open.
7. Now you can browse your Minecraft save files' inner workings at your leisure.