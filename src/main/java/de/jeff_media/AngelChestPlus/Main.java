package de.jeff_media.AngelChestPlus;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import de.jeff_media.AngelChestPlus.commands.*;
import de.jeff_media.AngelChestPlus.gui.GUIListener;
import de.jeff_media.AngelChestPlus.gui.GUIManager;
import de.jeff_media.AngelChestPlus.hooks.AngelChestPlaceholders;
import de.jeff_media.AngelChestPlus.hooks.MinepacksHook;
import de.jeff_media.AngelChestPlus.listeners.BlockListener;
import de.jeff_media.AngelChestPlus.listeners.HologramListener;
import de.jeff_media.AngelChestPlus.listeners.PistonListener;
import de.jeff_media.AngelChestPlus.listeners.PlayerListener;
import de.jeff_media.AngelChestPlus.utils.ConfigUtils;
import de.jeff_media.AngelChestPlus.utils.GroupUtils;
import de.jeff_media.AngelChestPlus.utils.HookUtils;
import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import io.papermc.lib.PaperLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class Main extends JavaPlugin {

	private static final String SPIGOT_RESOURCE_ID = "60383";
	private static final int BSTATS_ID = 3194;
	private static final String UPDATECHECKER_LINK_API = "https://api.jeff-media.de/angelchestplus/latest-version.txt";
	private static final String UPDATECHECKER_LINK_DOWNLOAD = "https://www.spigotmc.org/resources/"+SPIGOT_RESOURCE_ID;
	private static final String UPDATECHECKER_LINK_CHANGELOG = "https://www.spigotmc.org/resources/"+SPIGOT_RESOURCE_ID+"/updates";
	private static final String UPDATECHECKER_LINK_DONATE = "https://paypal.me/mfnalex";

	public HashMap<UUID,PendingConfirm> pendingConfirms;
	public LinkedHashMap<Block,AngelChest> angelChests;
	public HashMap<UUID,Block> lastPlayerPositions;
	public Material chestMaterial;
	PluginUpdateChecker updateChecker;

	public boolean debug = false;
	public boolean verbose = false;

	public boolean gracefulShutdown = false;

	public List<String> disabledMaterials;
	public List<String> disabledWorlds;
	public List<String> disabledRegions;
	public List<Material> dontSpawnOn;
	public List<Material> onlySpawnIn;

	boolean emergencyMode = false;
	
	public Messages messages;
	public GroupUtils groupUtils;
	public WorldGuardHandler worldGuardHandler;
	public HookUtils hookUtils;
	public Watchdog watchdog;
	public MinepacksHook minepacksHook;
	public GUIManager guiManager;

	public void debug(String t) {
		if(debug) getLogger().info("[DEBUG] " + t);
	}

	public void verbose(String t) {
		if(verbose) getLogger().info("[DEBUG] [VERBOSE] " + t);
	}
	
	@Override
	public void onEnable() {

		if(isFreeVersionInstalled()) {
			emergencyMode = true;
			EmergencyMode.run(this, EmergencyMode.EmergencyReason.FREE_VERSION_INSTALLED);
			return;
		}

		watchdog = new Watchdog(this);

		ConfigUtils.reloadCompleteConfig(this,false);


		angelChests = new LinkedHashMap<>();
		lastPlayerPositions = new HashMap<>();

		debug("Loading AngelChests from disk");
		loadAllAngelChestsFromFile();
		//armorStandUUIDs = new ArrayList<UUID>();


		
		// Deletes old armorstands and restores broken AngelChests (only case where I currently know this happens is when a endcrystal spanws in a chest)
		Main main = this;

		scheduleRepeatingTasks(main);

		debug("Registering commands");
		registerCommands();
		debug("Setting command executors...");
		CommandFetchOrTeleport commandFetchOrTeleport = new CommandFetchOrTeleport(this);
		this.getCommand("acunlock").setExecutor(new CommandUnlock(this));
		this.getCommand("aclist").setExecutor(new CommandList(this));
		this.getCommand("acfetch").setExecutor(commandFetchOrTeleport);
		this.getCommand("actp").setExecutor(commandFetchOrTeleport);
		this.getCommand("acreload").setExecutor(new CommandReload(this));
		this.getCommand("acgui").setExecutor(new CommandGUI(this));

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
			new AngelChestPlaceholders(this).register();
		}

		this.getCommand("acd").setExecutor(new CommandDebug(this));

		debug("Registering listeners");
		getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
		getServer().getPluginManager().registerEvents(new HologramListener(this),this);
		getServer().getPluginManager().registerEvents(new BlockListener(this),this);
		getServer().getPluginManager().registerEvents(new PistonListener(this),this);
		getServer().getPluginManager().registerEvents(new GUIListener(this),this);
		
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this,3194);
		metrics.addCustomChart(new Metrics.SimplePie(Config.MATERIAL, () -> chestMaterial.name()));
		metrics.addCustomChart(new Metrics.SimplePie("auto_respawn", () -> getConfig().getBoolean(Config.AUTO_RESPAWN)+""));
		metrics.addCustomChart(new Metrics.SimplePie("totem_works_everywhere", () -> getConfig().getBoolean(Config.TOTEM_OF_UNDYING_WORKS_EVERYWHERE)+""));
		

		if (debug) getLogger().info("Disabled Worlds: "+disabledWorlds.size());
		if (debug) getLogger().info("Disabled WorldGuard regions: "+disabledRegions.size());
		
		
		// Schedule DurationTimer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			Iterator<AngelChest> it = angelChests.values().iterator();
			while(it.hasNext()) {
				AngelChest ac = it.next();
				if(ac==null) continue;
				ac.secondsLeft--;
				if(ac.secondsLeft<=0 && !ac.infinite) {
					if(getServer().getPlayer(ac.owner)!=null) {
						getServer().getPlayer(ac.owner).sendMessage(messages.MSG_ANGELCHEST_DISAPPEARED);
					}
					ac.destroy(true);
					it.remove();
				}
			}
		}, 0, 20);
		
	}

	private void registerCommands() {
		String[] commands = new String[] {"acgui","aclist","acfetch","actp","acunlock","acreload"};
		for(String command : commands) {
			ArrayList<String> newCommand = new ArrayList<>();
			debug("Registering command "+command+" with aliases");
			newCommand.add(command);
			List<String> aliases = getConfig().getStringList("command-aliases-"+command);
			for(String alias : aliases) {
				newCommand.add(alias);
				debug("- "+alias);
			}
			CommandManager.registerCommand(this, newCommand.toArray(new String[0]));
		}
	}

	private void scheduleRepeatingTasks(Main main) {
		// Rename holograms
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for(AngelChest chest : angelChests.values()) {
				if(chest != null && chest.hologram != null) {
					chest.hologram.update(chest);
				}
			}
		}, 20L, 20L);

		// Track player positions
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(((Entity) player).isOnGround()) {
					lastPlayerPositions.put(player.getUniqueId(), player.getLocation().getBlock());
				}
			}
		}, 20L, 20L);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

			// The following might only be needed for chests destroyed by end crystals spawning during the init phase of the ender dragon
			for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {

				if(!PaperLib.isChunkGenerated(entry.getKey().getLocation())) {
					verbose("Chunk at "+entry.getKey().getLocation().toString()+" has not been generated!");
				}

				if(!entry.getKey().getWorld().isChunkLoaded(entry.getKey().getX() >> 4,entry.getKey().getZ() >> 4)) {

					verbose("Chunk at "+entry.getKey().getLocation().toString() + " is not loaded, skipping repeating task regarding angelChests.entrySet()");
					// CONTINUE IF CHUNK IS NOT LOADED

					continue;
				}
				/*if(!isAngelChest(entry.getKey())) {
					entry.getValue().destroy();
					debug("Removing block from list because it's no AngelChest");
				}*/
				if(isBrokenAngelChest(entry.getKey())) {
					// TODO: Disabled for now, but left behind if someone still has missing chests upon end crystal generation
					Block block = entry.getKey();
					debug("Fixing broken AngelChest at "+block.getLocation());
					entry.setValue(new AngelChest(getAngelChest(block).saveToFile(true), main));
				}
			}
		}, 0L, 2 * 20);
	}

	public void loadAllAngelChestsFromFile() {
		File dir = new File(getDataFolder().getPath() + File.separator + "angelchests");
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
				getLogger().info("Loading AngelChest " + child.getName());
		      AngelChest ac = new AngelChest(child,this);
		      if(ac.success) {
				  angelChests.put(ac.block, ac);
			  } else {
				  getLogger().info("Error while loading "+child.getName()+", probably the world is not loaded yet. Will try again on next world load.");
			  }
		    }
		  }
		
	}

	public void onDisable() {
		gracefulShutdown = true;
		if(emergencyMode) return;

		saveAllAngelChestsToFile();

	}

	public void saveAllAngelChestsToFile() {
		// Destroy all Angel Chests, including hologram AND CONTENTS!
		//for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
		//	Utils.destroyAngelChest(entry.getKey(), entry.getValue(), this);
		//}
		for (Entry<Block, AngelChest> entry : angelChests.entrySet()) {
			entry.getValue().saveToFile(true);
			entry.getValue().hologram.destroy();
		}
	}

	

	public boolean isAngelChest(Block block) {
		return angelChests.containsKey(block);
	}

	public boolean isBrokenAngelChest(Block block) {
		return block.getType() != chestMaterial;
	}
	
	public boolean isAngelChestHologram(Entity e) {
		// Skip this because it is checked in the listener and this method is not needed elsewhere
		//if(!(e instanceof ArmorStand)) return false;

		return getAllArmorStandUUIDs().contains(e.getUniqueId());
	}
	
	public @Nullable AngelChest getAngelChest(Block block) {
		debug("Getting AngelChest for block "+block.getLocation().toString());
		if(angelChests.containsKey(block)) {
			return angelChests.get(block);
		}
		return null;
	}
	
	public AngelChest getAngelChestByHologram(ArmorStand armorStand) {
		for(AngelChest angelChest : angelChests.values()) {
			if( angelChest == null) continue;
			if(angelChest.hologram == null) continue;
			if(angelChest.hologram.armorStandUUIDs.contains(armorStand.getUniqueId())) {
				return angelChest;
			}
		}
		return null;
	}
	
	public ArrayList<UUID> getAllArmorStandUUIDs() {
		ArrayList<UUID> armorStandUUIDs = new ArrayList<>();
		for(AngelChest ac : angelChests.values()) {
			if(ac==null || ac.hologram==null) continue;
			//if(armorStand == null) continue;
			armorStandUUIDs.addAll(ac.hologram.armorStandUUIDs);
		}
		return armorStandUUIDs;
	}

	public void initUpdateChecker() {
		if(updateChecker == null) {
			updateChecker = new PluginUpdateChecker(this,
					UPDATECHECKER_LINK_API,
					UPDATECHECKER_LINK_DOWNLOAD,
					UPDATECHECKER_LINK_CHANGELOG,
					UPDATECHECKER_LINK_DONATE);
		} else {
			updateChecker.stop();
		}

		switch(getConfig().getString(Config.CHECK_FOR_UPDATES).toLowerCase()) {
			case "true":
				updateChecker.check((long) (getConfig().getDouble(Config.CHECK_FOR_UPDATES_INTERVAL) * 60 * 60));
				break;
			case "false":
				break;
			default:
				updateChecker.check();
		}
	}

	private boolean isFreeVersionInstalled() {
		return Bukkit.getPluginManager().getPlugin("AngelChest") != null;
	}

}
