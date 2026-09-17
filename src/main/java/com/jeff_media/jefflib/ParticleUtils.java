package com.jeff_media.jefflib;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public final class ParticleUtils {

    public static final Particle PARTICLE_EXPLOSION_NORMAL = EnumUtils.getIfPresent(Particle.class, "EXPLOSION_NORMAL", "EXPLOSION").orElse(null);

    private ParticleUtils() {
    }

    public static BukkitRunnable drawHollowCube(final World world, final BoundingBox box, final Player player, final Particle particle, final int count, final Object data) {
        final Set<Location> points = new HashSet<>();
        for (double x = box.getMinX(); x <= box.getMaxX(); x += 0.5) {
            for (double y = box.getMinY(); y <= box.getMaxY(); y += 0.5) {
                for (double z = box.getMinZ(); z <= box.getMaxZ(); z += 0.5) {
                    int edges = 0;
                    if (x == box.getMinX() || x == box.getMaxX()) edges++;
                    if (y == box.getMinY() || y == box.getMaxY()) edges++;
                    if (z == box.getMinZ() || z == box.getMaxZ()) edges++;
                    if (edges >= 2) points.add(new Location(world, x, y, z));
                }
            }
        }
        return new BukkitRunnable() {
            private int runs;

            @Override
            public void run() {
                for (Location point : points) {
                    if (data == null) player.spawnParticle(particle, point, count);
                    else player.spawnParticle(particle, point, count, data);
                }
                if (++runs >= 4) cancel();
            }
        };
    }
}
