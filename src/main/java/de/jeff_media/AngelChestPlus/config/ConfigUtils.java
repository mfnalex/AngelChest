package de.jeff_media.AngelChestPlus.config;

import java.io.File;
import java.util.*;

import de.jeff_media.AngelChestPlus.*;
import de.jeff_media.AngelChestPlus.gui.GUIManager;
import de.jeff_media.AngelChestPlus.hooks.MinepacksHook;
import de.jeff_media.AngelChestPlus.hooks.WorldGuardHandler;
import de.jeff_media.AngelChestPlus.utils.GroupUtils;
import de.jeff_media.AngelChestPlus.utils.HookUtils;
import org.bukkit.Material;

/**
 * Creates the default config and directories, handles reloading and adds the default values
 */
public class ConfigUtils {

	@SuppressWarnings("SameParameterValue")
	static void createDirectory(String name) {
		File folder = new File(Main.getInstance().getDataFolder().getPath() + File.separator + name);
		if (!folder.getAbsoluteFile().exists()) {
			folder.mkdirs();
		}
	}

	static void createDirectories() {
		createDirectory("angelchests");
		createDirectory("logs");
	}

	public static void reloadCompleteConfig(boolean reload) {
		Main main = Main.getInstance();
		if(reload) {
			main.saveAllAngelChestsToFile();
		}
		main.reloadConfig();
		createConfig();
		ConfigUpdater.updateConfig();
		main.initUpdateChecker();
		main.debug = main.getConfig().getBoolean(Config.DEBUG,false);
		main.verbose = main.getConfig().getBoolean(Config.VERBOSE,false);
		main.messages = new Messages(main);
		main.pendingConfirms = new HashMap<>();
		File groupsFile = new File(main.getDataFolder()+File.separator+"groups.yml");
		main.groupUtils = new GroupUtils(groupsFile);
		main.worldGuardHandler = new WorldGuardHandler(main);
		main.hookUtils = new HookUtils(main);
		main.minepacksHook = new MinepacksHook();
		main.guiManager = new GUIManager(main);
		//main.debugger = new AngelChestDebugger(main);
		if(reload) {
			main.loadAllAngelChestsFromFile();
		}

	}

	
	static void createConfig() {

		Main main = Main.getInstance();
		ConfigUpdater.migrateFromFreeVersion();

		main.saveDefaultConfig();
		main.saveResource("groups.example.yml", true);
		createDirectories();

		main.getConfig().addDefault(Config.CHECK_FOR_UPDATES, "true");
		//main.getConfig().addDefault(Config.DETECT_PLAYER_HEAD_DROPS,false);
		main.getConfig().addDefault(Config.CHECK_FOR_UPDATES_INTERVAL,4);
		main.getConfig().addDefault(Config.ALLOW_ANGELCHEST_IN_PVP,true);
		main.getConfig().addDefault(Config.TOTEM_OF_UNDYING_WORKS_EVERYWHERE,true);
		main.getConfig().addDefault(Config.SHOW_LOCATION, true);
		main.getConfig().addDefault(Config.ANGELCHEST_DURATION, 600);
		main.getConfig().addDefault(Config.MAX_ALLOWED_ANGELCHESTS,5);
		main.getConfig().addDefault(Config.HOLOGRAM_OFFSET,0.0);
		main.getConfig().addDefault(Config.HOLOGRAM_OFFSET_PER_LINE,0.25d);
		main.getConfig().addDefault(Config.MAX_RADIUS, 10);
		main.getConfig().addDefault(Config.MATERIAL, "CHEST");
		main.getConfig().addDefault("player-head","{PLAYER}");
		main.getConfig().addDefault("preserve-xp", true);
		main.getConfig().addDefault(Config.REMOVE_CURSE_OF_VANISHING,true);
		main.getConfig().addDefault(Config.REMOVE_CURSE_OF_BINDING,true);
		main.getConfig().addDefault(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD,false);
		main.getConfig().addDefault(Config.DISABLE_WORLDGUARD_INTEGRATION,false);
		//main.getConfig().addDefault("ignore-keep-inventory",false);
		main.getConfig().addDefault(Config.EVENT_PRIORITY,"HIGHEST");
		main.getConfig().addDefault(Config.HEAD_USES_PLAYER_NAME,true);
		main.getConfig().addDefault(Config.AUTO_RESPAWN,false);
		main.getConfig().addDefault(Config.AUTO_RESPAWN_DELAY,10);
		main.getConfig().addDefault("play-can-skip-auto-respawn",false);
		main.getConfig().addDefault(Config.USE_SLIMEFUN,true);
		main.getConfig().addDefault(Config.CHECK_GENERIC_SOULBOUND,true);
		main.getConfig().addDefault(Config.SHOW_LINKS_ON_SEPARATE_LINE,false);
		main.getConfig().addDefault(Config.CONFIRM,true);
		main.getConfig().addDefault(Config.PRICE,0.0d);
		main.getConfig().addDefault(Config.PRICE_OPEN,0.0d);
		main.getConfig().addDefault(Config.VOID_DETECTION,true);
		main.getConfig().addDefault(Config.REFUND_EXPIRED_CHESTS,true);
		main.getConfig().addDefault(Config.PRICE_TELEPORT,0.0d);
		main.getConfig().addDefault(Config.PRICE_FETCH,0.0d);
		main.getConfig().addDefault(Config.CONSOLE_MESSAGE_ON_OPEN,true);
		main.getConfig().addDefault(Config.ASYNC_CHUNK_LOADING, true);
		main.getConfig().addDefault(Config.GUI_BUTTON_BACK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0=");
		main.getConfig().addDefault(Config.GUI_BUTTON_TELEPORT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=");
		main.getConfig().addDefault(Config.GUI_BUTTON_FETCH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=");
		main.getConfig().addDefault(Config.GUI_BUTTON_UNLOCK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19");
		main.getConfig().addDefault(Config.GUI_BUTTON_CONFIRM_INFO, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZlNTIyZDkxODI1MjE0OWU2ZWRlMmVkZjNmZTBmMmMyYzU4ZmVlNmFjMTFjYjg4YzYxNzIwNzIxOGFlNDU5NSJ9fX0=");
		main.getConfig().addDefault(Config.GUI_BUTTON_CONFIRM_ACCEPT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UyYTUzMGY0MjcyNmZhN2EzMWVmYWI4ZTQzZGFkZWUxODg5MzdjZjgyNGFmODhlYThlNGM5M2E0OWM1NzI5NCJ9fX0=");
		main.getConfig().addDefault(Config.GUI_BUTTON_CONFIRM_DECLINE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTljZGI5YWYzOGNmNDFkYWE1M2JjOGNkYTc2NjVjNTA5NjMyZDE0ZTY3OGYwZjE5ZjI2M2Y0NmU1NDFkOGEzMCJ9fX0=");
		main.getConfig().addDefault(Config.ALIAS_ACGUI, Arrays.asList("ac","angelchest","angelchests","angelchestgui"));
		main.getConfig().addDefault(Config.ALIAS_ACLIST, Arrays.asList("acinfo","angelchestinfo","angelchestlist"));
		main.getConfig().addDefault(Config.ALIAS_ACFETCH, Arrays.asList("acretrieve","angelchestretrieve","angelchestfetch"));
		main.getConfig().addDefault(Config.ALIAS_ACTP, Arrays.asList("acteleport","angelchesttp","angelchestteleport"));
		main.getConfig().addDefault(Config.ALIAS_ACUNLOCK, Arrays.asList("angelchestunlock","unlockchest","unlock"));
		main.getConfig().addDefault(Config.ALIAS_ACRELOAD, Collections.singletonList("angelchestreload"));
		main.getConfig().addDefault(Config.SHOW_GUI_AFTER_DEATH, "false");
		main.getConfig().addDefault(Config.ONLY_SHOW_GUI_AFTER_DEATH_IF_PLAYER_CAN_TP_OR_FETCH, true);
		main.getConfig().addDefault("tp-distance",2);
		main.getConfig().addDefault("full-xp", false); // Currently not in config because there is no way to get players XP
		main.getConfig().addDefault(Config.DONT_PROTECT_CHEST_IF_PLAYER_DIED_IN_PVP,false);
		main.getConfig().addDefault(Config.ALLOW_CHEST_IN_LAVA,true);
		main.getConfig().addDefault(Config.ALLOW_CHEST_IN_VOID,true);
		main.getConfig().addDefault(Config.LOG_ANGELCHESTS, false);
		main.getConfig().addDefault(Config.LOG_FILENAME,"{player}_{world}_{date}.log");
		main.getConfig().addDefault(Config.CHEST_FILENAME,"{player}_{world}_{x}_{y}_{z}.yml");
		main.disabledMaterials = main.getConfig().getStringList(Config.DISABLED_MATERIALS);
		main.disabledWorlds =  main.getConfig().getStringList(Config.DISABLED_WORLDS);
		main.disabledRegions =  main.getConfig().getStringList(Config.DISABLED_WORLDGUARD_REGIONS);
		
		List<String> dontSpawnOnTmp = main.getConfig().getStringList(Config.DONT_SPAWN_ON);
		main.dontSpawnOn = new ArrayList<>();
		
		List<String> onlySpawnInTmp =  main.getConfig().getStringList(Config.ONLY_SPAWN_IN);
		main.onlySpawnIn = new ArrayList<>();
		
		for(String string : dontSpawnOnTmp) {
			Material mat = Material.getMaterial(string.toUpperCase());
			if(mat==null) {			
				main.getLogger().warning(String.format("Invalid Material while parsing %s: %s", string,Config.DONT_SPAWN_ON));
				continue;
			}
			if(!mat.isBlock()) {
				main.getLogger().warning(String.format("Invalid Block while parsing %s: %s", string, Config.DONT_SPAWN_ON));
				continue;
			}
			//System.out.println(mat.name() + " added to blacklist");
			main.dontSpawnOn.add(mat);
		}

		if(false) return;
		
		for(String string : onlySpawnInTmp) {
			Material mat = Material.getMaterial(string.toUpperCase());
			if(mat==null) {
				main.getLogger().warning(String.format("Invalid Material while parsing %s: %s", string,Config.ONLY_SPAWN_IN));
				continue;
			}
			if(!mat.isBlock()) {
				main.getLogger().warning(String.format("Invalid Block while parsing %s: %s", string, Config.ONLY_SPAWN_IN));
				continue;
			}
			//System.out.println(mat.name() + " added to whitelist");
			main.onlySpawnIn.add(mat);
		}

		
		if(Material.getMaterial(main.getConfig().getString(Config.MATERIAL).toUpperCase())==null) {
			main.getLogger().warning("Invalid Material: "+main.getConfig().getString(Config.MATERIAL)+" - falling back to CHEST");
			main.chestMaterial = Material.CHEST;
		} else {
			main.chestMaterial = Material.getMaterial(main.getConfig().getString(Config.MATERIAL).toUpperCase());
			if(!main.chestMaterial.isBlock()) {
				main.getLogger().warning("Not a block: "+main.getConfig().getString(Config.MATERIAL)+" - falling back to CHEST");
				main.chestMaterial = Material.CHEST;
			}
		}

	}
	
	private static void showOldConfigWarning(Main main) {
		main.getLogger().warning("==============================================");
		main.getLogger().warning("You were using an old config file. AngelChest");
		main.getLogger().warning("has updated the file to the newest version.");
		main.getLogger().warning("Your changes have been kept.");
		main.getLogger().warning("==============================================");
	}

}
