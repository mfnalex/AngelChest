package de.jeff_media.AngelChest.utils;

import de.jeff_media.AngelChest.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class HologramFixer {

    public static int removeDeadHolograms(World world) {

        Main main = Main.getInstance();

        Collection armorStands;
        /*if(radius != null) {
            armorStands = location.getWorld().getNearbyEntities(location, radius, radius, radius, new Predicate<Entity>() {
                @Override
                public boolean test(Entity entity) {
                    return entity.getType() == EntityType.ARMOR_STAND;
                }
            });
        } else {*/
        armorStands = world.getEntitiesByClass(ArmorStand.class);
        //}

        Set<Pair<Integer,Integer>> deadLocations = new HashSet<>();
        Iterator<Entity> it = armorStands.iterator();
        while(it.hasNext()) {
            Entity entity = it.next();
            if(main.nbtUtils.isBrokenHologram((ArmorStand) entity)) {
                //deadHolograms++;
                Pair<Integer,Integer> deadLocation = new Pair<>(entity.getLocation().getBlockX(),entity.getLocation().getBlockZ());
                deadLocations.add(deadLocation);
                entity.remove();
            }
        }

        if(deadLocations.size()>0) {
            main.getLogger().warning("HologramFixer: Found and removed "+deadLocations.size()+" dead holograms in world "+world.getName());
        }

        return deadLocations.size();
    }
}
