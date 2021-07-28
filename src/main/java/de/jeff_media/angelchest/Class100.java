package de.jeff_media.angelchest;

import net.minecraft.world.entity.EntityAreaEffectCloud;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;

/**
 * This is the 100th class added to the original AngelChest source code, NOT including all the shaded libraries etc
 *
 * It's useless and has been added on 30th June 2021 at 12:50 CEST
 *
 * The total number of files contained in AngelChest.jar at this time was 1923, of which 1633 were .class files.
 */
public class Class100 {

    public static void test(Location location) {
        AreaEffectCloud cloud = (AreaEffectCloud) location.getWorld().spawnEntity(location,EntityType.AREA_EFFECT_CLOUD);
        cloud.setDuration(Integer.MAX_VALUE);
        cloud.setCustomName("test");
        cloud.setCustomNameVisible(true);
        cloud.setReapplicationDelay(Integer.MAX_VALUE);
        cloud.setPersistent(true);
        System.out.println("Spawned AreaEffectCloud");
    }
}
