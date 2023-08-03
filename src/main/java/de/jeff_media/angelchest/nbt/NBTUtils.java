package de.jeff_media.angelchest.nbt;

import de.jeff_media.angelchest.AngelChestMain;
import com.jeff_media.jefflib.NBTAPI;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public final class NBTUtils {

    private final AngelChestMain main;

    public NBTUtils() {
        this.main = AngelChestMain.getInstance();
    }

    public boolean isBrokenHologram(final ArmorStand armorStand) {
        if (!NBTAPI.hasNBT(armorStand, NBTTags.IS_HOLOGRAM)) return false;
        final UUID uuid = armorStand.getUniqueId();
        return !main.getAllArmorStandUUIDs().contains(uuid);
    }
}
