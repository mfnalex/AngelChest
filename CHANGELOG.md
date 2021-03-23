## 3.0.0-SNAPSHOT
- AngelChestPlus and AngelChest (free version) now share the same source code. This means:
  - All the premium features are still only available in AngelChestPlus. It will not change anything for your players.
  - The version has been bumped from 2.X (free version) and 1.X (plus version) to 3.0.0 so they will always share the same version number.
  - The free version will show links to both the free and plus version when a new update is available, the plus version will only show a link to the plus version
- Fixed chest spawning above world's height limit
- Fixed chest spawning at random locations in caves (when dying inside CAVE_AIR) 
- Added config validator that will show a warning in console and to server operators when your config file is broken

Note when upgrading: The file has been renamed from AngelChestPlus.jar to AngelChest.jar. Please remove your old .jar file before updating. You do NOT have to rename your config folder.
Note when upgrading: This version does NO LONGER support Minecraft 1.12!!!

## 1.12.4


## 1.12.3
- Fixed some messages being shown twice in some server implementations under certain circumstances
- Removed "Loading AngelChest xyz.yml" on startup. This will now only be shown when debug mode is enabled

## 1.12.2
- Fixed GUI not being closed/updated when a player empties the same AngelChest using right-click
- Fixed GUI sometimes not allowing items to be placed in the player's inventory until clicked twice
- Fixed GUI preventing players from dragging items inside the player's inventory while having the chest opened

## 1.12.1
- Improved /acd command for debugging blacklist issues
- Fixed typos in config.yml and blacklist.example.yml

## 1.12.0
- Added item blacklist. Blacklisted items will behave just like they would normally do on death.
  - You can blacklist items by lore, material and/or name.
  - To see the exact lore/name of a custom item, or see its blacklist status, use /acd blacklist
  - You can either match name and/or lore exactly, or partly. You can also set individually for each item whether color codes should be checked or ignored.
  - The blacklist is a separate file. See blacklist.example.yml for more information

## 1.11.2
- Improved behaviour when loading AngelChests inside unloaded / removed worlds
- Added debug command "/acd" (Permission: angelchest.debug) to enable/disable mode via command and more

## 1.11.1
- Fixed minor console warning

## 1.11.0
- Added option to set prices based on player's bank account balance.
  - Works for all prices (fetch, spawn, open, teleport)
  - E.g. setting "price-teleport" to "0.1p" means it costs 10% of the player's total account balance
- Added chest spawn chance (normally set to 1.0 = 100%)
  - Can also be configured per group
  - When the chance does not succeed, no chest will be spawned, and the items drop normally

## 1.10.2
- Updated Polish and Spanish translations

## 1.10.1
- Added option to automatically add a prefix to all chat messages

## 1.10.0
- Added two new messages to show to AngelChest owners when other people open or empty their chests
  - Can be disabled in the config
- Fixed players not having to pay to open an AngelChest when doing so by clicking the hologram

## 1.9.0
- Added option to use different materials for protected and unprotected/unlocked chests

## 1.8.1
- Fixed UpdateChecker pointing to the outdated free version

## 1.8.0
- Added option to show LOCKED/LOCKED for X minutes/UNLOCKED status to the chest hologram (placeholder: "{protected}")
- Fixed: setting unlock-duration to 0 automatically unlocked the chest instea of never unlocking it
- Improved French translation

## 1.7.2
- Fixed exception when using buckets

## 1.7.1
- Added new message to translations
- Updated German and Turkish translations

## 1.7.0
- Added option to automatically unlock chests after a specific amount of time.
  - Default is 0 = never automatically unlock
  - Configurable per group in groups.yml, or globally in the config.yml
- Fixed player heads being shown as "regular" head to the player after he tried to break the head using a water bucket
- Fixed exception when taking last item from AngelChest through the GUI
- Fixed AngelChest not despawning when taking last item from AngelChest through the GUI

## 1.6.2
- Fixed players being able to duplicate player heads (if you set this as your chest material) by putting water "inside" the head block

## 1.6.1
- Added option to remove old AngelChest log files after X hours

## 1.6.0
- Added option to show the cause of the death (e.g. playername, SKELETON or SUFFOCATION) in the hologram
  - Just add {deathcause} to your hologram.
  - New Hologram default text: `"&a&l[AngelChest]&r\n&b{player}\n&6{time}\n&cKilled by {deathcause}"`

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
