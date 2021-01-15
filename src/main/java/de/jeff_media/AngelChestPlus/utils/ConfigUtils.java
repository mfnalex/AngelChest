package de.jeff_media.AngelChestPlus.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.jeff_media.AngelChestPlus.*;
import de.jeff_media.AngelChestPlus.hooks.MinepacksHook;
import org.bukkit.Material;

public class ConfigUtils {
	
	static void createDirectory(Main main, String name) {

		File folder = new File(main.getDataFolder().getPath() + File.separator + name);
		if (!folder.getAbsoluteFile().exists()) {
			folder.mkdirs();
		}
	}

	static void createDirectories(Main main) {
		createDirectory(main, "angelchests");
		createDirectory(main, "persistent");
	}

	public static void reloadCompleteConfig(Main main,boolean reload) {
		if(reload) {
			main.saveAllAngelChestsToFile();
		}
		main.reloadConfig();
		createConfig(main);
		ConfigUpdater.updateConfig(main);
		main.initUpdateChecker();
		main.debug = main.getConfig().getBoolean(Config.DEBUG,false);
		main.verbose = main.getConfig().getBoolean(Config.VERBOSE,false);
		main.messages = new Messages(main);
		main.pendingConfirms = new HashMap<>();
		File groupsFile = new File(main.getDataFolder()+File.separator+"groups.yml");
		main.groupUtils = new GroupUtils(main,groupsFile);
		main.worldGuardHandler = new WorldGuardHandler(main);
		main.hookUtils = new HookUtils(main);
		main.minepacksHook = new MinepacksHook();
		//main.debugger = new AngelChestDebugger(main);
		if(reload) {
			main.loadAllAngelChestsFromFile();
		}

	}

	
	static void createConfig(Main main) {

		ConfigUpdater.migrateFromFreeVersion(main);

		main.saveDefaultConfig();
		main.saveResource("groups.example.yml", true);
		createDirectories(main);

		main.getConfig().addDefault(Config.CHECK_FOR_UPDATES, "true");
		main.getConfig().addDefault(Config.DETECT_PLAYER_HEAD_DROPS,false);
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
		main.getConfig().addDefault(Config.EVENT_PRIORITY,"NORMAL");
		main.getConfig().addDefault(Config.HEAD_USES_PLAYER_NAME,true);
		main.getConfig().addDefault(Config.AUTO_RESPAWN,false);
		main.getConfig().addDefault(Config.AUTO_RESPAWN_DELAY,10);
		main.getConfig().addDefault("play-can-skip-auto-respawn",false);
		main.getConfig().addDefault(Config.USE_SLIMEFUN,true);
		main.getConfig().addDefault(Config.CHECK_GENERIC_SOULBOUND,true);
		main.getConfig().addDefault(Config.SHOW_LINKS_ON_SEPARATE_LINE,false);
		main.getConfig().addDefault(Config.CONFIRM,true);
		main.getConfig().addDefault(Config.PRICE,0.0d);
		main.getConfig().addDefault(Config.VOID_DETECTION,true);
		main.getConfig().addDefault(Config.REFUND_EXPIRED_CHESTS,true);
		main.getConfig().addDefault(Config.PRICE_TELEPORT,0.0d);
		main.getConfig().addDefault(Config.PRICE_FETCH,0.0d);
		main.getConfig().addDefault(Config.CONSOLE_MESSAGE_ON_OPEN,true);
		main.getConfig().addDefault("tp-distance",2);
		main.getConfig().addDefault("full-xp", false); // Currently not in config because there is no way to get players XP
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
