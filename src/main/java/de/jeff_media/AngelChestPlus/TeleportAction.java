package de.jeff_media.AngelChestPlus;

import org.bukkit.entity.Player;

public enum TeleportAction {

    TELEPORT_TO_CHEST("angelchest.tp", "AngelChest TP","actp"),
    FETCH_CHEST("angelchest.fetch", "AngelChest Fetch","acfetch");

    private final String permission;
    private final String economyReason;
    private final String command;

    TeleportAction(String permission, String economyReason, String command) {
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

    public double getPrice(Main main, Player player) {
        //return main.getConfig().getDouble(priceInConfig);
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
