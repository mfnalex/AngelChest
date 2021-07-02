package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.data.Graveyard;
import org.bukkit.entity.Player;

// TODO: Use this more often.
public class Prompt {

    public static Boolean getBoolean(Player player, String arg) {
        if(arg.equalsIgnoreCase("true")) return true;
        if(arg.equalsIgnoreCase("false")) return false;
        player.sendMessage("§cYou must specify either true or false.");
        return null;
    }

    public static Integer getPositiveInteger(Player player, String arg) {
        Integer value;
        try {
            value = Integer.parseInt(arg);
            if (value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (Exception exception) {
            player.sendMessage("§c" + arg + " is not a valid positive integer.");
            return null;
        }
    }

    public static void showSuccess(Player player, String key, String value) {
        player.sendMessage("§aSet §b"+key+"§a to §b"+value);
    }

    public static void showSuccess(Player player, String key, String value, Graveyard yard) {
        player.sendMessage("§aSet §b"+key+"§a to §b"+value+"§a in §b" + yard.getName());
    }
}
