package de.jeff_media.angelchest;

import de.jeff_media.SpigotJeffMediaPlugin;
import de.jeff_media.angelchest.commands.*;
import de.jeff_media.angelchest.config.*;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.BlacklistEntry;
import de.jeff_media.angelchest.data.DeathCause;
import de.jeff_media.angelchest.data.PendingConfirm;
import de.jeff_media.angelchest.enums.BlacklistResult;
import de.jeff_media.angelchest.enums.EconomyStatus;
import de.jeff_media.angelchest.enums.Features;
import de.jeff_media.angelchest.gui.GUIListener;
import de.jeff_media.angelchest.gui.GUIManager;
import de.jeff_media.angelchest.hooks.MinepacksHook;
import de.jeff_media.angelchest.hooks.PlaceholderAPIHook;
import de.jeff_media.angelchest.hooks.WorldGuardHandler;
import de.jeff_media.angelchest.hooks.WorldGuardWrapper;
import de.jeff_media.angelchest.listeners.*;
import de.jeff_media.angelchest.nbt.NBTUtils;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.JeffLib;
import de.jeff_media.jefflib.NBTAPI;
import de.jeff_media.jefflib.Ticks;
import de.jeff_media.jefflib.thirdparty.io.papermc.paperlib.PaperLib;
import de.jeff_media.pluginupdatechecker.PluginUpdateChecker;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.math.NumberUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
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

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin implements SpigotJeffMediaPlugin, AngelChestPlugin {

    public static final int BSTATS_ID = 3194;
    public static final String UPDATECHECKER_LINK_DONATE = "https://paypal.me/mfnalex";
    public static final String DISCORD_LINK = "https://discord.jeff-media.de";
    public static final UUID consoleSenderUUID = UUID.randomUUID();
    private static final String SPIGOT_RESOURCE_ID_PLUS = "88214";
    public static final String UPDATECHECKER_LINK_DOWNLOAD_PLUS = "https://www.spigotmc.org/resources/" + SPIGOT_RESOURCE_ID_PLUS;
    public static final String UPDATECHECKER_LINK_CHANGELOG = "https://www.spigotmc.org/resources/" + SPIGOT_RESOURCE_ID_PLUS + "/updates";
    private static final String SPIGOT_RESOURCE_ID_FREE = "60383";
    public static final String UPDATECHECKER_LINK_DOWNLOAD_FREE = "https://www.spigotmc.org/resources/" + SPIGOT_RESOURCE_ID_FREE;
    private static final String UPDATECHECKER_LINK_API = "https://api.jeff-media.de/angelchestplus/latest-version.txt";
    private static Main instance;
    public HashMap<UUID, PendingConfirm> pendingConfirms;
    public LinkedHashMap<Block, AngelChest> angelChests;
    public HashMap<UUID, Block> lastPlayerPositions;
    public HashMap<UUID, Entity> killers;
    public Material chestMaterial;
    public Material chestMaterialUnlocked;
    public String[] invalidConfigFiles;
    public PluginUpdateChecker updateChecker;
    public boolean debug = false;
    public boolean verbose = false;
    public List<String> disabledMaterials;
    public List<String> disabledWorlds;
    public List<String> disabledRegions;
    public List<Material> dontSpawnOn;
    public List<Material> onlySpawnIn;
    public Messages messages;
    public GroupUtils groupUtils;
    public WorldGuardWrapper worldGuardWrapper;
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
    boolean emergencyMode = false;
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private String UID = "%%__USER__%%";
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private String NONCE = "%%__NONCE__%%";
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private String RESOURCE = "%%__RESOURCE__%%";

    public static Main getInstance() {
        return instance;
    }

    // Returns 16 for 1.16, etc.
    public static int getMcVersion() {
        final Pattern p = Pattern.compile("^1\\.(\\d*)\\.");
        final Matcher m = p.matcher((Bukkit.getBukkitVersion()));
        int version = -1;
        while (m.find()) {
            if (NumberUtils.isNumber(m.group(1)))
                version = Integer.parseInt(m.group(1));
        }
        return version;
    }

    public Material getChestMaterial(final AngelChest chest) {
        if (!Daddy.allows(Features.GENERIC)) {
            return chestMaterial;
        }
        if (!getConfig().getBoolean(Config.USE_DIFFERENT_MATERIAL_WHEN_UNLOCKED)) {
            return chestMaterial;
        }
        return chest.isProtected ? chestMaterial : chestMaterialUnlocked;
    }

    public void debug(final String t) {
        if (debug) getLogger().info("[DEBUG] " + t);
    }

    public void verbose(final String t) {
        if (verbose) getLogger().info("[DEBUG] [VERBOSE] " + t);
    }

    @Override
    public void onLoad() {
        instance = this;
        /*try {
            Class.forName("com.sk89q.worldguard.protection.flags.registry.FlagConflictException");*/
            WorldGuardWrapper.tryToRegisterFlags();
        /*} catch (NoClassDefFoundError | ClassNotFoundException ignored) {

        }*/
    }
    @Override
    public void onEnable() {

        /*Daddy start*/
        Daddy.init(this);
        /*Daddy end*/
        JeffLib.init(this);

        migrateFromAngelChestPlus1X();
        ChestFileUpdater.updateChestFilesToNewDeathCause();
        if (getMcVersion() < 13) {
            EmergencyMode.severe(EmergencyMode.UNSUPPORTED_MC_VERSION_1_12);
            emergencyMode = true;
            return;
        }

        ConfigurationSerialization.registerClass(DeathCause.class);

        if (isAngelChestPlus1XInstalled()) {
            emergencyMode = true;
            EmergencyMode.severe(EmergencyMode.FREE_VERSION_INSTALLED);
            return;
        }

        watchdog = new Watchdog(this);

        metrics = new Metrics(this, BSTATS_ID);
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
        final CommandFetchOrTeleport commandFetchOrTeleport = new CommandFetchOrTeleport();
        final GenericTabCompleter genericTabCompleter = new GenericTabCompleter();
        Objects.requireNonNull(this.getCommand("acunlock")).setExecutor(new CommandUnlock());
        Objects.requireNonNull(this.getCommand("acunlock")).setTabCompleter(genericTabCompleter);
        Objects.requireNonNull(this.getCommand("aclist")).setExecutor(new CommandList());
        Objects.requireNonNull(this.getCommand("acfetch")).setExecutor(commandFetchOrTeleport);
        Objects.requireNonNull(this.getCommand("acfetch")).setTabCompleter(genericTabCompleter);
        Objects.requireNonNull(this.getCommand("actp")).setExecutor(commandFetchOrTeleport);
        Objects.requireNonNull(this.getCommand("actp")).setTabCompleter(genericTabCompleter);
        Objects.requireNonNull(this.getCommand("acreload")).setExecutor(new CommandReload());
        Objects.requireNonNull(this.getCommand("acgui")).setExecutor(new CommandGUI());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        final CommandDebug commandDebug = new CommandDebug();
        Objects.requireNonNull(this.getCommand("acdebug")).setExecutor(commandDebug);
        Objects.requireNonNull(this.getCommand("acdebug")).setTabCompleter(commandDebug);
        Objects.requireNonNull(this.getCommand("acversion")).setExecutor(new CommandVersion());


        debug("Registering listeners");
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new HologramListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new PistonListener(), this);
        getServer().getPluginManager().registerEvents(new EmergencyListener(), this);
        getServer().getPluginManager().registerEvents(new UpdateCheckListener(), this);
        guiListener = new GUIListener();
        getServer().getPluginManager().registerEvents(guiListener, this);


        if (debug) getLogger().info("Disabled Worlds: " + disabledWorlds.size());
        if (debug) getLogger().info("Disabled WorldGuard regions: " + disabledRegions.size());

        setEconomyStatus();

        final char color = Daddy.allows(Features.DONT_SHOW_NAG_MESSAGE) ? 'a' : '6';
        for (final String line : Daddy.allows(Features.DONT_SHOW_NAG_MESSAGE) ? Messages.usingPlusVersion : Messages.usingFreeVersion) {
            getLogger().info(ChatColor.translateAlternateColorCodes('&', "&" + color + line));
        }

        if (Daddy.allows(Features.GENERIC)) {
            DiscordVerificationUtils.createVerificationFile();
        }

    }

    private void migrateFromAngelChestPlus1X() {
        final File ACFolder = new File(getDataFolder().getAbsolutePath());
        final File ACPlusFolder = new File(ACFolder.getParentFile(), "AngelChestPlus");

        // ACPlus folder exists
        if (ACPlusFolder.isDirectory()) {
            getLogger().warning("You are upgrading from AngelChestPlus 1.XX to " + getDescription().getVersion());
            getLogger().warning("Trying to rename your AngelChestPlus folder to AngelChest...");

            // Shit: AC folder also exists
            if (ACFolder.isDirectory()) {
                getLogger().warning("Strange - you already have a directory called AngelChest. Trying to rename that one...");

                ACFolder.renameTo(new File(getDataFolder().getAbsolutePath(), "AngelChest-backup"));

                if (ACFolder.isDirectory()) {
                    getLogger().warning("It gets stranger: Couldn't rename that directory.");
                    getLogger().severe("Well, we couldn't remove your old AngelChest folder, which means we cannot apply your old config. Please manually rename the AngelChestPlus folder to AngelChest.");
                }
            }

            // AC folder does not (longer) exist
            if (!ACFolder.isDirectory()) {
                ACPlusFolder.renameTo(ACFolder);
            }

            if (!ACFolder.isDirectory()
                    || ACPlusFolder.isDirectory()) {
                getLogger().severe("Could not rename your AngelChestPlus folder to AngelChest. Please do so manually.");
            } else {
                getLogger().warning(ChatColor.GREEN + "Successfully upgraded from AngelChestPlus 1.XX to " + getDescription().getVersion());
            }

        }
    }

    private void setEconomyStatus() {
        final Plugin v = getServer().getPluginManager().getPlugin("Vault");

        if (v == null) {
            getLogger().info("Vault not installed, disabling economy functions.");
            economyStatus = EconomyStatus.INACTIVE;
            return;
        }

        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
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
        final String[][] commands = new String[][]{
                {"acgui", Permissions.USE},
                {"aclist", Permissions.USE},
                {"acfetch", Permissions.FETCH},
                {"actp", Permissions.TP},
                {"acunlock", Permissions.PROTECT},
                {"acreload", Permissions.RELOAD},
                {"acdebug", Permissions.DEBUG},
                {"acversion", Permissions.VERSION}
        };
        for (final String[] commandAndPermission : commands) {
            final ArrayList<String> command = new ArrayList<>();
            command.add(commandAndPermission[0]);
            final List<String> aliases = getConfig().getStringList("command-aliases-" + commandAndPermission[0]);
            command.addAll(aliases);
            CommandManager.registerCommand(commandAndPermission[1], command.toArray(new String[0]));
        }
    }

    private void scheduleRepeatingTasks() {
        // Rename holograms
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (final AngelChest chest : angelChests.values()) {
                if (chest != null && chest.hologram != null) {
                    chest.hologram.update(chest);
                }
            }
        }, Ticks.fromSeconds(1), Ticks.fromSeconds(1));

        // Track player positions
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (((Entity) player).isOnGround()) {
                    lastPlayerPositions.put(player.getUniqueId(), player.getLocation().getBlock());
                }
            }
        }, Ticks.fromSeconds(1), Ticks.fromSeconds(1));

        // Fix broken AngelChests
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

            // The following might only be needed for chests destroyed by end crystals spawning during the init phase of the ender dragon
            for (final Entry<Block, AngelChest> entry : angelChests.entrySet()) {

                if (!PaperLib.isChunkGenerated(entry.getKey().getLocation())) {
                    verbose("Chunk at " + entry.getKey().getLocation().toString() + " has not been generated!");
                }

                if (!entry.getKey().getWorld().isChunkLoaded(entry.getKey().getX() >> 4, entry.getKey().getZ() >> 4)) {

                    verbose("Chunk at " + entry.getKey().getLocation().toString() + " is not loaded, skipping repeating task regarding angelChests.entrySet()");
                    // CONTINUE IF CHUNK IS NOT LOADED

                    continue;
                }
				/*if(!isAngelChest(entry.getKey())) {
					entry.getValue().destroy();
					debug("Removing block from list because it's no AngelChest");
				}*/
                if (isBrokenAngelChest(entry.getKey(), entry.getValue())) {
                    final Block block = entry.getKey();
                    debug("Fixing broken AngelChest at " + block.getLocation());
                    entry.setValue(new AngelChest(Objects.requireNonNull(getAngelChest(block)).saveToFile(true)));
                }
            }
        }, 0L, Ticks.fromSeconds(2));

        // Schedule DurationTimer
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            final Iterator<AngelChest> it = angelChests.values().iterator();
            while (it.hasNext()) {
                final AngelChest ac = it.next();
                if (ac == null) continue;
                ac.secondsLeft--;
                if (ac.secondsLeft <= 0 && !ac.infinite) {
                    if (getServer().getPlayer(ac.owner) != null) {
                        Messages.send(getServer().getPlayer(ac.owner), messages.MSG_ANGELCHEST_DISAPPEARED);
                    }
                    ac.destroy(true);
                    it.remove();
                }
                if (Daddy.allows(Features.GENERIC) && ac.isProtected && ac.unlockIn != -1) { // Don't add feature here, gets called every second
                    ac.unlockIn--;
                    if (ac.unlockIn == -1) {
                        ac.isProtected = false;
                        ac.scheduleBlockChange();
                        if (getServer().getPlayer(ac.owner) != null) {
                            Messages.send(getServer().getPlayer(ac.owner), messages.MSG_UNLOCKED_AUTOMATICALLY);
                        }
                    }
                }
            }
        }, 0, Ticks.fromSeconds(1));

        // Remove dead holograms
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (final World world : Bukkit.getWorlds()) {
                HologramFixer.removeDeadHolograms(world);
            }
        }, Ticks.fromMinutes(1), Ticks.fromMinutes(1));
    }

    public void loadAllAngelChestsFromFile() {
        final File dir = new File(getDataFolder().getPath() + File.separator + "angelchests");
        final File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (final File child : directoryListing) {
                debug("Loading AngelChest " + child.getName());
                final AngelChest ac = new AngelChest(child);
                if (ac.success) {
                    angelChests.put(ac.block, ac);
                } else {
                    debug("Error while loading " + child.getName() + ", probably the world is not loaded yet. Will try again on next world load.");
                }
            }
        }

    }

    public void onDisable() {
        if (emergencyMode) return;

        saveAllAngelChestsToFile(true);

    }

    public void saveAllAngelChestsToFile(final boolean removeChests) {
        // Destroy all Angel Chests, including hologram AND CONTENTS!
        //for(Entry<Block,AngelChest> entry : angelChests.entrySet()) {
        //	Utils.destroyAngelChest(entry.getKey(), entry.getValue(), this);
        //}
        for (final Entry<Block, AngelChest> entry : angelChests.entrySet()) {
            entry.getValue().saveToFile(removeChests);

            // The following line isn't needed anymore but it doesn't hurt either
            if (removeChests) {
                entry.getValue().hologram.destroy();
            }
        }
    }

    public @Nullable String isItemBlacklisted(final ItemStack item) {
        if (!Daddy.allows(Features.GENERIC)) { // Don't add feature here, gets called for every item on death
            return null;
        }
        for (final BlacklistEntry entry : itemBlacklist.values()) {
            final BlacklistResult result = entry.matches(item);
            if (result == BlacklistResult.MATCH) {
                return result.getName();
            }
        }
        return null;
    }

    public boolean isAngelChest(final Block block) {
        return angelChests.containsKey(block);
    }

    public boolean isBrokenAngelChest(final Block block, final AngelChest chest) {
        return block.getType() != getChestMaterial(chest);
    }

    public boolean isAngelChestHologram(final Entity e) {
        // Skip this because it is checked in the listener and this method is not needed elsewhere
        //if(!(e instanceof ArmorStand)) return false;

        return getAllArmorStandUUIDs().contains(e.getUniqueId());
    }

    public @Nullable AngelChest getAngelChest(final Block block) {
        if (angelChests.containsKey(block)) {
            return angelChests.get(block);
        }
        return null;
    }

    public AngelChest getAngelChestByHologram(final ArmorStand armorStand) {
        for (final AngelChest angelChest : angelChests.values()) {
            if (angelChest == null) continue;
            if (angelChest.hologram == null) continue;
            if (angelChest.hologram.armorStandUUIDs.contains(armorStand.getUniqueId())) {
                return angelChest;
            }
        }
        return null;
    }

    public ArrayList<UUID> getAllArmorStandUUIDs() {
        final ArrayList<UUID> armorStandUUIDs = new ArrayList<>();
        for (final AngelChest ac : angelChests.values()) {
            if (ac == null || ac.hologram == null) continue;
            //if(armorStand == null) continue;
            armorStandUUIDs.addAll(ac.hologram.armorStandUUIDs);
        }
        return armorStandUUIDs;
    }

    public void initUpdateChecker() {
        if (updateChecker == null) {
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

        switch (getConfig().getString(Config.CHECK_FOR_UPDATES).toLowerCase()) {
            case "true":
                updateChecker.check((long) (getConfig().getDouble(Config.CHECK_FOR_UPDATES_INTERVAL) * 60 * 60), null);
                break;
            case "false":
                break;
            default:
                updateChecker.check(null);
        }
    }

    private boolean isAngelChestPlus1XInstalled() {
        return Bukkit.getPluginManager().getPlugin("AngelChestPlus") != null;
    }

    // SpigotJeffMediaPlugin interface
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

    // AngelChestPlugin interface
    @Override
    public Set<de.jeff_media.angelchest.AngelChest> getAllAngelChests() {
        final Set<de.jeff_media.angelchest.AngelChest> chests = new HashSet<>(angelChests.values());
		/*for(AngelChest chest : angelChests.values()) {
			chests.add(chest);
		}*/
        chests.stream().sorted(Comparator.comparingLong(de.jeff_media.angelchest.AngelChest::getCreated)).collect(Collectors.toList());
        return chests;
    }

    @Override
    public LinkedHashSet<de.jeff_media.angelchest.AngelChest> getAllAngelChestsFromPlayer(final OfflinePlayer player) {
        final LinkedHashSet<de.jeff_media.angelchest.AngelChest> set = new LinkedHashSet<>(AngelChestUtils.getAllAngelChestsFromPlayer(player));
		/*for(de.jeff_media.angelchest.AngelChest chest : AngelChestUtils.getAllAngelChestsFromPlayer(player)) {
			set.add(chest);
		}*/
        set.stream().sorted(Comparator.comparingLong(de.jeff_media.angelchest.AngelChest::getCreated)).collect(Collectors.toList());
        return set;
    }

    @Override
    public de.jeff_media.angelchest.AngelChest getAngelChestAtBlock(final Block block) {
        return getAngelChest(block);
    }


}
