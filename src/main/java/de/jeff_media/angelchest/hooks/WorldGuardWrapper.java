package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Used to catch NoClassDefFound etc. when WorldGuard is not installed
 */
public class WorldGuardWrapper {

    /**
     * When WorldGuard is installed and is a supported version, it returns a WorldGuardHandler, otherwise a WorldGuardWrapper
     * @return WorldGuardHandler or WorldGuardWrapper
     */
    public static WorldGuardWrapper init() {
        WorldGuardWrapper handler;
        try {
            handler = new WorldGuardHandler(Main.getInstance());
        } catch (final Throwable t) {
            handler = new WorldGuardWrapper();
        }
        return handler;
    }

    /**
     * Flags have to be registered because a WorldGuard is available in case of circular soft-dependencies
     */
    public static void tryToRegisterFlags() {
        try {
            WorldGuardHandler.tryToRegisterFlags();
        } catch (final Throwable ignored) {
            //t.printStackTrace();
        }
    }

    public boolean getAngelChestFlag(final Player player) {
        return true;
    }

    public boolean isBlacklisted(final Block block) {
        return false;
    }
}
