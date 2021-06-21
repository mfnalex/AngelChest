package de.jeff_media.angelchest.data;

import javax.annotation.Nullable;

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
    @Nullable
    public final Boolean allowTpAcrossWorlds;
    @Nullable
    public final Boolean allowFetchAcrossWorlds;
    @Nullable
    public final Integer maxTpDistance;
    @Nullable
    public final Integer maxFetchDistance;

    public Group(final int duration, final int maxChests, final String priceSpawn, final String priceOpen, final String priceTeleport, final String priceFetch, final double xpPercentage, final int unlockDuration, final double spawnChance, final String itemLoss, final int invulnerabilityAfterTP, @Nullable final Boolean allowTpAcrossWorlds, @Nullable final Boolean allowFetchAcrossWorlds, @Nullable final Integer maxTpDistance, @Nullable final Integer maxFetchDistance) {
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
        this.allowTpAcrossWorlds = allowTpAcrossWorlds;
        this.allowFetchAcrossWorlds = allowFetchAcrossWorlds;
        this.maxTpDistance = maxTpDistance;
        this.maxFetchDistance = maxFetchDistance;
    }

    @Override
    public String toString() {
        return "Group{" +
                "duration=" + duration +
                ", invulnerabilityAfterTP=" + invulnerabilityAfterTP +
                ", itemLoss='" + itemLoss + '\'' +
                ", maxChests=" + maxChests +
                ", priceFetch='" + priceFetch + '\'' +
                ", priceOpen='" + priceOpen + '\'' +
                ", priceSpawn='" + priceSpawn + '\'' +
                ", priceTeleport='" + priceTeleport + '\'' +
                ", spawnChance=" + spawnChance +
                ", unlockDuration=" + unlockDuration +
                ", xpPercentage=" + xpPercentage +
                ", allowTpAcrossWorlds=" + allowTpAcrossWorlds +
                ", allowFetchAcrossWorlds=" + allowFetchAcrossWorlds +
                ", maxTpDistance=" + maxTpDistance +
                ", maxFetchDistance=" + maxFetchDistance +
                '}';
    }
}
