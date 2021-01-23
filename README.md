## Added
- Complete GUI for your AngelChests with configurable buttons etc.
  - GUI can automatically be opened after death
  - GUI can either be automatically opened for the latest chest, or a list of all chests
  - Showing the GUI can be disabled if the player neither has permission for fetching nor teleporting to the chest
- option to preview chest contents in the GUI
- per group prices for fetch / teleport / chest spawning
- configurable amount of xp loss (percentage) per group
- Better config updater
- More control over the holograms
- Literally unbreakable chests - not even /setblock or WorldEdit can destroy chests
- Even when the server crashes or if the process gets killed, AngelChest can remove "dead" (leftover) holograms on the next start
- Support for almost any third party plugin!
- 100% configurable command names / aliases
- Way better performance
  - Async chunk loading when players teleport to their chest
  - Completely new hologram mechanism that is way faster and stops updating holograms in unloaded chunks


## TODO
- Integrate GUI and Preview feature so a player can see contents while having the options to tp, fetch etc the chest on the same screen
- Allow players to run /acinfo, /aclist, /acfetch and /acgui for other players, including offline players
- Make EconomyReason configurable
- Make text in /acgui conigurable
- Option (for OPs, maybe everyone with the protect.ignore permission) to open chests with shift-rightclick without looting them
- Option to show ALL chests by ALL players (for OPs of course)
- Option to instantly save AngelChests 
- XP Percantage: https://minecraft.gamepedia.com/Experience#Leveling_up

## To test
- TEST IF FOLLOWING WORKS: per group prices for fetch / teleport / chest spawning

## Secret features:
- Only equip chestplate automatically if 