## 1.2.0
- Added config options:
  - allow-chest-in-lava
  - allow-chest-in-void

## 1.1.4
- Made GUI 100% translatable

## 1.1.3
- Added config option: "dont-protect-chest-if-player-died-in-pvp"

## 1.1.2
- Improved TPing to the Chest
- Fixed hoppers not working (wtf sorry for that, I am embarrassed)

## 1.1.1
- Fixed issue with the update checker

## 1.1.0
- Added support for EliteMob's soulbound mechanism
- Fixed exception in InventoryClickEvent
- Prevent player spawning on top of bedrock in the nether when player died near the ceiling
- Prevent Confirm menu in GUI showing up if price is set to 0
- Fixed BACK button not working in Chest Preview GUI
- Added number of XP to the XP bottle's name in the Chest Preview GUI
- detect ALL 3rd party plugin's additional death drops (such as player heads etc.)
- 100 % compatible with vanilla datapacks (VanillaTweaks etc.)
- configurable amount of xp loss (percentage) per group
- option to open a chest with shift+rightclick to only take certain items instead of looting everything
- Added preview feature to the GUI
- Configurable prices per player (might be bugged, needs testing)
- GUI can automatically be opened after death
  - GUI can either be automatically opened for the latest chest, or a list of all chests
  - Showing the GUI can be disabled if the player neither has permission for fetching nor teleporting to the chest
- Fixed /acunlock command and unlocking through GUI
- Only show buttons in GUI that the player has permission to
- Configurable command aliases