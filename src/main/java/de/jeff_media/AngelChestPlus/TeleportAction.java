package de.jeff_media.AngelChestPlus;

public enum TeleportAction {

    TELEPORT_TO_CHEST("angelchest.tp", "AngelChest TP", Config.PRICE_TELEPORT,"actp"),
    FETCH_CHEST("angelchest.fetch", "AngelChest Fetch", Config.PRICE_FETCH,"acfetch");

    private final String permission;
    private final String economyReason;
    private final String priceInConfig;
    private final String command;

    TeleportAction(String permission, String economyReason, String priceInConfig, String command) {
        this.permission = permission;
        this.economyReason = economyReason;
        this.priceInConfig = priceInConfig;
        this.command = command;
    }

    public String getPermission() {
        return permission;
    }

    public String getEconomyReason() {
        return economyReason;
    }

    public double getPrice(Main main) {
        return main.getConfig().getDouble(priceInConfig);
    }

    public String getCommand() {
        return command;
    }

}
