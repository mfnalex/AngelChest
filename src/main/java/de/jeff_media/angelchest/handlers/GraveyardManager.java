package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy_Stepsister;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GraveyardManager {

    private static final AngelChestMain main = AngelChestMain.getInstance();
    private static List<Graveyard> GRAVEYARDS;
    private static Graveyard GLOBAL_GRAVEYARD;
    private static final Map<UUID, Graveyard> LAST_GRAVEYARDS = new HashMap<>();
    private static final Map<UUID, Location> LAST_RESPAWN_LOCATIONS = new HashMap<>();

    @Nullable
    public static Graveyard getLastGraveyard(OfflinePlayer player) {
        if(LAST_GRAVEYARDS.containsKey(player.getUniqueId())) {
            return LAST_GRAVEYARDS.get(player.getUniqueId());
        }
        return null;
    }

    public static Location getLastRespawnLoc(OfflinePlayer player) {
        if(LAST_RESPAWN_LOCATIONS.containsKey(player.getUniqueId())) {
            return LAST_RESPAWN_LOCATIONS.get(player.getUniqueId());
        }
        return null;
    }

    public static void setLastGraveyard(OfflinePlayer player, Graveyard graveyard) {
        if(graveyard == null) {
            LAST_GRAVEYARDS.remove(player.getUniqueId());
        }
        LAST_GRAVEYARDS.put(player.getUniqueId(), graveyard);
    }

    public static void setLastRespawnLoc(OfflinePlayer player, Location respawnLoc) {
        if(respawnLoc == null) {
            LAST_RESPAWN_LOCATIONS.remove(player.getUniqueId());
        }
        LAST_RESPAWN_LOCATIONS.put(player.getUniqueId(), respawnLoc);
    }

    @Nullable
    public static Graveyard fromBlock(@NotNull Block block) {
        for(Graveyard graveyard : GRAVEYARDS) {
            if(!graveyard.getWorldBoundingBox().getWorld().equals(block.getWorld())) continue;
            if(graveyard.getWorldBoundingBox().contains(block)) return graveyard;
        }
        return null;
    }

    @Nullable
    public static Graveyard fromLocation(@NotNull Location location) {
        for(Graveyard graveyard : GRAVEYARDS) {
            if(!graveyard.getWorldBoundingBox().getWorld().equals(location.getWorld())) continue;
            if(graveyard.getWorldBoundingBox().contains(location)) return graveyard;
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
    public static Graveyard getNearestFreeGraveyard(Location location, boolean tryClosest, boolean tryGlobal) {
        List<Graveyard> graveyards = new ArrayList<>();

        for (Graveyard graveyard : GRAVEYARDS) {
            if (!graveyard.getWorldBoundingBox().getWorld().equals(location.getWorld())) continue;
            graveyards.add(graveyard);
        }

        graveyards.sort((o1, o2) -> {
            double d1 = o1.getWorldBoundingBox().getBoundingBox().getCenter().distanceSquared(location.toVector());
            double d2 = o2.getWorldBoundingBox().getBoundingBox().getCenter().distanceSquared(location.toVector());
            return Double.compare(d1, d2);
        });

        if (graveyards.isEmpty()) {
            if (tryGlobal) {
                if(GLOBAL_GRAVEYARD == null) {
                    //System.out.println("[GRAVEYARDS] No graveyards in this world and no global graveyard, too");
                    return null;
                } else if (GLOBAL_GRAVEYARD.hasSpace()) {
                    //System.out.println("[GRAVEYARDS] No graveyards in this world but global graveyard has space");
                    return GLOBAL_GRAVEYARD;
                } else {
                    //System.out.println("[GRAVEYARDS] No graveyards in this world and global graveyard is full too");
                    return null;
                }
            } else {
                //System.out.println("[GRAVEYARDS] No yards in this world and global yard disabled");
                return null;
            }
        }


        if (graveyards.get(0).hasSpace()) {
            //System.out.println("[GRAVEYARDS] Closest graveyard is " + graveyards.get(0).getName() + " and is has space left.");
            return graveyards.get(0);
        }

        if (!tryClosest && !tryGlobal) {
            //System.out.println("[GRAVEYARDS] Closest graveyard is full, neither trying other nor the global yard");
            return null;
        }

        if (!tryClosest && tryGlobal) {
            if (GLOBAL_GRAVEYARD.hasSpace()) {
                //System.out.println("[GRAVEYARDS] Closest graveyard is full, not looking for other ones but using the global yard instead");
                return GLOBAL_GRAVEYARD;
            }
            //System.out.println("[GRAVEYARDS] Closest graveyard is full and global graveyard is full, too");
            return null;
        }

        for (Graveyard graveyard : graveyards) {
            if (graveyard.hasSpace()) {
                //System.out.println("[GRAVEYARDS] Closest graveyard is full but we found " + graveyard.getName() + " instead");
                return graveyard;
            }
        }

        if (tryGlobal && GLOBAL_GRAVEYARD != null) {
            if (GLOBAL_GRAVEYARD.hasSpace()) {
                //System.out.println("[GRAVEYARDS] All graveyards in this world are full but the global yard still has space");
                return GLOBAL_GRAVEYARD;
            }
        }

        //System.out.println("[GRAVEYARDS] Could not find any graveyard although we tried all yards in this world AND the global yard");
        return null;
    }

    @Nullable
    public static Block getGraveLocation(Location location, boolean tryClosest, boolean tryGlobal) {
        Graveyard yard = getNearestFreeGraveyard(location, tryClosest, tryGlobal);
        if(yard == null) return null;
        return yard.getFreeSpot();
    }

    public static boolean hasGraveyard(World world) {
        for(Graveyard yard : GRAVEYARDS) {
            if(yard.getWorldBoundingBox().getWorld().equals(world)) return true;
        }
        return false;
    }

    public static void init() {
        init(true);
    }

    public static void init(boolean output) {
        GRAVEYARDS = new ArrayList<>();
        GLOBAL_GRAVEYARD = null;
        File file = new File(main.getDataFolder(), "graveyards.yml");
        if(output && !Daddy_Stepsister.allows(PremiumFeatures.GRAVEYARDS)) {
            main.getLogger().info("Not using premium version, disabling Graveyards feature");
            return;
        }
        if(output && !file.exists()) {
            main.getLogger().info("No graveyards.yml found, disabling Graveyards feature");
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        for(String graveyardName : yaml.getKeys(false)) {
            if(Bukkit.getWorld(yaml.getString(graveyardName+".location.world")) == null) {
                if(output) {
                    main.getLogger().info("Could not load graveyard " + graveyardName + " because world " + yaml.getString(graveyardName + ".location.world") + " does not exist or wasn't loaded yet.");
                }
                continue;
            }
            Graveyard next = Graveyard.fromConfig(yaml.getConfigurationSection(graveyardName));
            GRAVEYARDS.add(next);
            if(next.isGlobal()) {
                GLOBAL_GRAVEYARD = next;
            }
        }
    }
}
