package de.jeff_media.angelchest.data;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.handlers.ChunkManager;
import de.jeff_media.jefflib.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Graveyard {

    private final String name;
    private final WorldBoundingBox boundingBox;
    private final Collection<Material> spawnOn;
    private final List<Block> cachedValidGraveLocations = new ArrayList<>();
    private final Material material;
    private final String hologramText;

    public List<Block> getCachedValidGraveLocations() {
        return cachedValidGraveLocations;
    }

    public WorldBoundingBox getWorldBoundingBox() {
        return boundingBox;
    }

    private void populateBlocksInside() {
        for(int x = boundingBox.getMinBlock().getX(); x <= boundingBox.getMaxBlock().getX(); x++) {
            for(int y = boundingBox.getMinBlock().getY(); y <= boundingBox.getMaxBlock().getY(); y++) {
                for(int z = boundingBox.getMinBlock().getZ(); z <= boundingBox.getMaxBlock().getZ(); z++) {
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

    private Graveyard(String name, WorldBoundingBox worldBoundingBox, @Nullable Collection<Material> spawnOn, @Nullable Material material, @Nullable String hologramText) {
        this.name = name;
        this.boundingBox = worldBoundingBox;
        this.spawnOn = spawnOn;
        this.material = material;
        this.hologramText = hologramText;
        populateBlocksInside();
    }

    public boolean hasCustomMaterial() {
        return material != null;
    }

    public Material getCustomMaterial() {
        return material;
    }

    @Override
    public String toString() {
        return "Graveyard{" +
                "name='" + name + '\'' +
                ", boundingBox=" + boundingBox +
                ", spawnOn=" + spawnOn +
                ", material=" + material +
                '}';
    }

    public static Graveyard fromConfig(ConfigurationSection config) {
        //this.name = config.getName();
        String name = config.getCurrentPath();
        int minX = config.getInt("location.min.x");
        int minY = config.getInt("location.min.y");
        int minZ = config.getInt("location.min.z");
        int maxX = config.getInt("location.max.x");
        int maxY = config.getInt("location.max.y");
        int maxZ = config.getInt("location.max.z");
        World world = Bukkit.getWorld(config.getString("location.world"));
        if(world == null) {
            new IllegalArgumentException("World " + config.getString("location.world") + " is not loaded.").printStackTrace();
        }

        Collection<Material> spawnOn = new ArrayList<>();
        if(config.isList("only-spawn-on")) {
            for(String matName : config.getStringList("only-spawn-on")) {
                Material mat = Enums.getIfPresent(Material.class, matName.toUpperCase(Locale.ROOT)).orNull();
                if(mat != null) {
                    spawnOn.add(mat);
                }
            }
            if(spawnOn.isEmpty()) {
                spawnOn = null;
            }
        } else {
            spawnOn = null;
        }

        WorldBoundingBox boundingBox = new WorldBoundingBox(world,BoundingBox.of(world.getBlockAt(minX,minY,minZ), world.getBlockAt(maxX,maxY,maxZ)));
        Material material;
        if(config.isSet("material")) {
            material = Enums.getIfPresent(Material.class, config.getString("material").toUpperCase(Locale.ROOT)).orNull();
            if(material == null) {
                Main.getInstance().getLogger().warning("Unknown material specified in Graveyard " + name + ": " + config.getString("material"));
            }
        } else {
            material = null;
        }

        String hologram;
        if(config.isSet("hologram-text")) {
            hologram = config.getString("hologram-text");
        } else {
            hologram = null;
        }

        return new Graveyard(name, boundingBox, spawnOn, material, hologram);
    }

    public boolean isValidSpawnOn(Block block) {
        if(!block.getType().isAir()) return false;
        Block below = block.getRelative(BlockFace.DOWN);
        if(below.getType().isAir()) return false;
        if(spawnOn == null) return true;
        return spawnOn.contains(below.getType());
    }

    @NotNull
    public Collection<Block> getFreeSpots() {
        TimeUtils.startTimings("getFreeSpots");
        HashSet<Block> blocks = new HashSet<>();
        for(Block block : cachedValidGraveLocations) {
            if(isValidSpawnOn(block)) blocks.add(block);
        }
        TimeUtils.endTimings("getFreeSpots");
        return blocks;
    }

    @Nullable
    public Block getFreeSpot() {
        for(Block block : cachedValidGraveLocations) {
            if(isValidSpawnOn(block)) return block;
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
}
