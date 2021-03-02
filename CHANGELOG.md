## 1.5.0
- Added "collect-xp" setting
  - When set to "true", AngelChest collects XP (see "xp-percentage" setting)
  - When set to "false", it doesn't
  - When set to "nopvp", it only collects XP if the death didnt happen during PvP

## 1.4.1
- Fixed "xp-percentage" not working with fractions when not using groups file

## 1.4.0
- Opening the AngelChest after not all items could be stored will now open the regular AngelChest GUI
- Fixed: Opening the GUI will not show any items after not everything could be stored in the player inventory
- Fixed: Using "totem-works-everywhere" reducing offhand slot by one when the totem was not in main or offhand (this actually was a Bukkit/Spigot bug)
- Fixed: Players being able to remove the glass panes from the AngelChest GUI
- Fixed: Confirmation message for prices will no longer be shown if Vault/Economy is not installed
- Fixed: Logging AngelChest transactions now log EVERYTHING

## 1.3.0
- Added "price for opening" the chest
- Added config option to extensively log all AngelChest actions into separate files
  - Currently does NOT log taking out individual items from the overflow inventory
- Recoded many parts for better performance and stability
- Updated API to 1.16.5

## 1.2.2
- Fixed XP percentage not working when set to a fractional value

## 1.2.1
- Fixed rare exception in GUI

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