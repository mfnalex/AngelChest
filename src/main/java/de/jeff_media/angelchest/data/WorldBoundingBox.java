package de.jeff_media.angelchest.data;

import com.allatori.annotations.DoNotRename;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

@DoNotRename
public class WorldBoundingBox {

    private final BoundingBox boundingBox;
    private final World world;

    public WorldBoundingBox(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.boundingBox = new BoundingBox(x1, y1, z1, x2, y2, z2);
        this.world = world;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean contains(Location location) {
        if(!location.getWorld().equals(world)) return false;
        if(location.getX() > boundingBox.getMaxX()) return false;
        if(location.getX() < boundingBox.getMinX()) return false;
        if(location.getY() > boundingBox.getMaxY()) return false;
        if(location.getY() < boundingBox.getMinY()) return false;
        if(location.getZ() > boundingBox.getMaxZ()) return false;
        if(location.getZ() < boundingBox.getMinZ()) return false;
        return true;
    }

    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    public WorldBoundingBox(World world, BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.world = world;
    }

    @NotNull
    public Location getMinLocation() {
        return new Location(world, boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ());
    }

    @NotNull
    public Location getMaxLocation() {
        return new Location(world, boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }

    @NotNull
    public Block getMinBlock() {
        return getMinLocation().getBlock();
    }

    @NotNull
    public Block getMaxBlock() {
        return getMaxLocation().getBlock();
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = boundingBox.serialize();
        map.put("world",world.getUID());
        return map;
    }

    @NotNull
    public static WorldBoundingBox deserialize(@NotNull Map<String, Object> args) {
        BoundingBox boundingBox = BoundingBox.deserialize(args);
        UUID worldUID = (UUID) args.get("world");
        World world = Bukkit.getWorld(worldUID);
        if(world == null) {
            throw new IllegalArgumentException("World " + worldUID + " not found!");
        }
        return new WorldBoundingBox(world, boundingBox);
    }

    @Override
    public String toString() {
        return "WorldBoundingBox{" +
                "boundingBox=" + boundingBox +
                ", world=" + world +
                '}';
    }
}
