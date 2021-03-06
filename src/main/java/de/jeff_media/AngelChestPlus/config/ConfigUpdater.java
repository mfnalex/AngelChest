package de.jeff_media.AngelChestPlus.config;

import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.config.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Updates the config file. When a new config file is shipped with AngelChest, it will save the new
 * file and replace all default values with the values that were set in the old config file.
 */
public class ConfigUpdater {

	// Lines STARTING WITH these names will get their values wrapped in double quotes
	private static final String[] NODES_NEEDING_DOUBLE_QUOTES = {
			"message-",
			Config.CUSTOM_HEAD_BASE64,
			Config.HOLOGRAM_TEXT,
			Config.ANGELCHEST_INVENTORY_NAME,
			Config.ANGELCHEST_LIST,
			Config.HOLOGRAM_PROTECTED_TEXT,
			Config.HOLOGRAM_PROTECTED_COUNTDOWN_TEXT,
			Config.HOLOGRAM_UNPROTECTED_TEXT,
			"link-",
			"gui-",
			"log-filename",
			"chest-filename"
	};
	// Lines STARTING WITH these names will get their values wrapped in single quotes
	private static final String[] NODES_NEEDING_SINGLE_QUOTES = {"test-"};

	// Lines STARTING WITH these names will be treated as String lists
	private static final String[] LINES_CONTAINING_STRING_LISTS = {
			Config.DONT_SPAWN_ON+":",
			Config.ONLY_SPAWN_IN+":",
			Config.DISABLED_MATERIALS+":",
			Config.DISABLED_WORLDS+":",
			Config.DISABLED_WORLDGUARD_REGIONS+":",
			"command-aliases-"
	};
	// Lines STARTING WITH these names will never get the old value applied
	private static final String[] LINES_IGNORED = {"config-version:", "plugin-version:"};

	/**
	 * For debugging the config updater only
	 */
	private static void debug(Logger logger, String message) {
		if(false) {
			logger.warning(message);
		}
	}

	/**
	 * Attempts to update the config
	 */
	public static void updateConfig() {
		Main main = Main.getInstance();
		Logger logger = main.getLogger();
		debug(logger,"Newest config version  = "+getNewConfigVersion());
		debug(logger,"Current config version = "+main.getConfig().getLong(Config.CONFIG_VERSION));
		if(main.getConfig().getLong(Config.CONFIG_VERSION) >= getNewConfigVersion()) {
			debug(logger,"The config currently used has an equal or newer version than the one shipped with this release.");
			return;
		}

		logger.info("===========================================");
		logger.info("You are using an outdated config file.");
		logger.info("Your config file will now be updated to the");
		logger.info("newest version. You changes will be kept.");
		logger.info("===========================================");

		backupCurrentConfig(main);
		main.saveDefaultConfig();

		Set<String> oldConfigNodes = main.getConfig().getKeys(false);
		ArrayList<String> newConfig = new ArrayList<>();

		// Iterate through ALL lines from the new default config
		for(String defaultLine : getNewConfigAsArrayList(main)) {

			String updatedLine = defaultLine;

			if(defaultLine.startsWith("-") || defaultLine.startsWith(" -") || defaultLine.startsWith("  -")) {
				debug(logger, "Not including default String list entry: "+defaultLine);
				updatedLine = null;
			}
			else if(lineContainsIgnoredNode(defaultLine)) {
				debug(logger,"Not updating this line: " + defaultLine);
			}
			else if(lineIsStringList(defaultLine)) {
				updatedLine = null;
				newConfig.add(defaultLine);
				String node = defaultLine.split(":")[0];
				for(String entry : main.getConfig().getStringList(node)) {
					newConfig.add("- " + entry);
				}
			}
			else {
				for(String node : oldConfigNodes) {
					// Iterate through all keys from the old config file.
					if (defaultLine.startsWith(node+":")) {
						// This key from the old file matches this line from the new file! Updating...
						String quotes = getQuotes(node);
						String value = main.getConfig().get(node).toString();

						// The hologram text needs special escaping for the newline symbols
						if(node.equals("hologram-text")) {
							value = value.replaceAll("\n","\\\\n");
						}

						updatedLine = node + ": " + quotes + value + quotes;
					}
				}
			}

			if(updatedLine != null) {
				newConfig.add(updatedLine);
			}
		}

		saveArrayListToConfig(main, newConfig);
	}

	/**
	 * Returns a String representing the correct quotes to use for this key's value
	 * @param line line/key to get the quotes for
	 * @return double quote, single quote or empty string, according to the key name
	 */
	private static String getQuotes(String line) {
		for(String test : NODES_NEEDING_DOUBLE_QUOTES) {
			if (line.startsWith(test)) {
				return "\"";
			}
		}
		for(String test : NODES_NEEDING_SINGLE_QUOTES) {
			if(line.startsWith(test)) {
				return "'";
			}
		}
		return "";
	}

	private static boolean lineIsStringList(String line) {
		for(String test : LINES_CONTAINING_STRING_LISTS) {
			if(line.startsWith(test)) {
				return true;
			}
		}
		return false;
	}

	private static boolean lineContainsIgnoredNode(String line) {
		for(String test : LINES_IGNORED) {
			if(line.startsWith(test)) {
				return true;
			}
		}
		return false;
	}

	private static List<String> getNewConfigAsArrayList(Main main) {
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get(getFilePath(main,"config.yml")), StandardCharsets.UTF_8);
			return lines;
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		return null;
	}

	private static void saveArrayListToConfig(Main main, List<String> lines) {
		try {
			BufferedWriter fw = Files.newBufferedWriter(new File(getFilePath(main,"config.yml")).toPath(),StandardCharsets.UTF_8);
			for(String line : lines) {
				fw.write(line + System.lineSeparator());
			}
			fw.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private static String getFilePath(Main main, String fileName) {
		return main.getDataFolder() + File.separator + fileName;
	}

	/**
	 * Returns the config version of the currently installed AngelChest default config
	 * @return default config version
	 */
	private static long getNewConfigVersion() {
		InputStream in = Main.getInstance().getClass().getResourceAsStream("/config-version.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			return Long.parseLong(reader.readLine());
		} catch (IOException ioException) {
			ioException.printStackTrace();
			return 0;
		}

	}

	private static void backupCurrentConfig(Main main) {
		File oldFile = new File(getFilePath(main,"config.yml"));
		File newFile = new File(getFilePath(main,"config-backup-"+main.getConfig().getString(Config.CONFIG_PLUGIN_VERSION)+".yml"));
		if(newFile.exists()) newFile.delete();
		if(oldFile.getAbsoluteFile().renameTo(newFile.getAbsoluteFile())) {
			main.getLogger().severe("Could not rename "+oldFile.getAbsolutePath()+" to "+newFile.getAbsolutePath());
		}
	}

	/**
	 * Attempts to rename the old AngelChest folder to AngelChestPlus
	 * TODO: This doesn't work?!
	 */
	public static void migrateFromFreeVersion() {
		Main main = Main.getInstance();
		if(!main.getDataFolder().exists()) {
			File oldFolder = new File(main.getDataFolder().getPath()+File.separator+".."+File.separator+"AngelChest");
			if(oldFolder.exists()) {
				main.getLogger().warning("Renaming AngelChest folder to AngelChestPlus");
				if(!oldFolder.renameTo(new File(main.getDataFolder()+"asd"))) {
					main.getLogger().severe("Could not rename old plugin folder!");
				}
			} else {
				main.getLogger().warning("4");
				main.getDataFolder().mkdirs();
			}
		}
	}
}
