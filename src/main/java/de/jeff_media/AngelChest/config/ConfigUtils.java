package de.jeff_media.AngelChest.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import de.jeff_media.AngelChest.*;
import de.jeff_media.AngelChest.data.BlacklistEntry;
import de.jeff_media.AngelChest.gui.GUIManager;
import de.jeff_media.AngelChest.hooks.MinepacksHook;
import de.jeff_media.AngelChest.hooks.WorldGuardHandler;
import de.jeff_media.AngelChest.utils.GroupUtils;
import de.jeff_media.AngelChest.utils.HookUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.sqlite.util.StringUtils;

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

	public static void validateConfigFiles() {
		Main.getInstance().invalidConfigFiles = getBrokenConfigFiles();
		EmergencyMode.warnBrokenConfig();
	}

	public static @Nullable String[] getBrokenConfigFiles() {
		ArrayList<String> files = new ArrayList<>();
		for(String fileName : new String[] { "config.yml", "blacklist.yml", "groups.yml"}) {
			Main.getInstance().debug("Checking if file is broken: "+fileName);
			File file = new File(Main.getInstance().getDataFolder(), fileName);

			if (!file.exists()) continue;

			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(file);
				Main.getInstance().debug("- Valid file: "+ fileName);
			} catch (FileNotFoundException e) {
				Main.getInstance().debug("- Missing file: "+fileName);
				continue;
			} catch (InvalidConfigurationException | IOException e) {
				files.add(fileName);
				Main.getInstance().debug("- Broken file: "+fileName);
			}
		}
		return files.size()==0 ? null : files.toArray(new String[0]);
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
		main.hookUtils = new HookUtils();
		main.minepacksHook = new MinepacksHook();
		main.guiManager = new GUIManager();
		main.itemBlacklist = loadItemBlacklist();
		//main.debugger = new AngelChestDebugger(main);
		if(reload) {
			main.loadAllAngelChestsFromFile();
		}
		ConfigUtils.validateConfigFiles();
	}

	private static Map<String, BlacklistEntry> loadItemBlacklist() {
		Main main = Main.getInstance();
		Map<String, BlacklistEntry> set = new HashMap<>();
		File yamlFile = new File(main.getDataFolder()+File.separator+"blacklist.yml");
		if(!yamlFile.exists()) {
			main.getLogger().info("blacklist.yml does not exist, disabling item blacklist.");
			return set;
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
		for(String node : yaml.getKeys(false)) {
			set.put(node.toLowerCase(),new BlacklistEntry(node,yaml));
		}
		return set;
	}

	static void metric(String name, String value) {
		Main.getInstance().metrics.addCustomChart(new Metrics.SimplePie(name.replace('-','_').toLowerCase(), () -> value));
	}

	static void metric(String name) {
		metric(name, Main.getInstance().getConfig().getString(name));
	}

	static void metric(String name, List<String> values) {
		Collections.sort(values);
		String value = StringUtils.join(values, ",");
		metric(name,value);
	}


	static void createConfig() {

		Main main = Main.getInstance();
		FileConfiguration conf = main.getConfig();

		metric("using_plus_version", String.valueOf(main.premium()));

		main.saveDefaultConfig();
		main.saveResource("groups.example.yml", true);
		main.saveResource("blacklist.example.yml", true);
		createDirectories();

		conf.addDefault(Config.XP_PERCENTAGE, -1);
		metric(Config.XP_PERCENTAGE);

		conf.addDefault(Config.SPAWN_CHANCE, 1.0);
		metric(Config.SPAWN_CHANCE);

		conf.addDefault(Config.CHECK_FOR_UPDATES, "true");
		metric(Config.CHECK_FOR_UPDATES);

		conf.addDefault(Config.CHECK_FOR_UPDATES_INTERVAL,4);
		metric(Config.CHECK_FOR_UPDATES_INTERVAL);

		conf.addDefault(Config.ALLOW_ANGELCHEST_IN_PVP,true);
		metric(Config.ALLOW_ANGELCHEST_IN_PVP);

		conf.addDefault(Config.TOTEM_OF_UNDYING_WORKS_EVERYWHERE,true);
		metric(Config.TOTEM_OF_UNDYING_WORKS_EVERYWHERE);

		conf.addDefault(Config.SHOW_LOCATION, true);
		metric(Config.SHOW_LOCATION);

		conf.addDefault(Config.ANGELCHEST_DURATION, 600);
		metric(Config.ANGELCHEST_DURATION);

		conf.addDefault(Config.MAX_ALLOWED_ANGELCHESTS,5);
		metric(Config.MAX_ALLOWED_ANGELCHESTS);

		conf.addDefault(Config.HOLOGRAM_OFFSET,0.0);
		metric(Config.HOLOGRAM_OFFSET);

		conf.addDefault(Config.HOLOGRAM_OFFSET_PER_LINE,0.25d);
		metric(Config.HOLOGRAM_OFFSET_PER_LINE);

		conf.addDefault(Config.MAX_RADIUS, 10);
		metric(Config.MAX_RADIUS);

		conf.addDefault(Config.MATERIAL, "CHEST");
		metric(Config.MATERIAL);

		conf.addDefault(Config.MATERIAL_UNLOCKED,"ENDER_CHEST");
		metric(Config.MATERIAL_UNLOCKED);

		conf.addDefault(Config.REMOVE_CURSE_OF_VANISHING,true);
		metric(Config.REMOVE_CURSE_OF_VANISHING);

		conf.addDefault(Config.REMOVE_CURSE_OF_BINDING,true);
		metric(Config.REMOVE_CURSE_OF_BINDING);

		conf.addDefault(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD,false);
		metric(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD);

		conf.addDefault(Config.DISABLE_WORLDGUARD_INTEGRATION,false);
		metric(Config.DISABLE_WORLDGUARD_INTEGRATION);

		conf.addDefault(Config.EVENT_PRIORITY,"HIGHEST");
		metric(Config.EVENT_PRIORITY);

		conf.addDefault(Config.HEAD_USES_PLAYER_NAME,true);
		metric(Config.HEAD_USES_PLAYER_NAME);

		conf.addDefault(Config.AUTO_RESPAWN,false);
		metric(Config.AUTO_RESPAWN);

		conf.addDefault(Config.AUTO_RESPAWN_DELAY,10);
		metric(Config.AUTO_RESPAWN_DELAY);

		conf.addDefault(Config.USE_SLIMEFUN,true);
		metric(Config.USE_SLIMEFUN);

		conf.addDefault(Config.CHECK_GENERIC_SOULBOUND,true);
		metric(Config.CHECK_GENERIC_SOULBOUND);

		conf.addDefault(Config.SHOW_LINKS_ON_SEPARATE_LINE,false);
		metric(Config.SHOW_LINKS_ON_SEPARATE_LINE);

		conf.addDefault(Config.CONFIRM,true);
		metric(Config.CONFIRM);

		conf.addDefault(Config.PRICE,0.0d);
		metric(Config.PRICE);

		conf.addDefault(Config.PRICE_OPEN,0.0d);
		metric(Config.PRICE_OPEN);

		conf.addDefault(Config.PRICE_TELEPORT,0.0d);
		metric(Config.PRICE_TELEPORT);

		conf.addDefault(Config.PRICE_FETCH,0.0d);
		metric(Config.PRICE_FETCH);


		conf.addDefault(Config.VOID_DETECTION,true);
		metric(Config.VOID_DETECTION);

		conf.addDefault(Config.REFUND_EXPIRED_CHESTS,true);
		metric(Config.REFUND_EXPIRED_CHESTS);

		conf.addDefault(Config.ASYNC_CHUNK_LOADING, true);
		metric(Config.ASYNC_CHUNK_LOADING);

		conf.addDefault(Config.SHOW_GUI_AFTER_DEATH, "false");
		metric(Config.SHOW_GUI_AFTER_DEATH);

		conf.addDefault(Config.ONLY_SHOW_GUI_AFTER_DEATH_IF_PLAYER_CAN_TP_OR_FETCH, true);
		metric(Config.ONLY_SHOW_GUI_AFTER_DEATH_IF_PLAYER_CAN_TP_OR_FETCH);

		conf.addDefault(Config.DONT_PROTECT_CHEST_IF_PLAYER_DIED_IN_PVP,false);
		metric(Config.DONT_PROTECT_CHEST_IF_PLAYER_DIED_IN_PVP);

		conf.addDefault(Config.ALLOW_CHEST_IN_LAVA,true);
		metric(Config.ALLOW_CHEST_IN_LAVA);

		conf.addDefault(Config.ALLOW_CHEST_IN_VOID,true);
		metric(Config.ALLOW_CHEST_IN_VOID);

		conf.addDefault(Config.LOG_ANGELCHESTS, false);
		metric(Config.LOG_ANGELCHESTS);

		conf.addDefault(Config.CONSOLE_MESSAGE_ON_OPEN,true);
		metric(Config.CONSOLE_MESSAGE_ON_OPEN);

		conf.addDefault(Config.LOG_FILENAME,"{player}_{world}_{date}.log");
		metric(Config.LOG_FILENAME);

		conf.addDefault(Config.CHEST_FILENAME,"{player}_{world}_{x}_{y}_{z}.yml");
		metric(Config.CHEST_FILENAME);

		conf.addDefault(Config.COLLECT_XP,"true");
		metric(Config.COLLECT_XP);

		conf.addDefault(Config.PURGE_LOGS_OLDER_THAN_X_HOURS,48);
		metric(Config.PURGE_LOGS_OLDER_THAN_X_HOURS);

		conf.addDefault(Config.PURGE_LOGS_EVERY_X_HOURS,1);
		metric(Config.PURGE_LOGS_EVERY_X_HOURS);

		conf.addDefault(Config.UNLOCK_DURATION,0);
		metric(Config.UNLOCK_DURATION);

		conf.addDefault(Config.HOLOGRAM_PROTECTED_COUNTDOWN_TEXT,"&cProtected for {time}");
		metric(Config.HOLOGRAM_PROTECTED_COUNTDOWN_TEXT);

		conf.addDefault(Config.HOLOGRAM_PROTECTED_TEXT,"&cProtected");
		metric(Config.HOLOGRAM_PROTECTED_TEXT);

		conf.addDefault(Config.HOLOGRAM_UNPROTECTED_TEXT,"&aUnprotected");
		metric(Config.HOLOGRAM_UNPROTECTED_TEXT);

		conf.addDefault(Config.USE_DIFFERENT_MATERIAL_WHEN_UNLOCKED,false);
		metric(Config.USE_DIFFERENT_MATERIAL_WHEN_UNLOCKED);

		conf.addDefault(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_CHEST, true);
		metric(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_CHEST);

		conf.addDefault(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_CHEST, true);
		metric(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_CHEST);

		conf.addDefault(Config.PREFIX_MESSAGES, true);
		metric(Config.PREFIX_MESSAGES);

		conf.addDefault(Config.NEVER_REPLACE_BEDROCK,true);
		metric(Config.NEVER_REPLACE_BEDROCK);

		main.disabledMaterials = conf.getStringList(Config.DISABLED_MATERIALS);
		metric(Config.DISABLED_MATERIALS, String.valueOf(main.disabledMaterials.size()));

		main.disabledWorlds =  conf.getStringList(Config.DISABLED_WORLDS);
		metric(Config.DISABLED_WORLDS, String.valueOf(main.disabledWorlds.size()));

		main.disabledRegions =  conf.getStringList(Config.DISABLED_WORLDGUARD_REGIONS);
		metric(Config.DISABLED_WORLDGUARD_REGIONS, String.valueOf(main.disabledRegions.size()));

		conf.addDefault("tp-distance",2);

		conf.addDefault(Config.GUI_BUTTON_BACK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0=");
		conf.addDefault(Config.GUI_BUTTON_TELEPORT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=");
		conf.addDefault(Config.GUI_BUTTON_FETCH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=");
		conf.addDefault(Config.GUI_BUTTON_UNLOCK, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19");
		conf.addDefault(Config.GUI_BUTTON_CONFIRM_INFO, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZlNTIyZDkxODI1MjE0OWU2ZWRlMmVkZjNmZTBmMmMyYzU4ZmVlNmFjMTFjYjg4YzYxNzIwNzIxOGFlNDU5NSJ9fX0=");
		conf.addDefault(Config.GUI_BUTTON_CONFIRM_ACCEPT, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UyYTUzMGY0MjcyNmZhN2EzMWVmYWI4ZTQzZGFkZWUxODg5MzdjZjgyNGFmODhlYThlNGM5M2E0OWM1NzI5NCJ9fX0=");
		conf.addDefault(Config.GUI_BUTTON_CONFIRM_DECLINE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTljZGI5YWYzOGNmNDFkYWE1M2JjOGNkYTc2NjVjNTA5NjMyZDE0ZTY3OGYwZjE5ZjI2M2Y0NmU1NDFkOGEzMCJ9fX0=");
		conf.addDefault(Config.ALIAS_ACGUI, Arrays.asList("ac","angelchest","angelchests","angelchestgui"));
		conf.addDefault(Config.ALIAS_ACLIST, Arrays.asList("acinfo","angelchestinfo","angelchestlist"));
		conf.addDefault(Config.ALIAS_ACFETCH, Arrays.asList("acretrieve","angelchestretrieve","angelchestfetch"));
		conf.addDefault(Config.ALIAS_ACTP, Arrays.asList("acteleport","angelchesttp","angelchestteleport"));
		conf.addDefault(Config.ALIAS_ACUNLOCK, Arrays.asList("angelchestunlock","unlockchest","unlock"));
		conf.addDefault(Config.ALIAS_ACRELOAD, Collections.singletonList("angelchestreload"));
		
		List<String> dontSpawnOnTmp = conf.getStringList(Config.DONT_SPAWN_ON);
		main.dontSpawnOn = new ArrayList<>();
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
			main.dontSpawnOn.add(mat);
		}
		metric(Config.DONT_SPAWN_ON,main.dontSpawnOn.stream().map(Material::name).collect(Collectors.toList()));

		List<String> onlySpawnInTmp =  conf.getStringList(Config.ONLY_SPAWN_IN);
		main.onlySpawnIn = new ArrayList<>();
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
			main.onlySpawnIn.add(mat);
		}
		metric(Config.ONLY_SPAWN_IN,main.onlySpawnIn.stream().map(Material::name).collect(Collectors.toList()));

		if(Material.getMaterial(conf.getString(Config.MATERIAL).toUpperCase())==null) {
			main.getLogger().warning("Invalid Material: "+conf.getString(Config.MATERIAL)+" - falling back to CHEST");
			main.chestMaterial = Material.CHEST;
		} else {
			main.chestMaterial = Material.getMaterial(conf.getString(Config.MATERIAL).toUpperCase());
			if(!main.chestMaterial.isBlock()) {
				main.getLogger().warning("Not a block: "+conf.getString(Config.MATERIAL)+" - falling back to CHEST");
				main.chestMaterial = Material.CHEST;
			}
		}

		if(Material.getMaterial(conf.getString(Config.MATERIAL_UNLOCKED).toUpperCase())==null) {
			main.getLogger().warning("Invalid Material: "+conf.getString(Config.MATERIAL_UNLOCKED)+" - falling back to ENDER_CHEST");
			main.chestMaterialUnlocked = Material.ENDER_CHEST;
		} else {
			main.chestMaterialUnlocked = Material.getMaterial(conf.getString(Config.MATERIAL_UNLOCKED).toUpperCase());
			if(!main.chestMaterialUnlocked.isBlock()) {
				main.getLogger().warning("Not a block: "+conf.getString(Config.MATERIAL_UNLOCKED)+" - falling back to ENDER_CHEST");
				main.chestMaterialUnlocked = Material.ENDER_CHEST;
			}
		}

	}
}
