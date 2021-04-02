package de.jeff_media.angelchest.enums;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import org.bukkit.command.CommandSender;

/**
 * Represents a command related action (e.g. List, TP, Fetch, ...)
 */
public enum CommandAction {

    TELEPORT_TO_CHEST(Permissions.TP, "AngelChest TP","actp"),
    FETCH_CHEST(Permissions.FETCH, "AngelChest Fetch","acfetch"),
    UNLOCK_CHEST(Permissions.PROTECT,"","acunlock"),
    LIST_CHESTS(Permissions.USE,"","aclist");

    private final String permission;
    private final String economyReason;
    private final String command;

    CommandAction(String permission, String economyReason, String command) {
        this.permission = permission;
        this.economyReason = economyReason;
        this.command = command;
    }

    public String getPermission() {
        return permission;
    }

    public String getEconomyReason() {
        return economyReason;
    }

    public double getPrice(CommandSender player) {
        Main main = Main.getInstance();
        if(this == TELEPORT_TO_CHEST) {
            return main.groupUtils.getTeleportPricePerPlayer(player);
        } else if(this == FETCH_CHEST) {
            return main.groupUtils.getFetchPricePerPlayer(player);
        } else {
            return 0.0D;
        }
    }

    public String getCommand() {
        return command;
    }

}
