package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.javatuples.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class HologramFixer {

    public static int removeDeadHolograms(final World world) {

        final Main main = Main.getInstance();

        //noinspection rawtypes
        final Collection armorStands;
        armorStands = world.getEntitiesByClass(ArmorStand.class);


        final Set<Pair<Integer, Integer>> deadLocations = new HashSet<>();
        @SuppressWarnings("unchecked") final Iterator<Entity> it = armorStands.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) {
            final Entity entity = it.next();
            if (main.nbtUtils.isBrokenHologram((ArmorStand) entity)) {
                final Pair<Integer, Integer> deadLocation = new Pair<>(entity.getLocation().getBlockX(), entity.getLocation().getBlockZ());
                deadLocations.add(deadLocation);
                entity.remove();
            }
        }

        if (!deadLocations.isEmpty()) {
            main.debug("HologramFixer: Found and removed " + deadLocations.size() + " dead holograms in world " + world.getName());
        }

        return deadLocations.size();
    }
}
