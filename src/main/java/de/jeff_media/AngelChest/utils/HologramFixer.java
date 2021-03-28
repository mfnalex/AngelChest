package de.jeff_media.AngelChest.utils;

import de.jeff_media.AngelChest.Main;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.javatuples.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HologramFixer {

    public static int removeDeadHolograms(World world) {

        Main main = Main.getInstance();

        //noinspection rawtypes
        Collection armorStands;
        armorStands = world.getEntitiesByClass(ArmorStand.class);


        Set<Pair<Integer,Integer>> deadLocations = new HashSet<>();
        Iterator<Entity> it = armorStands.iterator();
        while(it.hasNext()) {
            Entity entity = it.next();
            if(main.nbtUtils.isBrokenHologram((ArmorStand) entity)) {
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
