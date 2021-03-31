package de.jeff_media.angelchest.nbt;

import de.jeff_media.angelchest.Main;
import de.jeff_media.nbtapi.NBTAPI;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public final class NBTUtils {

    private final Main main;

    public NBTUtils() {
        this.main= Main.getInstance();
    }

    public boolean isBrokenHologram(ArmorStand armorStand) {
        if(!NBTAPI.hasNBT(armorStand,NBTTags.IS_HOLOGRAM)) return false;
        UUID uuid = armorStand.getUniqueId();
        return !main.getAllArmorStandUUIDs().contains(uuid);
    }
}
