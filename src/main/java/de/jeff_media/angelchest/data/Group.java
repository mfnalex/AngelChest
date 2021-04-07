package de.jeff_media.angelchest.data;

public final class Group {
    public final int duration;
    public final int invulnerabilityAfterTP;
    public final String itemLoss;
    public final int maxChests;
    public final String priceFetch;
    public final String priceOpen;
    public final String priceSpawn;
    public final String priceTeleport;
    public final double spawnChance;
    public final int unlockDuration;
    public final double xpPercentage;

    public Group(final int duration, final int maxChests, final String priceSpawn, final String priceOpen, final String priceTeleport, final String priceFetch, final double xpPercentage, final int unlockDuration, final double spawnChance, final String itemLoss, final int invulnerabilityAfterTP) {
        this.duration = duration;
        this.maxChests = maxChests;
        this.priceSpawn = priceSpawn;
        this.priceOpen = priceOpen;
        this.priceTeleport = priceTeleport;
        this.priceFetch = priceFetch;
        this.xpPercentage = xpPercentage;
        this.unlockDuration = unlockDuration;
        this.spawnChance = spawnChance;
        this.itemLoss = itemLoss;
        this.invulnerabilityAfterTP = invulnerabilityAfterTP;
    }
}
