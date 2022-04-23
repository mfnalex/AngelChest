package de.jeff_media.angelchest.hooks;

import org.bukkit.entity.Player;

public class SentinelHook {

    public static boolean isNpc(Player player) {
        return player.getClass().getName().contains("NPC");
    }

}
