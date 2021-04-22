## 3.17.0
- Fixed compatibility with EcoEnchants (idk why but they changed the internal name from "soulbound" to "Soulbound")

## 3.16.0
- Improved chest placement when dying in lava (config option "lava-detection")
- Updated translations

## 3.15.0
- Added option to play custom sound effects when fetching or teleporting to an AngelChest

## 3.14.0
- Added "min-distance" for fetch and teleporting to avoid fetching or teleporting to chests that are already nearby
- Added messages when teleporting to or fetching a chest (can be disabled, see below)
- You can disable all messages sent by AngelChest by setting it to an empty string ("")
- Updated Chinese and Chinese (Traditional) translation

## 3.13.1
- Added description and usage for the /actoggle command
- Improved Debug messages to be better readable
- Improved UpdateChecker

## 3.13.0
- Added command /actoggle so players can disable/enable AngelChest spawning for themselves (Plus version only)
  - Toggling requires permission "angelchest.toggle"
  - They still need the angelchest.use permission to get AngelChests
  - You can decide whether disabling AngelChest for a player also breaks their already existing AngelChests  
  - You can set aliases to directly enable or disable it. Just see the config.yml
- Added Bulgarian translation
- Updated Dutch translation

## 3.12.3
- Added config option to decide whether EliteMobs soulbound should behave like Soulbound items from other plugins (keep on death) or like EliteMobs would handle them (they are dropped on death)
- Fixed Discord verification code not generating properly
- Updated to my new UpdateChecker API
- Added debug command to disable/enable AngelChest handling deaths: /acd disableac|enableac

## 3.12.2
- Fixed WorldGuard integration not working when there are unresolved circular dependencies between AngelChest, WorldGuard and another plugin
- Added new messages from 3.12.0 to the default translations (they are still untranslated though)

## 3.12.1
- Fixed AngelChests automatically unlocking after 1 second when using "unlock-duration: 0" in groups.yml
- Fixed "angelchest-duration: 0" in groups.yml not overriding higher values for infinite chests
- Fixed typo in groups.yml (it's called "random-item-loss", not "item-loss")
- Improved Holograms and fixed "Your AngelChest has been unlocked automatically" message being one second off from the hologram

## 3.12.0
- Added "allow-tp-across-world" and "allow-fetch-across-worlds" option
  - Can also be changed per group
- Added "max-tp-distance" and "max-fetch-distance" (Plus version only)
  - Can also be changed per group
- Added Indonesian translation
- Updated Spanish translation

## 3.11.1
- Fixed exception when player fetches their chest into the void or above the max build height
  - If one of those chests persisted through server restart, it prevented AngelChest from enabling.
  - This is now fixed, you don't have to worry about users abusing fetching.

## 3.11.0
- Added support for ExecutableItems' "Keep on Death" enchantment
- When using invulnerability, the player will no longer be protected from /kill or void damage

## 3.10.0
- Added option to give players invulnerability for X seconds when teleporting to their chests (Plus version only)
- Fixed rare exception in onEnable when using PLAYER_HEAD as material

## 3.9.0
- Added custom WorldGuard flag "allow-angelchest" (default: allow) (Plus version only)
- Improved WorldGuard integration
- Dropped support for WorldGuard older than WorldGuard 7.0.0 (since you are on 1.13+ anyway, you don't care about that)

## 3.8.0
- Added option to display list of AngelChests on Join (default true): "show-location-on-join"

## 3.7.0
- Added option to drop player's head on death
  - Head can always drop or only on PvP deaths
  - Head can be put into the chest, or drop next to it
- Added "angelchest.preview" permission to preview your AngelChests' contents in the GUI
- You can now run /aclist, /acunlock, /actp and /acfetch for other players (requires "angelchest.others" permission)
  - Also works for offline players! :)
- The AngelChests in /aclist will now always be sorted by their creation time. Using /acfetch will no longer change the order of chests
- Added TabCompletion for /aclist, /acunlock, /actp and /acfetch 
- Removed "This feature is only available in AngelChestPlus" from config.yml in the Plus version
- Improved API
  - Added API option to check when a chest has been created
  - Getting AngelChests (either all or by player) will now return a collection sorted by chest creation date/time
- Fixed typos in some messages
- Fixed broken chests being spawned when player has an empty inventory and 0 XP
- Fixed "show-links-on-separate-line" not working when player doesn't have angelchest.tp or angelchest.fetch permission
- Fixed watchdog file not being removed on graceful shutdown
- Improved overall performance by refactoring EVERY single class file

## 3.6.0
- Improved API, many new features for third party plugins!
- Added Hungarian translation (thanks to @Victor75007)
- Improved automatic config updater

## 3.5.4
- Fixed exception when using "/acd dump"
- Fixed "class loading" exception

## 3.5.2
- Fixed plugin not enabling AGAIN because of changed package name (sorry I was testing on Windows which doesn't differentiate between UPPER and lower case directory names)

## 3.5.1
- Fixed plugin not enabling because of changed package name (I was too stupid to update the plugin.yml file)

## 3.5.0
- Added API so other plugins can cancel the AngelChest creation
- Added "random-item-loss" option (Plus version only)
  - You can define an amount of item stacks that will randomly be lost on each death.
  - You can either use a fixed value (e.g. 2 item stacks) or a percentage (e.g. 10% of all item stacks)

## 3.4.0
- Added "/acd blacklist add" command to add the current item to the blacklist
- Fixed GUI showing regular heads instead of custom heads when using PLAYER_HEAD as chest item
- Made all GUI buttons configurable. You can either use normal material names (DIAMOND, CHEST, ...) or a base64 String for custom heads.
- /acversion will now check for updates and includes a link to my discord for support
- Made /acversion and /acdebug command aliases configurable
- Improved overall performance
- Fixed website link in plugin.yml

## 3.3.0
- Changed obfuscation to comply with Spigot guidelines
- Improved dead hologram detection by using NBT Tags
- Added new command: /acd fixholograms (you will never need this command though)

## 3.2.0
- Added "ignore-keep-inventory" option. You can set this to true if you have other plugins that make you keep your inventory on death but would rather get an AngelChest instead.

## 3.1.2
- Fixed exception when using other plugins that set certain drops to AIR instead of properly removing them

## 3.1.1
- Fixed "use-different-material-when-unlocked" not working properly sometimes
- Fixed UpdateChecker showing wrong version

## 3.1.0
- Fixed premium version not being detected when Vault isn't installed
- Added dump command to /acdebug
  - If you have any problems using AngelChest, just run "/acdebug dump".
  - It creates a .zip file containing your config files, latest.log and other useful information
  - You can send this file to me so I can instantly check where the problem is
  - It does NOT automatically upload anything, so don't worry about your privacy :)
  - DO NOT share that zip file to people you don't trust as it contains your latest.log!!!!!!!!!

## 3.0.5
- Fixed chests not being spawned when player dies at Y coordinates below 1
- Fixed watchdog not removing armor stands in void correctly
- Fixed exception regarding UpdateChecker

## 3.0.4
- Using /acreload while having a broken config file will now show a warning to all online OPs
- Updated bStats

## 3.0.3
- Fixed NullPointerException when using some plugin's /kill or /slay commands

## 3.0.2
- Added "discord-verification.html" file for super easy discord verification

## 3.0.1
- Fixed old chests from the Plus version not being compatible with 3.0.0+
- Improved config file

## 3.0.0
- AngelChestPlus and AngelChest (free version) now share the same source code. This means:
  - All the premium features are still only available in AngelChestPlus. It will not change anything for your players.
  - The version has been bumped from 2.X (free version) and 1.X (plus version) to 3.0.0 so they will always share the same version number.
  - The free version will show links to both the free and plus version when a new update is available, the plus version will only show a link to the plus version
- Fixed chest spawning above world's height limit
- Fixed chest spawning at random locations in caves (when dying inside CAVE_AIR) 
- Added config validator that will show a warning in console and to server operators when your config file is broken

Note when upgrading: The file has been renamed from AngelChestPlus.jar to AngelChest.jar. Please remove your old .jar file before updating. You do NOT have to rename your config folder.
Note when upgrading: This version does NO LONGER support Minecraft 1.12!!!

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
