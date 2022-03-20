package de.jeff_media.angelchest.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Hooks into WorldGuard 7+. If this fails, it tries to use the WorldGuardLegacyHandler for older versions.
 */
public final class WorldGuardHandler extends WorldGuardWrapper {

    public static StateFlag FLAG_ALLOW_ANGELCHEST = null;
    private static final Main main = Main.getInstance();
    public boolean disabled = false;
    RegionContainer regionContainer;
    WorldGuardPlugin worldGuardPlugin;

    public WorldGuardHandler(final Main main) {

        if (main.getConfig().getBoolean(Config.DISABLE_WORLDGUARD_INTEGRATION)) {
            disabled = true;
            main.getLogger().info("WorldGuard integration has been disabled in the config.yml.");
            return;
        }

        if (main.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            if (main.debug) main.debug("WorldGuard is not installed at all.");
            disabled = true;
            return;
        }

        main.getLogger().info("Trying to hook into WorldGuard...");

        try {
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin").getMethod("inst");
            worldGuardPlugin = WorldGuardPlugin.inst();
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            main.getLogger().severe("You are using a version of WorldGuard that does not implement all required API methods. You must use at least WorldGuard 7.0.0! WorldGuard integration is disabled.");
            if (main.debug) {
                e.printStackTrace();
            }
            disabled = true;
            return;
        }

        // Getting here means WorldGuard is installed

        if (worldGuardPlugin != null) {
            try {
                // This only works on WorldGuard 7+
                regionContainer = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(WorldGuard.getInstance(), "WorldGuard#getInstance is null").getPlatform(), "WorldGuard#getInstance#getPlatform is null").getRegionContainer(), "WorldGuard#getInstance#getRegionContainer is null");
                main.getLogger().info("Successfully hooked into WorldGuard.");
            } catch (final Throwable ignored) {
                disabled = true;
                main.getLogger().severe("You are using a version of WorldGuard that does not fully support your Minecraft version. WorldGuard integration is disabled.");
            }
        }
    }

    public static void tryToRegisterGraveyardFlag() {
        StringFlag graveyardFlag = new StringFlag("angelchest-graveyard");

    }

    public static void tryToRegisterFlags() {

        final Main main = Main.getInstance();
        //if(main.debug) main.debug("Trying to register WorldGuard Flags");

        // Check if WorldGuard is installed AND IF ITS A SUPPORTED VERSION (7+)
        if (main.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            //if(main.debug) main.debug("Could not register flags: WorldGuard not installed.");
            return;
        }
        try {
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin").getMethod("inst");
        } catch (final Exception | Error e) {
            main.getLogger().warning("Could not register WorldGuard flags although WorldGuard is installed.");
            e.printStackTrace();
            return;
        }

        // Flags start
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            final StateFlag allowFlag = new StateFlag("allow-angelchest", true);
            registry.register(allowFlag);
            FLAG_ALLOW_ANGELCHEST = allowFlag;
        } catch (final Exception weDontUseflagConflictExceptionBecauseItThrowsNoClassDefFoundErrorWhenWorldGuardIsNotInstalled) {
            final Flag<?> existing = registry.get("allow-angelchest");
            if (existing instanceof StateFlag) {
                FLAG_ALLOW_ANGELCHEST = (StateFlag) existing;
            } else {
                main.getLogger().warning("Could not register WorldGuard flag \"allow-angelchest\"");
            }
        }
        main.getLogger().info("Successfully registered WorldGuard flags.");
        // Flags end
    }

    @Override
    public boolean getAngelChestFlag(final Player player) {
        if (disabled) return true;
        if (worldGuardPlugin == null) return true;
        if (FLAG_ALLOW_ANGELCHEST == null) return true;
        final Block block = player.getLocation().getBlock();
        final RegionManager regions = regionContainer.get(BukkitAdapter.adapt(block.getWorld()));
        final BlockVector3 position = BlockVector3.at(block.getX(), block.getY(), block.getZ());
        final ApplicableRegionSet set = regions.getApplicableRegions(position);
        final LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);
        final boolean allow = set.testState(localPlayer, FLAG_ALLOW_ANGELCHEST);
        if (allow) {
            return true;
        } else {
            if (!Main.isPremiumVersion) {
                main.getLogger().warning("You are using AngelChest's WorldGuard flags, which are only available in AngelChestPlus. See here: " + Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
                return true;
            }
            return false;
        }
    }

    BlockVector3 getBlockVector3(final Block block) {
        return BlockVector3.at(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Checks whether this block is inside one of the disabled WorldGuard Regions.
     *
     * @param block Block to check
     * @return true if the block is inside a blacklisted region, otherwise false
     */
    @Override
    public boolean isBlacklisted(final Block block) {
        if (disabled) return false;
        if (worldGuardPlugin == null) return false;
        if (main.disabledRegions == null || main.disabledRegions.isEmpty()) return false;

        final RegionManager regions = regionContainer.get(BukkitAdapter.adapt(block.getWorld()));
        final List<String> regionList = regions.getApplicableRegionsIDs(getBlockVector3(block));

        if (main.debug) main.debug("Checking Regions in WG7+");

        for (final String r : regionList) {
            if (main.debug) main.debug("Player died in region " + r);
            if (main.disabledRegions.contains(r)) {
                if (main.debug) main.debug("Preventing AngelChest from spawning in disabled worldguard region");
                return true;
            }
        }
        return false;
    }
}
