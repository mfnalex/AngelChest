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
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.gui.GUIListener;
import de.jeff_media.angelchest.gui.GUIManager;
import de.jeff_media.angelchest.hooks.GenericHooks;
import de.jeff_media.angelchest.hooks.MinepacksHook;
import de.jeff_media.angelchest.hooks.PlaceholderAPIHook;
import de.jeff_media.angelchest.hooks.WorldGuardWrapper;
import de.jeff_media.angelchest.listeners.*;
import de.jeff_media.angelchest.nbt.NBTUtils;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.JeffLib;
import de.jeff_media.jefflib.Ticks;
import de.jeff_media.jefflib.VersionUtil;
import de.jeff_media.jefflib.thirdparty.io.papermc.paperlib.PaperLib;
import de.jeff_media.updatechecker.UpdateChecker;
import de.jeff_media.updatechecker.UserAgentBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import java.util.stream.Collectors;

public final class Main extends JavaPlugin implements SpigotJeffMediaPlugin, AngelChestPlugin {

    public static final int BSTATS_ID = 3194;
    public static final String DISCORD_LINK = "https://discord.jeff-media.de";
    public static final String UPDATECHECKER_LINK_DONATE = "https://paypal.me/mfnalex";
    public static final UUID consoleSenderUUID = UUID.randomUUID();
    private static final String SPIGOT_RESOURCE_ID_FREE = "60383";
    public static final String UPDATECHECKER_LINK_DOWNLOAD_FREE = "https://www.spigotmc.org/resources/" + SPIGOT_RESOURCE_ID_FREE;
    private static final String SPIGOT_RESOURCE_ID_PLUS = "88214";
    public static final String UPDATECHECKER_LINK_DOWNLOAD_PLUS = "https://www.spigotmc.org/resources/" + SPIGOT_RESOURCE_ID_PLUS;
    public static final String UPDATECHECKER_LINK_CHANGELOG = "https://www.spigotmc.org/resources/" + SPIGOT_RESOURCE_ID_PLUS + "/updates";
    private static final String UPDATECHECKER_LINK_API = "https://api.jeff-media.de/angelchestplus/latest-version.txt";
    private static Main instance;
    private static WorldGuardWrapper worldGuardWrapper;
    public LinkedHashMap<Block, AngelChest> angelChests;
    public Material chestMaterial;
    public Material chestMaterialUnlocked;
    public boolean debug = false;
    //public java.util.logging.Logger debugLogger;
    public boolean disableDeathEvent = false;
    public List<String> disabledMaterials;
    public List<String> disabledRegions;
    public List<String> disabledWorlds;
    public List<Material> dontSpawnOn;
    public Economy econ;
    public EconomyStatus economyStatus = EconomyStatus.UNKNOWN;
    public GenericHooks genericHooks;
    public GroupUtils groupUtils;
    public ProtectionUtils protectionUtils;
    public GUIListener guiListener;
    public GUIManager guiManager;
    public String[] invalidConfigFiles;
    public HashMap<UUID, Integer> invulnerableTasks;
    public Map<String, BlacklistEntry> itemBlacklist;
    public HashMap<UUID, Entity> killers;
    public HashMap<UUID, Block> lastPlayerPositions;
    public Logger logger;
    public Messages messages;
    public Metrics metrics;
    public MinepacksHook minepacksHook;
    public NBTUtils nbtUtils;
    public List<Material> onlySpawnIn;
    public HashMap<UUID, PendingConfirm> pendingConfirms;
    public boolean verbose = false;
    public Watchdog watchdog;
    boolean emergencyMode = false;
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private String NONCE = "%%__NONCE__%%";
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private String RESOURCE = "%%__RESOURCE__%%";
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal"})
    private String UID = "%%__USER__%%";

    public static Main getInstance() {
        return instance;
    }

    public static WorldGuardWrapper getWorldGuardWrapper() {
        // We have to do this because softdepend doesn't assure that AngelChest enables after WorldGuard
        if (worldGuardWrapper == null) {
            worldGuardWrapper = WorldGuardWrapper.init();
        }
        return worldGuardWrapper;
    }

    public void debug(final String... text) {
        if (debug) {
            for (String line : text) {
                getLogger().info("[DEBUG] " + line);
            }
        }
        /*for (String line : text) {
            debugLogger.info(line);
        }*/
    }

    // AngelChestPlugin interface
    @Override
    public Set<de.jeff_media.angelchest.AngelChest> getAllAngelChests() {
        final Set<de.jeff_media.angelchest.AngelChest> chests = new HashSet<>(angelChests.values());
        chests.stream().sorted(Comparator.comparingLong(de.jeff_media.angelchest.AngelChest::getCreated)).collect(Collectors.toList());
        return chests;
    }

    @Override
    public LinkedHashSet<de.jeff_media.angelchest.AngelChest> getAllAngelChestsFromPlayer(final OfflinePlayer player) {
        final LinkedHashSet<de.jeff_media.angelchest.AngelChest> set = new LinkedHashSet<>(AngelChestUtils.getAllAngelChestsFromPlayer(player));
        set.stream().sorted(Comparator.comparingLong(de.jeff_media.angelchest.AngelChest::getCreated)).collect(Collectors.toList());
        return set;
    }

    public ArrayList<UUID> getAllArmorStandUUIDs() {
        final ArrayList<UUID> armorStandUUIDs = new ArrayList<>();
        for (final AngelChest ac : angelChests.values()) {
            if (ac == null || ac.hologram == null) continue;
            armorStandUUIDs.addAll(ac.hologram.armorStandUUIDs);
        }
        return armorStandUUIDs;
    }

    public @Nullable AngelChest getAngelChest(final Block block) {
        if (angelChests.containsKey(block)) {
            return angelChests.get(block);
        }
        return null;
    }

    @Override
    public de.jeff_media.angelchest.AngelChest getAngelChestAtBlock(final Block block) {
        return getAngelChest(block);
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

    public Material getChestMaterial(final AngelChest chest) {
        if (!Daddy.allows(PremiumFeatures.GENERIC)) {
            return chestMaterial;
        }
        if (!getConfig().getBoolean(Config.USE_DIFFERENT_MATERIAL_WHEN_UNLOCKED)) {
            return chestMaterial;
        }
        return chest.isProtected ? chestMaterial : chestMaterialUnlocked;
    }

    @Override
    public String getNONCE() {
        return NONCE;
    }

    @Override
    public String getRESOURCE() {
        return RESOURCE;
    }

    @Override
    public String getUID() {
        return UID;
    }

    public void initUpdateChecker() {
        UpdateChecker.init(this, UPDATECHECKER_LINK_API)
                .setDonationLink(UPDATECHECKER_LINK_DONATE)
                .setChangelogLink(UPDATECHECKER_LINK_CHANGELOG)
                .setPaidDownloadLink(UPDATECHECKER_LINK_DOWNLOAD_PLUS)
                .setFreeDownloadLink(UPDATECHECKER_LINK_DOWNLOAD_FREE)
                .setColoredConsoleOutput(true)
                .setUserAgent(UserAgentBuilder.getDefaultUserAgent().addUsingPaidVersion().addSpigotUserId())
                .setNamePaidVersion("Plus")
                .setNameFreeVersion("Free")
                .setNotifyRequesters(true)
                .setNotifyOpsOnJoin(true)
                .setTimeout(10000);


        switch (getConfig().getString(Config.CHECK_FOR_UPDATES).toLowerCase()) {
            case "true":
                UpdateChecker.getInstance()
                        .checkEveryXHours(getConfig().getDouble(Config.CHECK_FOR_UPDATES_INTERVAL))
                        .checkNow();
                break;
            case "false":
                break;
            default:
                UpdateChecker.getInstance().checkNow();
        }
    }

    public boolean isAngelChest(final Block block) {
        return angelChests.containsKey(block);
    }

    public boolean isAngelChestHologram(final Entity e) {
        // Skip this because it is checked in the listener and this method is not needed elsewhere
        //if(!(e instanceof ArmorStand)) return false;

        return getAllArmorStandUUIDs().contains(e.getUniqueId());
    }

    private boolean isAngelChestPlus1XInstalled() {
        return Bukkit.getPluginManager().getPlugin("AngelChestPlus") != null;
    }

    public boolean isBrokenAngelChest(final Block block, final AngelChest chest) {
        if (isOutsideOfNormalWorld(block)) return false;
        return block.getType() != getChestMaterial(chest);
    }

    public @Nullable String isItemBlacklisted(final ItemStack item) {
        if (!Daddy.allows(PremiumFeatures.GENERIC)) { // Don't add feature here, gets called for every item on death
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

    public boolean isOutsideOfNormalWorld(final Block block) {
        return block.getY() < 0 || block.getY() >= block.getWorld().getMaxHeight();
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

            if (!ACFolder.isDirectory() || ACPlusFolder.isDirectory()) {
                getLogger().severe("Could not rename your AngelChestPlus folder to AngelChest. Please do so manually.");
            } else {
                getLogger().warning(ChatColor.GREEN + "Successfully upgraded from AngelChestPlus 1.XX to " + getDescription().getVersion());
            }

        }
    }

    public void onDisable() {
        if (emergencyMode) return;

        saveAllAngelChestsToFile(true);

    }

    @Override
    public void onEnable() {

        /*Daddy start*/
        Daddy.init(this);
        /*Daddy end*/
        JeffLib.init(this);

        migrateFromAngelChestPlus1X();
        ChestFileUpdater.updateChestFilesToNewDeathCause();
        if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_13_2_R01)) {
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
        invulnerableTasks = new HashMap<>();
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
        Objects.requireNonNull(this.getCommand("actoggle")).setExecutor(new CommandToggle());

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
        getServer().getPluginManager().registerEvents(new ChestProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new PistonListener(), this);
        getServer().getPluginManager().registerEvents(new EmergencyListener(), this);
        //getServer().getPluginManager().registerEvents(new UpdateCheckListener(), this);
        getServer().getPluginManager().registerEvents(new InvulnerabilityListener(), this);
        guiListener = new GUIListener();
        getServer().getPluginManager().registerEvents(guiListener, this);


        if (debug) getLogger().info("Disabled Worlds: " + disabledWorlds.size());
        if (debug) getLogger().info("Disabled WorldGuard regions: " + disabledRegions.size());

        setEconomyStatus();

        final char color = Daddy.allows(PremiumFeatures.DONT_SHOW_NAG_MESSAGE) ? 'a' : '6';
        for (final String line : Daddy.allows(PremiumFeatures.DONT_SHOW_NAG_MESSAGE) ? Messages.usingPlusVersion : Messages.usingFreeVersion) {
            getLogger().info(ChatColor.translateAlternateColorCodes('&', "&" + color + line));
        }

        if (Daddy.allows(PremiumFeatures.GENERIC)) {
            DiscordVerificationUtils.createVerificationFile();
        }

    }

    @Override
    public void onLoad() {
        instance = this;

        /*debugLogger = java.util.logging.Logger.getLogger("AngelChest Debug");
        try {
            FileHandler fileHandler = new FileHandler(getDataFolder() + File.separator + "debug.log");
            fileHandler.setFormatter(new DebugFormatter());
            debugLogger.addHandler(fileHandler);
            debugLogger.setUseParentHandlers(false);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }*/

        WorldGuardWrapper.tryToRegisterFlags();
    }

    private void registerCommands() {
        final String[][] commands = new String[][] {{"acgui", Permissions.USE}, {"aclist", Permissions.USE}, {"acfetch", Permissions.FETCH}, {"actp", Permissions.TP}, {"acunlock", Permissions.PROTECT}, {"acreload", Permissions.RELOAD}, {"acdebug", Permissions.DEBUG}, {"acversion", Permissions.VERSION}, {"actoggle", Permissions.TOGGLE}};
        for (final String[] commandAndPermission : commands) {
            final ArrayList<String> command = new ArrayList<>();
            command.add(commandAndPermission[0]);
            final List<String> aliases = getConfig().getStringList("command-aliases-" + commandAndPermission[0]);
            command.addAll(aliases);
            CommandManager.registerCommand(commandAndPermission[1], command.toArray(new String[0]));
        }
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

    private void scheduleRepeatingTasks() {

        // Track player positions
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::trackPlayerPositions, Ticks.fromSeconds(1), Ticks.fromSeconds(1));

        // Fix broken AngelChests
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::fixBrokenAngelChests, 0L, Ticks.fromSeconds(2));

        // Holograms, Durations
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::updateAngelChests, 0, Ticks.fromSeconds(1));

        // Remove dead holograms
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::removeDeadHolograms, Ticks.fromMinutes(1), Ticks.fromMinutes(1));
    }

    private void removeDeadHolograms() {
        for (final World world : Bukkit.getWorlds()) {
            HologramFixer.removeDeadHolograms(world);
        }
    }

    private void updateAngelChests() {
        final Iterator<AngelChest> it = angelChests.values().iterator();
        while (it.hasNext()) {
            final AngelChest ac = it.next();
            if (ac == null) continue;
            ac.secondsLeft--;
            if (ac.secondsLeft < 0 && !ac.infinite) {
                if (getServer().getPlayer(ac.owner) != null) {
                    Messages.send(getServer().getPlayer(ac.owner), messages.MSG_ANGELCHEST_DISAPPEARED);
                }
                ac.destroy(true);
                it.remove();
                continue;
            }
            if (Daddy.allows(PremiumFeatures.GENERIC) && ac.isProtected && ac.unlockIn > -1) { // Don't add feature here, gets called every second
                ac.unlockIn--;
                if (ac.unlockIn == -1) {
                    ac.isProtected = false;
                    ac.scheduleBlockChange();
                    if (getServer().getPlayer(ac.owner) != null) {
                        Messages.send(getServer().getPlayer(ac.owner), messages.MSG_UNLOCKED_AUTOMATICALLY);
                    }
                }
            }
            if (ac.hologram != null) {
                ac.hologram.update(ac);
            }
        }
    }

    private void fixBrokenAngelChests() {
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
            if (isBrokenAngelChest(entry.getKey(), entry.getValue())) {
                final Block block = entry.getKey();
                debug("Fixing broken AngelChest at " + block.getLocation());
                entry.setValue(new AngelChest(Objects.requireNonNull(getAngelChest(block)).saveToFile(true)));
            }
        }
    }

    private void trackPlayerPositions() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (((Entity) player).isOnGround()) {
                if(getConfig().getBoolean(Config.LAVA_DETECTION) == false || (player.getEyeLocation().getBlock().getType() != Material.LAVA && player.getEyeLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.LAVA)) {
                    lastPlayerPositions.put(player.getUniqueId(), player.getLocation().getBlock());
                }
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

    public void verbose(final String t) {
        if (verbose) getLogger().info("[DEBUG] [VERBOSE] " + t);
    }


}
