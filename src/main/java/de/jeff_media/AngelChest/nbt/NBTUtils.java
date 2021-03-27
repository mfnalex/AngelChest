package de.jeff_media.AngelChest.nbt;

import de.jeff_media.AngelChest.Main;
import de.jeff_media.nbtapi.NBTAPI;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class NBTUtils {

    private Main main;

    public NBTUtils() {
        this.main= Main.getInstance();
    }

    public boolean isBrokenHologram(ArmorStand armorStand) {
        if(!NBTAPI.hasNBT(armorStand,NBTTags.IS_HOLOGRAM)) return false;
        UUID uuid = armorStand.getUniqueId();
        return !main.getAllArmorStandUUIDs().contains(uuid);
    }
}
