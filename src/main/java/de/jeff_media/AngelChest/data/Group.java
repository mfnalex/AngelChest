package de.jeff_media.AngelChest.data;

public final class Group {
    public final int duration;
    public final int maxChests;
    public final String priceSpawn;
    public final String priceOpen;
    public final String priceTeleport;
    public final String priceFetch;
    public final double xpPercentage;
    public final int unlockDuration;
    public final double spawnChance;
  
    public Group(int duration,
                 int maxChests,
                 String priceSpawn,
                 String priceOpen,
                 String priceTeleport,
                 String priceFetch,
                 double xpPercentage,
                 int unlockDuration,
                 double spawnChance) {
        this.duration = duration;
        this.maxChests = maxChests;
        this.priceSpawn=priceSpawn;
        this.priceOpen = priceOpen;
        this.priceTeleport=priceTeleport;
        this.priceFetch=priceFetch;
        this.xpPercentage = xpPercentage;
        this.unlockDuration=unlockDuration;
        this.spawnChance = spawnChance;
    }
}
