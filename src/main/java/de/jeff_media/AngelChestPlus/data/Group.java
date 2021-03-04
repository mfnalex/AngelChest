package de.jeff_media.AngelChestPlus.data;

public class Group {
    public final int duration;
    public final int maxChests;
    public final double priceSpawn;
    public final double priceOpen;
    public final double priceTeleport;
    public final double priceFetch;
    public final double xpPercentage;
    public final int unlockDuration;

    public Group(int duration, int maxChests, double priceSpawn, double priceOpen, double priceTeleport, double priceFetch, double xpPercentage, int unlockDuration) {

        this.duration = duration;
        this.maxChests = maxChests;
        this.priceSpawn=priceSpawn;
        this.priceOpen = priceOpen;
        this.priceTeleport=priceTeleport;
        this.priceFetch=priceFetch;
        this.xpPercentage = xpPercentage;
        this.unlockDuration=unlockDuration;
    }
}
