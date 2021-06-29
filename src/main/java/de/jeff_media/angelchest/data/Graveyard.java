package de.jeff_media.angelchest.data;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.exceptions.InvalidLocationDefinitionException;
import de.jeff_media.angelchest.handlers.ChunkManager;
import de.jeff_media.jefflib.LocationUtils;
import de.jeff_media.jefflib.TimeUtils;
import de.jeff_media.jefflib.thirdparty.io.papermc.paperlib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Graveyard {

    private final String name;
    private final WorldBoundingBox boundingBox;
    private final Collection<Material> spawnOn;
    private final List<Block> cachedValidGraveLocations = new ArrayList<>();
    private final Material material;
    private final String hologramText;
    private final Location spawn;
    private final boolean instantRespawn;
    private final boolean global;
    private final Integer totemAnimation;

    private static final Main main = Main.getInstance();

    private Graveyard(String name, WorldBoundingBox worldBoundingBox, @Nullable Collection<Material> spawnOn, @Nullable Material material, @Nullable String hologramText, boolean global, @Nullable Location spawn, boolean instantRespawn, Integer totemAnimation) {
        this.name = name;
        this.boundingBox = worldBoundingBox;
        this.spawnOn = spawnOn;
        this.material = material;
        this.hologramText = hologramText;
        this.global = global;
        this.spawn = spawn;
        this.instantRespawn = instantRespawn;
        this.totemAnimation = totemAnimation;
        populateBlocksInsideAsync();
    }

    @Nullable
    public Location getSpawn() {
        return spawn;
    }

    public boolean hasSpace() {
        return getFreeSpot() != null;
    }

    @Override
    public String toString() {
        return "Graveyard{" +
                "name='" + name + '\'' +
                ", boundingBox=" + boundingBox +
                ", spawnOn=" + spawnOn +
                ", material=" + material +
                ", hologramText='" + hologramText + '\'' +
                ", spawn=" + spawn +
                ", instantRespawn=" + instantRespawn +
                ", global=" + global +
                '}';
    }

    public static Graveyard fromConfig(ConfigurationSection config) {

        //this.name = config.getName();
        String name = config.getCurrentPath();

        main.getLogger().info("Loading Graveyard " + name);

        int minX = config.getInt("location.min.x");
        int minY = config.getInt("location.min.y");
        int minZ = config.getInt("location.min.z");
        int maxX = config.getInt("location.max.x");
        int maxY = config.getInt("location.max.y");
        int maxZ = config.getInt("location.max.z");
        World world = Bukkit.getWorld(config.getString("location.world"));
        if (world == null) {
            new IllegalArgumentException("World " + config.getString("location.world") + " is not loaded.").printStackTrace();
        }

        Collection<Material> spawnOn = new ArrayList<>();
        if (config.isList("only-spawn-on")) {
            for (String matName : config.getStringList("only-spawn-on")) {
                Material mat = Enums.getIfPresent(Material.class, matName.toUpperCase(Locale.ROOT)).orNull();
                if (mat != null) {
                    spawnOn.add(mat);
                } else {
                    Main.getInstance().getLogger().warning("Unknown material defined in graveyard " + name + ": " + matName);
                }
            }
            if (spawnOn.isEmpty()) {
                spawnOn = null;
            }
        } else {
            spawnOn = null;
        }

        WorldBoundingBox boundingBox = new WorldBoundingBox(world, BoundingBox.of(world.getBlockAt(minX, minY, minZ), world.getBlockAt(maxX, maxY, maxZ)));
        Material material;
        if (config.isSet("material")) {
            material = Enums.getIfPresent(Material.class, config.getString("material").toUpperCase(Locale.ROOT)).orNull();
            if (material == null) {
                Main.getInstance().getLogger().warning("Unknown material specified in Graveyard " + name + ": " + config.getString("material"));
            }
        } else {
            material = null;
        }

        String hologram;
        if (config.isSet("hologram-text")) {
            hologram = config.getString("hologram-text");
        } else {
            hologram = null;
        }

        boolean global = config.getBoolean("global", false);

        Location spawn = null;
        if(config.isSet("spawn") && config.isConfigurationSection("spawn")) {
            try {
                spawn = LocationUtils.getLocationFromSection(config.getConfigurationSection("spawn"), world);
            } catch (InvalidLocationDefinitionException exception) {
                main.getLogger().warning("Invalid spawn location defined for graveyard " + name + ", ignoring spawn location");
                exception.printStackTrace();
                spawn = null;
            }
        }

        boolean instantRespawn = config.getBoolean("instant-respawn",false);

        Integer totemAnimation = null;
        if(config.isBoolean("totem-animation")) {
            totemAnimation = config.getBoolean("totem-animation") ? 0 : null;
        } else if(config.isInt("totem-animation")) {
            totemAnimation = config.getInt("totem-animation");
        }

        return new Graveyard(name, boundingBox, spawnOn, material, hologram, global, spawn, instantRespawn, totemAnimation);
    }

    public boolean hasCustomTotemAnimation() {
        return totemAnimation != null;
    }

    public int getCustomTotemModelData() {
        return totemAnimation;
    }

/*    private void populateBlocksInside() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(int x = boundingBox.getMinBlock().getX(); x <= boundingBox.getMaxBlock().getX(); x++) {
                    for(int y = boundingBox.getMinBlock().getY(); y <= boundingBox.getMaxBlock().getY(); y++) {
                        for(int z = boundingBox.getMinBlock().getZ(); z <= boundingBox.getMaxBlock().getZ(); z++) {
                            Location location = new Location(getWorldBoundingBox().getWorld(), x,y,z);
                            if(!PaperLib.isChunkGenerated(location)) continue;
                            Future<Chunk> future = PaperLib.getChunkAtAsync(location);
                            while(!future.isDone()) {
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }

                            Block block = boundingBox.getWorld().getBlockAt(x,y,z);
                            if(isValidSpawnOn(block)) {
                                cachedValidGraveLocations.add(block);
                                ChunkManager.keepLoaded(block);
                            }
                        }
                    }
                }
                Collections.shuffle(cachedValidGraveLocations);
            }
        }.runTaskAsynchronously(Main.getInstance());*/

    public List<Block> getCachedValidGraveLocations() {
        return cachedValidGraveLocations;
    }

    public WorldBoundingBox getWorldBoundingBox() {
        return boundingBox;
    }

    private void populateBlocksInsideAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                TimeUtils.startTimings("Find grave locations for graveyard " + name);
                for (int x = boundingBox.getMinBlock().getX(); x <= boundingBox.getMaxBlock().getX(); x += 16) {
                    for (int z = boundingBox.getMinBlock().getZ(); z <= boundingBox.getMaxBlock().getZ(); z += 16) {
                        LocationUtils.ChunkCoordinates chunkCoordinates = LocationUtils.getChunkCoordinates(x, z);
                        Future<Chunk> future = PaperLib.getChunkAtAsync(getWorldBoundingBox().getWorld(), chunkCoordinates.getX(), chunkCoordinates.getZ());
                        while (!future.isDone() && !future.isCancelled()) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                future.cancel(false);
                                return;
                            }
                        }
                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                            try {
                                Chunk chunk = future.get();
                                for (int bx = 0; bx < 16; bx++) {
                                    for (int by = (int) boundingBox.getBoundingBox().getMinY(); by < boundingBox.getBoundingBox().getMaxY(); by++) {
                                        for (int bz = 0; bz < 16; bz++) {
                                            //Location location = new Location(chunk.getWorld(), bx, by, bz);
                                            Block block = chunk.getBlock(bx,by,bz);
                                            if (boundingBox.contains(block)) {
                                                if (isValidSpawnOn(block)) {
                                                    /*if(Main.getInstance().debug) {
                                                        System.out.println("Found valid grave: " + block);
                                                    }*/
                                                    cachedValidGraveLocations.add(block);
                                                    ChunkManager.keepLoaded(block);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> Collections.shuffle(cachedValidGraveLocations));
                TimeUtils.endTimings("Find grave locations for graveyard " + name,main,true);
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public boolean hasCustomMaterial() {
        return material != null;
    }

    public Material getCustomMaterial() {
        return material;
    }

    public boolean isValidSpawnOn(Block block) {
        if (!block.getType().isAir()) return false;
        Block below = block.getRelative(BlockFace.DOWN);
        if (below.getType().isAir()) return false;
        if (spawnOn == null) return true;
        return spawnOn.contains(below.getType());
    }

    @NotNull
    public Collection<Block> getFreeSpots() {
        HashSet<Block> blocks = new HashSet<>();
        for (Block block : cachedValidGraveLocations) {
            if (isValidSpawnOn(block)) blocks.add(block);
        }
        return blocks;
    }

    @Nullable
    public Block getFreeSpot() {
        Collections.shuffle(cachedValidGraveLocations);
        for (Block block : cachedValidGraveLocations) {
            if (isValidSpawnOn(block)) return block;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public boolean hasCustomHologram() {
        return hologramText != null;
    }

    public String getCustomHologram() {
        return hologramText;
    }

    public boolean isGlobal() {
        return global;
    }

    public boolean isInstantRespawn() {
        return instantRespawn;
    }
}
