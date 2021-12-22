## Todo

- TODO: Angelchest vanishing animation (falling block going upwards)
- TODO: Teleport delay for /actp
- TODO: Add option to disable mob block damage / disallow mob spawning in graveyards
- TODO: Add option to spawn in graveyard when chest was empty
- TODO. Add option to preserve XP in the chest even there are no items to drop LEL shesh
- TODO: Make GUI behave like normal inventory (let people take out items manually)
- TODO: Fix logging, see experimental branch

## 7.0.0
TODO: FIX: Chests sterben bei Serverneustart, sind nach /acreload wieder da - liegt wohl daran dass die ChestMaterials noch nich geladen sind???

Another new **major** update of AngelChest is here!

- Totally new way to use custom blocks, player heads and blocks from ItemsAdder for your chests! Oraxen will be added too.
  - You can use ANY vanilla blockdata (e.g. red candles with 3 candles), or player heads, or custom textured heads (with base64), or ItemsAdder blocks.
  - You can define different values for locked chests, unlocked chests, and for every graveyard!
  - For example, you can have your graveyards use custom ItemsAdder items, use the player's head for protected chests, and use a custom base64 head for unlocked chests!
  - Support for Oraxen will also be added soon
  - See config.yml (the `material` option) for more information.
  - **Important:** If you already have used custom or player heads before updating to this version, you will have to update that line in your configuration, otherwise you will only get the default head. You can simply use `head:player` instead of where you used `PLAYER_HEAD` before.
- New placeholder to check whether a player disabled AngelChests for themselves: `%angelchest_enabled%`
- A tiny change: The .jar files' name will from now on include the current version number, as demanded by 107 people in the last 2 days.

## 6.2.0
- Added option to prevent placing custom AngelChest items (default: enabled)

## 6.1.1
- Fixed AngelChests spawning below Y=0-radius always using the exact death location instead of looking for a safe spot 

## 6.1.0
- Added option to disable the chest contents GUI by setting "allow-fastlooting" to "force"
- Added option to disable AngelChests spawning for players in creative mode

## 6.0.1
- Fixed AngelChests spawning at about Y=1 when dying below Y=0 in certain situations

## 6.0.0
- Added full support for 1.18

## 5.0.2
- Added option "disable-interacting-with-holograms"

## 5.0.1
- Added some funny example items in items.yml file
  - Crafting is disabled for the default items. To enable it, just change "crafting-enabled" to true for all items you want to be craftabe, then do /acreload 
  - When you already had version 5.0.0 installed, simply remove your items.yml to let AngelChest generate the new file on next /acreload
- You can now specify an amount in /acadmin giveitem <item> [player] [amount]
- Reloading AngelChest will now let players automatically discover all recipes for which you enabled "auto-discover".
- Added items.yml to /acd dump

## 5.0.0

https://youtu.be/_McDGk9DcdM

- Use custom items instead of money (if you like)
  - You can create custom items in the items.yml file. They can be used for having a chest spawned, to open them, teleport to them or fetch them
  - The items can be given to players using /acadmin giveitem, or by crafting them
  - You can use all available crafting types: shaped, shapeless, furnace, blast furnace, campfire, stonecutter, smithing, ...
  - You can also disallow crafting all or only certain items
  - AngelChest items can be set to be kept on death on death. For example, if you use an item to allow players to teleport to their chest, you probably want the players to keep that item when they die
  - Recipes for the custom items can be auto-discovered for players
- Improved performance by getting rid of the InventoryMoveItemEvent (this will improve performance by a good amount for servers with many, many hoppers)
- Updated translations & added Swedish translation

## 4.15.0
- /actoggle can now be run by console or admins for other players. Requires permission angelchest.others
- Removed /acon and /acoff commands. Might be added back at a later time. For now, players can simple use /actoggle as a replacement for both commands.

## 4.14.0
- Readded option "ignore-telekinesis" for servers that have enchantment plugins offering a telekinesis enchantment
  - IMPORTANT: ONLY enable "ignore-telekinesis" when you set your enchantment plugin to NOT work on player deaths! Otherwise you MIGHT be getting duplicated items! More information in config.yml

## 4.13.4
- IMPORTANT: FIXED POSSIBLE DUPLICATION BUG (again - sorry for the trouble. I will not explain how the duplication worked to avoid players being able to abuse it before every admin could update. If you're an admin and have verified your purchase of AngelChestPlus, I will explain it to you in a DM if you wish: mfnalex#0001)
- Made bossbar teleportation message configurable and added translations for Swedish, Turkish, Dutch, Spanish and another language which I already forgot

## 4.13.3
- IMPORTANT: FIXED POSSIBLE DUPLICATION BUG

## 4.13.2
- Fixed a visual bug where the last item you took from the death chest is shown twice in your inventory when it was taken using shift-click (This was NOT a duplication bug, just a VISUAL bug)

## 4.13.1
- Fixed auto-respawn respawning players too fast when they died while another respawn task was already scheduled (because the player manually clicked the "Respawn" button)

## 4.13.0
- Added option to ignore EcoEnchant's Telekinesis enchantment
  - When enabled, an AngelChest will spawn like normal even when you get killed by a player with telekinesis

## 4.12.4
- Fixer rare exception on Airplane when attempting to fastloot a chest with an already full inventory

## 4.12.3
- Fixed death chests not saving experience on shutdown / reload

## 4.12.2
- Fixed logs being generated even when "log-angelchests" is set to false

## 4.12.0
- Players are no longer able to fetch chests into areas where they aren't allowed to place blocks when "only-spawn-chests-if-player-may-build" is set to true

## 5.0.0
- FULL Oraxen support! Use all your custom blocks/models for your death chests!
- You can set different blockdata for every death chest material (locked, unlocked, and per graveyard)
  - In the future, you will be able to choose several material / block types, and on every death, one gets randomly selected

## 4.11.0
- Added option to prevent auto-equipping of armor when fast-looting the chest (paid version only)
  - Can be useful for PvP servers or when you want your players to meet certain requirements to be able to equip some armor pieces

## 4.10.2
- Added {player} placeholder to the main GUI title

## 4.10.1
- Fixed disabled messages still sending the message prefix

## 4.10.0
- Added config option "avoid-lava-oceans"

## 4.9.0
- Added lores for GUI teleport and fetch buttons
- The "gui-info-lore", "gui-teleport", "gui-teleport-lore", "gui-fetch" and "gui-fetch-lore" settings now support the following placeholders:
  - {price}
  - {currency}
  - {balance}

## 4.8.0
- **Added separate permission for /acunlock command**: `angelchest.unlock`
  - Given to all players by default
- **Added proper HexColor support**
  - Hex colors will work in all messages now using the following codes:
    - &#rrggbb
    - <#rrggbb>
  - **You can also use gradients**, for example:
    - <#ff0000>red to green gradient<#/00ff00>
    - <#ff0000>red to <#/00ff00>green to <#/0000ff>blue gradient
- **Emojis from ItemsAdder and PlaceholderAPI placeholders should work in EVERY message and hologram now**
- ~~Added option to disable showing the list of AngelChests on death~~ (This was a thing for years already, my bad) 

## 4.7.1
- Fixed console error in versions 1.16.2 and 1.16.1
- Updated Hungarian translation

## 4.7.0
- Added support for ItemsAdder's emojis to holograms

## 4.6.1
- Added hardcoded limit to "max-radius". It cannot be set higher to 10 anymore, which is also the default value.
  - (one person blamed AngelChest to lag their server after they set this value soooo ridiculous high that it resulted in AngelChest having to check more than 8 million blocks on each death)

## 4.6.0
- Added support for Lands' "Wars" feature (BETA)

## 4.4.2
- FIXED DUPLICATION BUG that existed since 4.4.0

## 4.4.1
- Fixed "cost-teleport" not working when using "tp-wait-time"

## 4.4.0
- Fixed "keep-inventory" not working in disabled worlds
- Added option to prevent dropping chest contents when the chest expires

## 4.3.3
- Fixed helmets and leggings not being movable in the GUI
- Removed the "Black Stained Glass Pane" name from the GUI placeholders

## 4.3.2
- Fixed discord-verification.html file being invalid sometimes
- Removed unused config option

## 4.3.1
- Fixed items being detected as having soulbound when they have "Not Soulbound" in their lore
  - Seriously, EliteMobs, why don't you just remove the lore line instead lol

## 4.3.0
- Added option to ignore enchanted items for the "random-item-loss" option

## 4.2.0
- Added support for ItemsAdder emojis

## 4.1.3
- Fixed Piglin brutes being able to break AngelChests

## 4.1.2
- Fixed "IllegalGroupReference" exception

## 4.1.1
- Fixed exception thrown on /actp and /acfetch in certain cases

## 4.1.0
- Added "tp-wait-time" option
  - You can enter an amount in seconds. Players using /actp will have to stand still for this mount of seconds or they will not be teleported

## 4.0.4
- Fixed Russian translation

## 4.0.3
- Fixed debug message being shown on every BlockBreakEvent - I'm veeeery sorry :( :(

## 4.0.2
- Fixed in free version: Removed message "You are using the config option [...]. This is only available in AngelChestPlus"
- Added another Spanish translation

## 4.0.1
- Added support for EcoEnchants' Telekinesis enchantment
- Removed console message "Could not find a matching grave for player XY, ..."

## 4.0.0

- Added Graveyards!
  - You can define any amount of Graveyards per world and one global "fallback" graveyard.
  - When a player dies in a world with a graveyard, their chest will be put into the nearest graveyard
    - If that graveyard is full, you have the option to send the player to another graveyard in the same world
    - If those are full too, you can define a global graveyard where the chest will spawn
    - If that one is full too, or if you disallowed spawning at other graveyards, you can decide whether the player will drop their inventory or get a chest spawned at their death location like usually
  - You can define certain ground materials for the chests to spawn on
  - Option to define custom totem animations (using the regular totem of undying animation, or custom model data) to play upon respawns per graveyard
  - Graveyards can be defined in a YAML file or using the /acgraveyards command (Permission: angelchest.admin)
  - A tutorial will follow that explains how to create graveyards
- Ability to use custom block data for all chests.
  - For example if you use CANDLE as chest material, you can set the amount of candles and whether they're lit
  - To use this, look at a block with the desired blockdata and enter "/acadmin saveblockdata"
- Fixed exception when player died do to Player#setHealth(0) without having taken damage before
- Improved chest spawn location when player died in lava.

## 3.29.1

- FIXED POSSIBLE DUPLICATION BUG in the Plus version
  - I will explain how the duplication worked after a few days, to give server admins time to update without players abusing it
  - Tip: Dupe bug involves hacked clients
  - (Free version users shouldn't be affected, but they get the update too, just in case)

## 3.29.0

- Made damage causes shown in the hologram customizable

## 3.28.2

- Fixed custom base64 heads not working in 1.17+

## 3.28.1

- Fixed AngelChest not enabling when you removed the playerdata of a player that still had an AngelChest.
    - Once the player has joined again, the chest will be restored on the next server startup / on the next /acreload

## 3.28.0

- Added option to use CustomModelData for the Totem of Undying animation

## 3.27.1

- Fixed Totem animation not working in 1.17

## 3.27.0

- Added config option "allow-fastlooting". When disabled. fast-looting is disabled and players can only open the GUI.

## 3.26.0

- Made chest item name and lore in GUI configurable

## 3.25.1

- Fixed "price" / "price-spawn" not working in groups.yml file
- Added Pirate Speak translation
- Updated Spanish translation

## 3.25.0

- Added two new hologram placeholders:
    - {items} shows the number of items inside the chest
    - {xp} shows the amount of XP the chest stores
- Players killed by an End crystal that was shot by another player will be treated as if the other player killed them

## 3.24.0

- Added "protected.yml" file to control who should be able to open protected chests (owner-outside-pvp, owner-in-pvp,
  killer, others, or certain groups)

## 3.23.0

- Added "gui-requires-shift". Set to false to open the GUI when rightclicking a chest without using shift.
- Fixed AngelChest owners being able to equip armor held in their hand to the hologram when right-clicking on the
  hologram

## 3.22.0

- Fixed players being able to break the chest if they don't have enough money to open it

## 3.21.0

- Added "play-totem-animation" option that will show the Totem of Undying animation when a users dies and gets an
  AngelChest spawned (disabled by default) (Premium version only)

## 3.20.0

- Added support for enchanted books containing EcoEnchants' Soulbound enchantment. Actually enchanted books only store
  enchantments, but EcoEnchants treats them as being enchanted itself.

## 3.19.0

- Added option to blacklist items by enchantment (see blacklist.yml)

## 3.18.0

- Added config option "minimum-air-above-chest" (default 0)

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

- Added config option to decide whether EliteMobs soulbound should behave like Soulbound items from other plugins (keep
  on death) or like EliteMobs would handle them (they are dropped on death)
- Fixed Discord verification code not generating properly
- Updated to my new UpdateChecker API
- Added debug command to disable/enable AngelChest handling deaths: /acd disableac|enableac

## 3.12.2

- Fixed WorldGuard integration not working when there are unresolved circular dependencies between AngelChest,
  WorldGuard and another plugin
- Added new messages from 3.12.0 to the default translations (they are still untranslated though)

## 3.12.1

- Fixed AngelChests automatically unlocking after 1 second when using "unlock-duration: 0" in groups.yml
- Fixed "angelchest-duration: 0" in groups.yml not overriding higher values for infinite chests
- Fixed typo in groups.yml (it's called "random-item-loss", not "item-loss")
- Improved Holograms and fixed "Your AngelChest has been unlocked automatically" message being one second off from the
  hologram

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
- The AngelChests in /aclist will now always be sorted by their creation time. Using /acfetch will no longer change the
  order of chests
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

- Fixed plugin not enabling AGAIN because of changed package name (sorry I was testing on Windows which doesn't
  differentiate between UPPER and lower case directory names)

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
- Made all GUI buttons configurable. You can either use normal material names (DIAMOND, CHEST, ...) or a base64 String
  for custom heads.
- /acversion will now check for updates and includes a link to my discord for support
- Made /acversion and /acdebug command aliases configurable
- Improved overall performance
- Fixed website link in plugin.yml

## 3.3.0

- Changed obfuscation to comply with Spigot guidelines
- Improved dead hologram detection by using NBT Tags
- Added new command: /acd fixholograms (you will never need this command though)

## 3.2.0

- Added "ignore-keep-inventory" option. You can set this to true if you have other plugins that make you keep your
  inventory on death but would rather get an AngelChest instead.

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
    - The version has been bumped from 2.X (free version) and 1.X (plus version) to 3.0.0 so they will always share the
      same version number.
    - The free version will show links to both the free and plus version when a new update is available, the plus
      version will only show a link to the plus version
- Fixed chest spawning above world's height limit
- Fixed chest spawning at random locations in caves (when dying inside CAVE_AIR)
- Added config validator that will show a warning in console and to server operators when your config file is broken

Note when upgrading: The file has been renamed from AngelChestPlus.jar to AngelChest.jar. Please remove your old .jar
file before updating. You do NOT have to rename your config folder. Note when upgrading: This version does NO LONGER
support Minecraft 1.12!!!

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
    - You can either match name and/or lore exactly, or partly. You can also set individually for each item whether
      color codes should be checked or ignored.
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

- Fixed players being able to duplicate player heads (if you set this as your chest material) by putting water "inside"
  the head block

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
- Fixed: Using "totem-works-everywhere" reducing offhand slot by one when the totem was not in main or offhand (this
  actually was a Bukkit/Spigot bug)
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
