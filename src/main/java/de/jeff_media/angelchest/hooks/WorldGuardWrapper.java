package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Used to catch NoClassDefFound etc. stuff because the WorldGuardAPI
 */
public class WorldGuardWrapper {

    public static WorldGuardWrapper init() {
        WorldGuardWrapper handler;
        try {
            handler = new WorldGuardHandler(Main.getInstance());
        } catch (final Throwable t) {
            handler = new WorldGuardWrapper();
        }
        return handler;
    }

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
