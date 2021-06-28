package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class GraveyardManager {

    private static final Main main = Main.getInstance();
    private static HashSet<Graveyard> GRAVEYARDS;

    @Nullable
    public static Graveyard fromBlock(@NotNull Block block) {
        for(Graveyard graveyard : GRAVEYARDS) {
            if(!graveyard.getWorldBoundingBox().getWorld().equals(block.getWorld())) continue;
            if(graveyard.getWorldBoundingBox().contains(block)) return graveyard;
        }
        return null;
    }

    @Nullable
    public static Graveyard fromName(@Nullable String name) {
        if(name == null || name.length()==0) return null;
        for(Graveyard graveyard : GRAVEYARDS) {
            if(graveyard.getName().equals(name)) {
                return graveyard;
            }
        }
        return null;
    }

    @NotNull
    public static Collection<Graveyard> getGraveyards(World world) {
        HashSet<Graveyard> set = new HashSet<>();
        for(Graveyard graveyard : GRAVEYARDS) {
            if(graveyard.getWorldBoundingBox().getWorld().equals(world)) {
                set.add(graveyard);
            }
        }
        return set;
    }

    @Nullable
    public static Graveyard getNearestGraveyard(Location location) {
        Graveyard nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for(Graveyard graveyard : GRAVEYARDS) {
            if(!graveyard.getWorldBoundingBox().getWorld().equals(location.getWorld())) continue;
            double distance = graveyard.getWorldBoundingBox().getBoundingBox().getCenter().distanceSquared(location.toVector());
            if(distance < nearestDistance) {
                nearest = graveyard;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    @Nullable
    public static Block getGraveLocation(Location location) {
        Graveyard yard = getNearestGraveyard(location);
        return yard.getFreeSpot();
    }

    public static boolean hasGraveyard(World world) {
        for(Graveyard yard : GRAVEYARDS) {
            if(yard.getWorldBoundingBox().getWorld().equals(world)) return true;
        }
        return false;
    }

    public static void init() {
        GRAVEYARDS = new HashSet<>();
        File file = new File(main.getDataFolder(), "graveyards.yml");
        if(!Daddy.allows(PremiumFeatures.GRAVEYARDS)) {
            main.getLogger().info("Not using premium version, disabling Graveyards feature");
            return;
        }
        if(!file.exists()) {
            main.getLogger().info("No graveyards.yml found, disabling Graveyards feature");
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        for(String graveyardName : yaml.getKeys(false)) {
            GRAVEYARDS.add(Graveyard.fromConfig(yaml.getConfigurationSection(graveyardName)));
        }
    }
}
