package de.jeff_media.AngelChest;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jeff_media.AngelChest.commands.*;
import de.jeff_media.AngelChest.config.*;
import de.jeff_media.AngelChest.data.AngelChest;
import de.jeff_media.AngelChest.data.BlacklistEntry;
import de.jeff_media.AngelChest.data.PendingConfirm;
import de.jeff_media.AngelChest.data.DeathCause;
import de.jeff_media.AngelChest.enums.BlacklistResult;
import de.jeff_media.AngelChest.enums.EconomyStatus;
import de.jeff_media.AngelChest.enums.Features;
import de.jeff_media.AngelChest.gui.GUIListener;
import de.jeff_media.AngelChest.gui.GUIManager;
import de.jeff_media.AngelChest.hooks.PlaceholderAPIHook;
import de.jeff_media.AngelChest.hooks.MinepacksHook;
import de.jeff_media.AngelChest.hooks.WorldGuardHandler;
import de.jeff_media.AngelChest.listeners.*;
import de.jeff_media.AngelChest.nbt.NBTUtils;
import de.jeff_media.AngelChest.utils.*;
import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import de.jeff_media.SpigotJeffMediaPlugin;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.nbtapi.NBTAPI;
import io.papermc.lib.PaperLib;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.math.NumberUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class Main extends JavaPlugin implements SpigotJeffMediaPlugin {

	private static final String SPIGOT_RESOURCE_ID_PLUS = "88214";
	private static final String SPIGOT_RESOURCE_ID_FREE = "60383";
	public static final int BSTATS_ID = 3194;
	private static final String UPDATECHECKER_LINK_API = "https://api.jeff-media.de/angelchestplus/latest-version.txt";
	private static final String UPDATECHECKER_LINK_DOWNLOAD_FREE = "https://www.spigotmc.org/resources/"+SPIGOT_RESOURCE_ID_FREE;
	private static final String UPDATECHECKER_LINK_DOWNLOAD_PLUS = "https://www.spigotmc.org/resources/"+SPIGOT_RESOURCE_ID_PLUS;
	private static final String UPDATECHECKER_LINK_CHANGELOG = "https://www.spigotmc.org/resources/"+SPIGOT_RESOURCE_ID_PLUS+"/updates";
	private static final String UPDATECHECKER_LINK_DONATE = "https://paypal.me/mfnalex";

	@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
	private String UID = "%%__USER__%%" ;
	@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
	private String NONCE = "%%__NONCE__%%" ;
	@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
	private String RESOURCE = "%%__RESOURCE__%%" ;

	public HashMap<UUID, PendingConfirm> pendingConfirms;
	public LinkedHashMap<Block, AngelChest> angelChests;
	public HashMap<UUID,Block> lastPlayerPositions;
	public HashMap<UUID,Entity> killers;
	public Material chestMaterial;
	public Material chestMaterialUnlocked;
	public String[] invalidConfigFiles;
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
	public GUIListener guiListener;
	public Logger logger;
	public Economy econ;
	public Map<String, BlacklistEntry> itemBlacklist;
	public Metrics metrics;
	public NBTUtils nbtUtils;

	public EconomyStatus economyStatus = EconomyStatus.UNKNOWN;

	private static Main instance;


	public Material getChestMaterial(AngelChest chest) {
		if(!Daddy.allows(Features.GENERIC)) {
			return chestMaterial;
		}
		if(!getConfig().getBoolean(Config.USE_DIFFERENT_MATERIAL_WHEN_UNLOCKED)) {
			return chestMaterial;
		}
		return chest.isProtected ? chestMaterial : chestMaterialUnlocked;
	}

	public static Main getInstance() {
		return instance;
	}

	public void debug(String t) {
		if(debug) getLogger().info("[DEBUG] " + t);
	}

	public void verbose(String t) {
		if(verbose) getLogger().info("[DEBUG] [VERBOSE] " + t);
	}
	
	@Override
	public void onEnable() {

		instance = this;
		/*Daddy start*/
		Daddy.init(this);
		/*Daddy end*/
		NBTAPI.init(this);

		migrateFromAngelChestPlus1X();
		ChestFileUpdater.updateChestFilesToNewDeathCause();
		if(getMcVersion()<13) {
			EmergencyMode.severe(EmergencyMode.UNSUPPORTED_MC_VERSION_1_12);
			emergencyMode = true;
			return;
		}

		ConfigurationSerialization.registerClass(DeathCause.class);

		if(isAngelChestPlus1XInstalled()) {
			emergencyMode = true;
			EmergencyMode.severe(EmergencyMode.FREE_VERSION_INSTALLED);
			return;
		}

		watchdog = new Watchdog(this);

		metrics = new Metrics(this,BSTATS_ID);
		ConfigUtils.reloadCompleteConfig(false);


		angelChests = new LinkedHashMap<>();
		lastPlayerPositions = new HashMap<>();
		killers = new HashMap<>();
		logger = new Logger();

		debug("Loading AngelChests from disk");
		loadAllAngelChestsFromFile();

		scheduleRepeatingTasks();

		debug("Registering commands");
		registerCommands();
		debug("Setting command executors...");
		CommandFetchOrTeleport commandFetchOrTeleport = new CommandFetchOrTeleport();
		this.getCommand("acunlock").setExecutor(new CommandUnlock());
		this.getCommand("aclist").setExecutor(new CommandList());
		this.getCommand("acfetch").setExecutor(commandFetchOrTeleport);
		this.getCommand("actp").setExecutor(commandFetchOrTeleport);
		this.getCommand("acreload").setExecutor(new CommandReload());
		this.getCommand("acgui").setExecutor(new CommandGUI());

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
			new PlaceholderAPIHook(this).register();
		}

		CommandDebug commandDebug = new CommandDebug();
		this.getCommand("acdebug").setExecutor(commandDebug);
		this.getCommand("acdebug").setTabCompleter(commandDebug);
		this.getCommand("acversion").setExecutor(new CommandVersion());


		debug("Registering listeners");
		getServer().getPluginManager().registerEvents(new PlayerListener(),this);
		getServer().getPluginManager().registerEvents(new HologramListener(),this);
		getServer().getPluginManager().registerEvents(new BlockListener(),this);
		getServer().getPluginManager().registerEvents(new PistonListener(),this);
		getServer().getPluginManager().registerEvents(new EmergencyListener(),this);
		guiListener = new GUIListener();
		getServer().getPluginManager().registerEvents(guiListener,this);
		

		if (debug) getLogger().info("Disabled Worlds: "+disabledWorlds.size());
		if (debug) getLogger().info("Disabled WorldGuard regions: "+disabledRegions.size());

		setEconomyStatus();

		char color = Daddy.allows(Features.DONT_SHOW_NAG_MESSAGE) ? 'a' : '6';
		for(String line : Daddy.allows(Features.DONT_SHOW_NAG_MESSAGE) ? Messages.usingPlusVersion : Messages.usingFreeVersion) {
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&"+color+line));
		}

		if(Daddy.allows(Features.GENERIC)) {
			DiscordVerificationUtils.createVerificationFile();
		}
		
	}

	private void migrateFromAngelChestPlus1X() {
		File ACFolder = new File(getDataFolder().getAbsolutePath());
		File ACPlusFolder = new File(ACFolder.getParentFile(),"AngelChestPlus");

		// ACPlus folder exists
		if(ACPlusFolder.isDirectory()) {
			getLogger().warning("You are upgrading from AngelChestPlus 1.XX to "+getDescription().getVersion());
			getLogger().warning("Trying to rename your AngelChestPlus folder to AngelChest...");

			// Shit: AC folder also exists
			if(ACFolder.isDirectory()) {
				getLogger().warning("Strange - you already have a directory called AngelChest. Trying to rename that one...");

				ACFolder.renameTo(new File(getDataFolder().getAbsolutePath(),"AngelChest-backup"));

				if(ACFolder.isDirectory()) {
					getLogger().warning("It gets stranger: Couldn't rename that directory.");
					getLogger().severe("Well, we couldn't remove your old AngelChest folder, which means we cannot apply your old config. Please manually rename the AngelChestPlus folder to AngelChest.");
				}
			}

			// AC folder does not (longer) exist
			if(!ACFolder.isDirectory()) {
				ACPlusFolder.renameTo(ACFolder);
			}

			if(!ACFolder.isDirectory()
				|| ACPlusFolder.isDirectory()) {
				getLogger().severe("Could not rename your AngelChestPlus folder to AngelChest. Please do so manually.");
			} else {
				getLogger().warning(ChatColor.GREEN+"Successfully upgraded from AngelChestPlus 1.XX to "+getDescription().getVersion());
			}

		}
	}

	private void setEconomyStatus() {
		Plugin v = getServer().getPluginManager().getPlugin("Vault");

		if (v == null) {
			getLogger().info("Vault not installed, disabling economy functions.");
			economyStatus = EconomyStatus.INACTIVE;
			return;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			getLogger().info("No EconomyServiceProvider found, disabling economy functions.");
			economyStatus = EconomyStatus.INACTIVE;
			return;
		}

		if (rsp.getProvider() == null) {
			getLogger().info("No EconomyProvider found, disabling economy functions.");
			economyStatus = EconomyStatus.INACTIVE;
			return;
		}

		econ = rsp.getProvider();
		economyStatus = EconomyStatus.ACTIVE;
		getLogger().info("Successfully hooked into Vault and the EconomyProvider, enabling economy functions.");
	}

	private void registerCommands() {
		String[][] commands = new String[][]{
				{"acgui", Permissions.ALLOW_USE},
				{"aclist", Permissions.ALLOW_USE},
				{"acfetch", Permissions.ALLOW_FETCH},
				{"actp", Permissions.ALLOW_TELEPORT},
				{"acunlock", Permissions.ALLOW_PROTECT},
				{"acreload", Permissions.ALLOW_RELOAD},
				{"acdebug", Permissions.DEBUG}
		};
		for(String[] commandAndPermission : commands) {
			ArrayList<String> command = new ArrayList<>();
			debug("Registering command "+commandAndPermission+" with aliases");
			command.add(commandAndPermission[0]);
			List<String> aliases = getConfig().getStringList("command-aliases-"+commandAndPermission);
			for(String alias : aliases) {
				command.add(alias);
				debug("- "+alias);
			}
			CommandManager.registerCommand(commandAndPermission[1],command.toArray(new String[0]));
		}
	}

	private void scheduleRepeatingTasks() {
		// Rename holograms
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for(AngelChest chest : angelChests.values()) {
				if(chest != null && chest.hologram != null) {
					chest.hologram.update(chest);
				}
			}
		}, Ticks.fromSeconds(1), Ticks.fromSeconds(1));

		// Track player positions
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(((Entity) player).isOnGround()) {
					lastPlayerPositions.put(player.getUniqueId(), player.getLocation().getBlock());
				}
			}
		}, Ticks.fromSeconds(1), Ticks.fromSeconds(1));

		// Fix broken AngelChests
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
				if(isBrokenAngelChest(entry.getKey(),entry.getValue())) {
					// TODO: Disabled for now, but left behind if someone still has missing chests upon end crystal generation
					Block block = entry.getKey();
					debug("Fixing broken AngelChest at "+block.getLocation());
					entry.setValue(new AngelChest(getAngelChest(block).saveToFile(true)));
				}
			}
		}, 0L, Ticks.fromSeconds(2));

		// Schedule DurationTimer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			Iterator<AngelChest> it = angelChests.values().iterator();
			while(it.hasNext()) {
				AngelChest ac = it.next();
				if(ac==null) continue;
				ac.secondsLeft--;
				if(ac.secondsLeft<=0 && !ac.infinite) {
					if(getServer().getPlayer(ac.owner)!=null) {
						Messages.send(getServer().getPlayer(ac.owner),messages.MSG_ANGELCHEST_DISAPPEARED);
					}
					ac.destroy(true);
					it.remove();
				}
				if(Daddy.allows(Features.GENERIC) && ac.isProtected && ac.unlockIn!=-1) { // Don't add feature here, gets called every second
					ac.unlockIn--;
					if(ac.unlockIn==-1) {
						ac.isProtected=false;
						ac.scheduleBlockChange();
						if(getServer().getPlayer(ac.owner)!=null) {
							Messages.send(getServer().getPlayer(ac.owner),messages.MSG_UNLOCKED_AUTOMATICALLY);
						}
					}
				}
			}
		}, 0, Ticks.fromSeconds(1));

		// Remove dead holograms
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,() -> {
			for(World world : Bukkit.getWorlds()) {
				HologramFixer.removeDeadHolograms(world);
			}
		}, Ticks.fromMinutes(1), Ticks.fromMinutes(1));
	}



	public void loadAllAngelChestsFromFile() {
		File dir = new File(getDataFolder().getPath() + File.separator + "angelchests");
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
				debug("Loading AngelChest " + child.getName());
		      AngelChest ac = new AngelChest(child);
		      if(ac.success) {
				  angelChests.put(ac.block, ac);
			  } else {
				  debug("Error while loading "+child.getName()+", probably the world is not loaded yet. Will try again on next world load.");
			  }
		    }
		  }
		
	}

	public void onDisable() {
		gracefulShutdown = true;
		if(emergencyMode) return;

		saveAllAngelChestsToFile(true);

	}

	public void saveAllAngelChestsToFile(boolean removeChests) {
		// Destroy all Angel Chests, including hologram AND CONTENTS!
		//for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
		//	Utils.destroyAngelChest(entry.getKey(), entry.getValue(), this);
		//}
		for (Entry<Block, AngelChest> entry : angelChests.entrySet()) {
			entry.getValue().saveToFile(removeChests);

			// The following line isn't needed anymore but it doesn't hurt either
			if(removeChests) {
				entry.getValue().hologram.destroy();
			}
		}
	}

	public @Nullable String isItemBlacklisted(ItemStack item) {
		if(!Daddy.allows(Features.GENERIC)) { // Don't add feature here, gets called for every item on death
			return null;
		}
		for(BlacklistEntry entry : itemBlacklist.values()) {
			BlacklistResult result = entry.matches(item);
			if(result == BlacklistResult.MATCH) {
				return result.getName();
			}
		}
		return null;
	}

	public boolean isAngelChest(Block block) {
		return angelChests.containsKey(block);
	}

	public boolean isBrokenAngelChest(Block block, AngelChest chest) {
		return block.getType() != getChestMaterial(chest);
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
					UPDATECHECKER_LINK_DOWNLOAD_PLUS,
					UPDATECHECKER_LINK_DOWNLOAD_FREE,
					UPDATECHECKER_LINK_CHANGELOG,
					UPDATECHECKER_LINK_DONATE,
					Daddy.allows(Features.GENERIC));
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

	private boolean isAngelChestPlus1XInstalled() {
		return Bukkit.getPluginManager().getPlugin("AngelChestPlus") != null;
	}

	// Returns 16 for 1.16, etc.
	public static int getMcVersion() {
		Pattern p = Pattern.compile("^1\\.(\\d*)\\.");
		Matcher m = p.matcher((Bukkit.getBukkitVersion()));
		int version = -1;
		while (m.find()) {
			if (NumberUtils.isNumber(m.group(1)))
				version = Integer.parseInt(m.group(1));
		}
		return version;
	}

	@Override
	public String getUID() {
		return UID;
	}

	@Override
	public String getNONCE() {
		return NONCE;
	}

	@Override
	public String getRESOURCE() {
		return RESOURCE;
	}
}
