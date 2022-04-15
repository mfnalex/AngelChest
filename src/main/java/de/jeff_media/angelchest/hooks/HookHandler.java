package de.jeff_media.angelchest.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HookHandler {

    private static final boolean worldBorderApiInstalled;

    static {
        worldBorderApiInstalled = Bukkit.getPluginManager().getPlugin("WorldBorderAPI") != null;
    }

    public static boolean isInsideWorldBorder(Location loc, Player player) {
        if(!worldBorderApiInstalled) return true;
        try {
            return WorldBorderApiHook.isWithinWorldWorder(loc, player);
        } catch (Throwable throwable) {
            return true;
        }
    }

}
