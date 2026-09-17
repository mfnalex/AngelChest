package com.jeff_media.jefflib;

import org.bukkit.World;

/** World helpers required by AngelChest. */
public final class WorldUtils {

    private WorldUtils() {
    }

    public static int getWorldMinHeight(final World world) {
        return world.getMinHeight();
    }
}
