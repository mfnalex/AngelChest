package de.jeff_media.AngelChestPlus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class ConfigUpdater {

	private static final String[] NODES_NEEDING_DOUBLE_QUOTES = {
			"message-",
			Config.CUSTOM_HEAD_BASE64,
			Config.HOLOGRAM_TEXT,
			Config.ANGELCHEST_INVENTORY_NAME,
			Config.ANGELCHEST_LIST,
			"link-"
	};
	private static final String[] NODES_NEEDING_SINGLE_QUOTES = {"test-"};
	private static final String[] LINES_CONTAINING_STRING_LISTS = {
			Config.DONT_SPAWN_ON+":",
			Config.ONLY_SPAWN_IN+":",
			Config.DISABLED_MATERIALS+":",
			Config.DISABLED_WORLDS+":",
			Config.DISABLED_WORLDGUARD_REGIONS
	};
	private static final String[] LINES_IGNORED = {"config-version:", "plugin-version:"};

	private static void debug(Logger logger, String message) {
		logger.warning(message);
	}

	public static void updateConfig(Main main) {
		Logger logger = main.getLogger();
		debug(logger,"Newest config version  = "+getNewConfigVersion(main));
		debug(logger,"Current config version = "+main.getConfig().getLong(Config.CONFIG_VERSION));
		if(main.getConfig().getLong(Config.CONFIG_VERSION) >= getNewConfigVersion(main)) {
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
					if (defaultLine.startsWith(node+":")) {
						String quotes = getQuotes(node);
						String value = main.getConfig().get(node).toString();

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

	private static String getQuotes(String line) {
		String quotes = "";
		for(String test : NODES_NEEDING_DOUBLE_QUOTES) {
			if (line.startsWith(test)) {
				quotes = "\"";
			}
		}
		for(String test : NODES_NEEDING_SINGLE_QUOTES) {
			if(line.startsWith(test)) {
				quotes="'";
			}
		}
		return quotes;
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

	private static long getNewConfigVersion(Main main) {
		InputStream in = main.getClass().getResourceAsStream("/config-version.txt");
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
		oldFile.getAbsoluteFile().renameTo(newFile.getAbsoluteFile());
	}

	public static void migrateFromFreeVersion(Main main) {
		if(!main.getDataFolder().exists()) {
			File oldFolder = new File(main.getDataFolder().getPath()+File.separator+".."+File.separator+"AngelChest");
			if(oldFolder.exists()) {
				oldFolder.renameTo(main.getDataFolder());
			} else {
				main.getDataFolder().mkdirs();
			}
		}
	}
}
