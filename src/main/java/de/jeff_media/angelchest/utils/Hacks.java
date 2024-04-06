package de.jeff_media.angelchest.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Hacks {

    public static Player getPlayer(Player player) {
        if(Bukkit.getPlayerExact(player.getName()) != null) {
            return Bukkit.getPlayerExact(player.getName());
        } else {
            return player;
        }
    }

    public static Player getPlayer(UUID uuid) {
        Player uuidPlayer = Bukkit.getPlayer(uuid);
        if(uuidPlayer != null) {
            return uuidPlayer;
        } else {
            return null;
        }
    }

}
