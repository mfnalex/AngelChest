package com.jeff_media.jefflib;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public final class LocationUtils {

    private LocationUtils() {
    }

    public static Location getLocationFromSection(final ConfigurationSection config, final World defaultWorld) {
        if (config.getBoolean("spawn")) {
            final World world = Bukkit.getWorld(Objects.requireNonNull(config.getString("world")));
            if (world == null) throw new IllegalArgumentException("World not found");
            return world.getSpawnLocation();
        }
        final World world = getWorld(config, defaultWorld);
        if (!config.isSet("x") || !config.isSet("y") || !config.isSet("z")) throw new IllegalArgumentException("x, y and z must be defined");
        final double x = config.getDouble("x");
        final double y = config.getDouble("y");
        final double z = config.getDouble("z");
        if (config.isSet("pitch") || config.isSet("yaw")) {
            if (!config.isSet("pitch") || !config.isSet("yaw")) throw new IllegalArgumentException("pitch and yaw must both be defined");
            return new Location(world, x, y, z, (float) config.getDouble("yaw"), (float) config.getDouble("pitch"));
        }
        return new Location(world, x, y, z);
    }

    private static World getWorld(final ConfigurationSection config, final World defaultWorld) {
        if (!config.isSet("world")) {
            if (defaultWorld == null) throw new IllegalArgumentException("world must be defined");
            return defaultWorld;
        }
        final String value = config.getString("world");
        World world = Bukkit.getWorld(value);
        if (world == null) {
            try {
                world = Bukkit.getWorld(UUID.fromString(value));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (world == null) throw new IllegalArgumentException("World \"" + value + "\" not found");
        return world;
    }

    public static ChunkCoordinates getChunkCoordinates(final int x, final int z) {
        return new ChunkCoordinates(x >> 4, z >> 4);
    }

    public static ChunkCoordinates getChunkCoordinates(final Location location) {
        return getChunkCoordinates(location.getBlockX(), location.getBlockZ());
    }

    public static String toPrettyString(final Location location, final boolean showYawAndPitch, final boolean showWorld) {
        String result = String.format(Locale.ROOT, "x=%.2f, y=%.2f, z=%.2f", location.getX(), location.getY(), location.getZ());
        if (showYawAndPitch) result += String.format(Locale.ROOT, ", yaw=%.2f, pitch=%.2f", location.getYaw(), location.getPitch());
        if (showWorld) result += ", world=" + Objects.requireNonNull(location.getWorld()).getName();
        return result;
    }

    public static final class ChunkCoordinates {
        private final int x;
        private final int z;

        public ChunkCoordinates(final int x, final int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }
    }
}
