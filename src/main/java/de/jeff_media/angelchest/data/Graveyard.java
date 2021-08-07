package de.jeff_media.angelchest.data;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.exceptions.InvalidLocationDefinitionException;
import de.jeff_media.angelchest.handlers.ChunkManager;
import de.jeff_media.angelchest.listeners.GraveyardListener;
import de.jeff_media.jefflib.LocationUtils;
import de.jeff_media.jefflib.TimeUtils;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private final BlockData blockData;
    private final String hologramText;
    private final Location spawn;
    private final boolean instantRespawn;
    private final boolean global;
    private final Integer totemAnimation;
    private final Collection<PotionEffect> potionEffects;
    private final Long localTime;
    private final WeatherType weatherType;

    private static final Main main = Main.getInstance();

    private Graveyard(String name, WorldBoundingBox worldBoundingBox, @Nullable Collection<Material> spawnOn, @Nullable BlockData blockData, @Nullable String hologramText, boolean global, @Nullable Location spawn, boolean instantRespawn, Integer totemAnimation, Collection<PotionEffect> potionEffects, @Nullable Long localTime, @Nullable WeatherType weatherType) {
        this.name = name;
        this.boundingBox = worldBoundingBox;
        this.spawnOn = spawnOn;
        this.blockData = blockData;
        this.hologramText = hologramText;
        this.global = global;
        this.spawn = spawn;
        this.instantRespawn = instantRespawn;
        this.totemAnimation = totemAnimation;
        this.potionEffects = potionEffects;
        this.localTime = localTime;
        this.weatherType = weatherType;
        populateBlocksInsideAsync();
    }

    @Nullable
    public Location getSpawn() {
        return spawn;
    }

    public boolean hasSpace() {
        return getFreeSpot() != null;
    }

    public Collection<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    @Override
    public String toString() {
        return "Graveyard{" +
                "name='" + name + '\'' +
                ", boundingBox=" + boundingBox +
                ", spawnOn=" + spawnOn +
                ", material=" + blockData +
                ", hologramText='" + hologramText + '\'' +
                ", spawn=" + spawn +
                ", instantRespawn=" + instantRespawn +
                ", global=" + global +
                ", totemAnimation=" + totemAnimation +
                ", potionEffects=" + potionEffects +
                '}';
    }

    public static Graveyard fromConfig(ConfigurationSection config) {

        //this.name = config.getName();
        String name = config.getCurrentPath();

        //main.getLogger().info("Loading Graveyard " + name);

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
        if (config.isList("grave-locations")) {
            for (String matName : config.getStringList("grave-locations")) {
                Material mat = Enums.getIfPresent(Material.class, matName.toUpperCase(Locale.ROOT)).orNull();
                if (mat != null) {
                    spawnOn.add(mat);
                } else {
                    Main.getInstance().getLogger().warning("Unknown material defined in graveyard " + name + ": " + matName);
                }
            }
            if (spawnOn.isEmpty()) {
                spawnOn = new ArrayList<>();
            }
        } else {
            spawnOn = new ArrayList<>();
        }

        WorldBoundingBox boundingBox = new WorldBoundingBox(world, BoundingBox.of(world.getBlockAt(minX, minY, minZ), world.getBlockAt(maxX, maxY, maxZ)));

        BlockData blockData;
        if (config.isSet("material")) {
            String value = config.getString("material");
            if(!value.contains(":") && !value.contains("[")) {
                Material mat = Enums.getIfPresent(Material.class, config.getString("material").toUpperCase(Locale.ROOT)).orNull();
                if (mat == null) {
                    Main.getInstance().getLogger().warning("Unknown material specified in Graveyard " + name + ": " + config.getString("material"));
                }
                blockData = Bukkit.getServer().createBlockData(mat);
            } else {
                blockData = Bukkit.getServer().createBlockData(value);
            }
        } else {
            blockData = null;
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

        Collection<PotionEffect> potionEffects = new ArrayList<>();
        if(config.isConfigurationSection("potion-effects")) {
            ConfigurationSection potionSection = config.getConfigurationSection("potion-effects");
            for(String key : potionSection.getKeys(false)) {
                PotionEffectType type = PotionEffectType.getByName(key.toUpperCase(Locale.ROOT));
                if(type == null) {
                    main.getLogger().warning("Invalid potion effect \"" + key + "\" defined defined for graveyard " + name);
                    continue;
                }
                int amplifier = potionSection.getInt(key.toUpperCase(Locale.ROOT));
                potionEffects.add(new PotionEffect(type,Integer.MAX_VALUE, amplifier));
            }
        }

        Long localTime = null;
        if(config.isSet("local-time")) {
            localTime = config.getLong("local-time");
        }

        WeatherType weatherType = null;
        if(config.isSet("local-weather")) {
            switch (config.getString("local-weather").toLowerCase(Locale.ROOT)) {
                case "rain":
                    weatherType = WeatherType.DOWNFALL;
                    break;
                case "sun":
                    weatherType = WeatherType.CLEAR;
                    break;
                default:
                    weatherType = null;
            }
        }

        return new Graveyard(name, boundingBox, spawnOn, blockData, hologram, global, spawn, instantRespawn, totemAnimation, potionEffects, localTime, weatherType);
    }

    public void applyPotionEffects(Player player) {
        GraveyardListener.getActivePotionEffects().put(player, player.getActivePotionEffects());
        for(PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.addPotionEffects(potionEffects);
    }

    public void removePotionEffects(Player player) {
        for(PotionEffect effect : potionEffects) {
            player.removePotionEffect(effect.getType());
        }
        Collection<PotionEffect> oldEffects = GraveyardListener.getActivePotionEffects().get(player);
        if(oldEffects != null) {
            player.addPotionEffects(oldEffects);
            GraveyardListener.getActivePotionEffects().remove(player);
        }
    }

    public Collection<Material> getSpawnOn() {
        return spawnOn;
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
                main.getLogger().info("Scanning graveyard " + name + " asynchronously for possible grave locations...");
                TimeUtils.startTimings("Find grave locations for graveyard " + name);
                for (int x = boundingBox.getMinBlock().getX(); x <= boundingBox.getMaxBlock().getX() + 16; x += 16) {
                    for (int z = boundingBox.getMinBlock().getZ(); z <= boundingBox.getMaxBlock().getZ() + 16; z += 16) {
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
                try {
                    long duration = TimeUtils.endTimings("Find grave locations for graveyard " + name, main, false);
                    Bukkit.getScheduler().runTaskLater(main, () -> main.getLogger().info("Found " + cachedValidGraveLocations.size() + " possible grave locations in graveyard " + name + " (Duration: " + TimeUtils.formatNanoseconds(duration)+")"), 1L);
                } catch (Exception ignored) {

                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public boolean hasCustomMaterial() {
        return blockData != null;
    }

    public BlockData getCustomMaterial() {
        return blockData;
    }

    public boolean isValidSpawnOn(Block block) {
        if (!block.getType().isAir()) return false;
        Block below = block.getRelative(BlockFace.DOWN);
        if (below.getType().isAir()) return false;
        if (spawnOn == null || spawnOn.isEmpty()) return true;
        return spawnOn.contains(below.getType());
    }

    public String[][] toPrettyString() {
        String onlySpawnOn = "[";
        if(spawnOn != null) {
            Iterator<Material> materialIterator = spawnOn.iterator();
            while (materialIterator.hasNext()) {
                Material mat = materialIterator.next();
                onlySpawnOn += mat.name();
                if (materialIterator.hasNext()) {
                    onlySpawnOn += ",";
                }
            }
        }
        onlySpawnOn+="]";
        String potionEffects = new String();
        Iterator<PotionEffect> potionEffectIterator = this.potionEffects.iterator();
        while(potionEffectIterator.hasNext()) {
            potionEffects += potionEffectIterator.next().toString();
            if(potionEffectIterator.hasNext()) {
                potionEffects += ",";
            }
        }
        return new String[][]{
                {"Name",name},
                {"Min",boundingBox.getMinBlock().getX()+", " + boundingBox.getMinBlock().getY()+", " + boundingBox.getMinBlock().getZ()},
                {"Max",boundingBox.getMaxBlock().getX()+", " + boundingBox.getMaxBlock().getY()+", " + boundingBox.getMaxBlock().getZ()},
                {"Spawn on", onlySpawnOn},
                {"Material", blockData == null ? "default" : blockData.getAsString()},
                {"Free graves", String.valueOf(getFreeSpots().size())},
                {"Global", String.valueOf(global)},
                {"Instant respawn", String.valueOf(instantRespawn)},
                {"Spawn",spawn == null ? null : LocationUtils.toPrettyString(spawn,true,true)},
                {"Potion effects",potionEffects.length()==0 ? "none" : potionEffects},
                {"Time",hasCustomTime() ? String.valueOf(localTime) : "default"},
                {"Weather",hasCustomWeather() ? String.valueOf(weatherType) : "default"}
        };
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

    public boolean hasCustomTime() {
        return localTime != null;
    }

    public Long getCustomTime() {
        return localTime;
    }

    public boolean hasCustomWeather() {
        return weatherType != null;
    }

    public WeatherType getCustomWeather() {
        return weatherType;
    }
}
