package de.jeff_media.angelchest.nbt;

import de.jeff_media.angelchest.Main;
import de.jeff_media.jefflib.NBTAPI;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public final class NBTUtils {

    private final Main main;

    public NBTUtils() {
        this.main = Main.getInstance();
    }

    public boolean isBrokenHologram(final ArmorStand armorStand) {
        if (!NBTAPI.hasNBT(armorStand, NBTTags.IS_HOLOGRAM)) return false;
        final UUID uuid = armorStand.getUniqueId();
        return !main.getAllArmorStandUUIDs().contains(uuid);
    }
}
