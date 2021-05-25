package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class AngelChestUtils {

    private static final int MAX_NETHER_HEIGHT = 128;

    public static ArrayList<AngelChest> getAllAngelChestsFromPlayer(final UUID uuid) {
        return getAllAngelChestsFromPlayer(Bukkit.getOfflinePlayer(uuid));
    }

    public static ArrayList<AngelChest> getAllAngelChestsFromPlayer(final OfflinePlayer p) {
        final ArrayList<AngelChest> angelChests = new ArrayList<>();
        for (final AngelChest angelChest : Main.getInstance().angelChests.values()) {
            if (!angelChest.owner.equals(p.getUniqueId())) continue;
            angelChests.add(angelChest);
        }
        angelChests.sort(Comparator.comparingLong(de.jeff_media.angelchest.AngelChest::getCreated));
        return angelChests;
    }

    @SuppressWarnings("MagicNumber")
    public static @NotNull String getCardinalDirection(final Player player) {
        double rotation = player.getLocation().getYaw() % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "S";
        }
        if (22.5 <= rotation && rotation < 67.5) {
            return "SW";
        }
        if (67.5 <= rotation && rotation < 112.5) {
            return "W";
        }
        if (112.5 <= rotation && rotation < 157.5) {
            return "NW";
        }
        if (157.5 <= rotation && rotation < 202.5) {
            return "N";
        }
        if (202.5 <= rotation && rotation < 247.5) {
            return "NE";
        }
        if (247.5 <= rotation && rotation < 292.5) {
            return "E";
        }
        if (292.5 <= rotation && rotation < 337.5) {
            return "SE";
        }
        /*if (337.5 <= rotation && rotation < 360.0) {
            return "S";
        }*/
        return "S";

    }

    public static Block getChestLocation(final Block playerLoc) {
        final Main main = Main.getInstance();
        Block fixedAngelChestBlock = playerLoc;

        //if (!playerLoc.getType().equals(Material.AIR)) {
        final List<Block> blocksNearby = getPossibleChestLocations(playerLoc.getLocation());

        if (!blocksNearby.isEmpty()) {
            sortBlocksByDistance(playerLoc, blocksNearby);
            fixedAngelChestBlock = blocksNearby.get(0);
        }
        //}

        if (main.getConfig().getBoolean(Config.NEVER_REPLACE_BEDROCK) && fixedAngelChestBlock.getType() == Material.BEDROCK) {
            final List<Block> nonBedrockBlocksNearby = getNonBedrockBlocks(playerLoc.getLocation());
            if (!nonBedrockBlocksNearby.isEmpty()) {
                sortBlocksByDistance(playerLoc, nonBedrockBlocksNearby);
                fixedAngelChestBlock = blocksNearby.get(0);
            }
        }

        return fixedAngelChestBlock;
    }

    public static List<Block> getNonBedrockBlocks(final Location location) {
        final Main main = Main.getInstance();
        final int radius = main.getConfig().getInt(Config.MAX_RADIUS);
        final List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {

                    final Block block = location.getWorld().getBlockAt(x, y, z);

                    if (block.getType() != Material.BEDROCK && y > 0 && y < location.getWorld().getMaxHeight()) {
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    public static List<Block> getPossibleChestLocations(final Location location) {
        final Main main = Main.getInstance();
        final int radius = main.getConfig().getInt(Config.MAX_RADIUS);
        final List<Block> blocks = new ArrayList<>();
        // TODO: Start with death location. Continue if a location has been found.
        // Fixes problems with too high max-radius values and also avoids having to sort the list
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                zloop:
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {

                    final Block block = location.getWorld().getBlockAt(x, y, z);
                    final Block oneBelow = location.getWorld().getBlockAt(x, y - 1, z);

                    if ((isAir(block.getType()) || main.onlySpawnIn.contains(block.getType())) && !main.dontSpawnOn.contains(oneBelow.getType()) && y > 0 && y < location.getWorld().getMaxHeight()) {
                        //main.verbose("Possible chest loc: "+block.toString());

                        if(main.getConfig().getInt(Config.MINIMUM_AIR_ABOVE_CHEST) > 0) {
                            //if(main.debug) main.debug("Minimum Air above Chest: " + main.getConfig().getInt(Config.MINIMUM_AIR_ABOVE_CHEST));
                            for(int i = 1; i <= main.getConfig().getInt(Config.MINIMUM_AIR_ABOVE_CHEST); i++) {
                                //if(main.debug) main.debug("Checking if " + x + ", " + (y + i) + ", " + z + " is air...");
                                if(!location.getWorld().getBlockAt(x, y + i, z).getType().isAir()) {
                                    //if(main.debug) main.debug("IT ISNT!");
                                    continue zloop;
                                }
                            }
                        }

                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    public static List<Block> getPossibleTPLocations(final Location location, final int radius) {
        final List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    final Block block = location.getWorld().getBlockAt(x, y, z);
                    if (isSafeSpot(location)) blocks.add(block);
                }
            }
        }
        return blocks;
    }

    @SuppressWarnings("SameParameterValue")
    static boolean isAboveLava(final Location loc, final int height) {
        final Block block = loc.getBlock();
        for (int i = 0; i < height; i++) {
            if (block.getRelative(0, -i, 0).getType() == Material.LAVA) return true;

            // TODO: Does this work?
            if (block.getRelative(0, -i, 0).getType().isSolid()) return false;
        }
        return false;
    }

    private static boolean isAir(final Material mat) {
        return mat == Material.AIR || mat == Material.CAVE_AIR || mat.isAir();
    }

    public static boolean isEmpty(final ItemStack[] items) {
        if (items == null) return true;
        return (items.length == 0);
    }

    public static boolean isSafeSpot(final Location location) {

        if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (location.getBlockY() >= MAX_NETHER_HEIGHT) return false;
        }
        if (isAboveLava(location, 10)) return false;

        if (location.getBlockY() <= 0) return false;

        if (location.getBlock().getType().isOccluding()) return false;

        if (location.getBlock().getRelative(0, -1, 0).getType().isSolid() || location.getBlock().getRelative(0, -1, 0).getType() == Material.WATER) {
            return true;
        }

        return false;
    }

    public static void sortBlocksByDistance(final Block angelChestBlock, final List<Block> blocksNearby) {
        blocksNearby.sort((b1, b2)->{
            final double dist1 = b1.getLocation().distance(angelChestBlock.getLocation());
            final double dist2 = b2.getLocation().distance(angelChestBlock.getLocation());
            return Double.compare(dist1, dist2);
        });
    }

    public static boolean spawnChance(final double chance) {
        final Main main = Main.getInstance();

        if (!Daddy.allows(PremiumFeatures.SPAWN_CHANCE)) {
            return true;
        }

        if(main.debug) main.debug("spawn chance = " + chance);
        if (chance >= 1.0) {
            if(main.debug) main.debug("chance >= 1.0, return true");
            return true;
        }
        final int chancePercent = (int) (chance * 100);
        final int random = new Random().nextInt(100); //Returns value between 0 and 99
        if(main.debug) main.debug("chancePercent = " + chancePercent);
        if(main.debug) main.debug("random = " + random);
        if(main.debug) main.debug("(random must be smaller or equal to chancePercent to succeed)");
        if(main.debug) main.debug("return " + (random <= chancePercent));
        return random <= chancePercent;
    }

    public static boolean tryToMergeInventories(final Main main, final AngelChest source, final PlayerInventory dest) {
        final File file = Main.getInstance().logger.getLogFile(source.logfile);
        final Player player = (Player) dest.getHolder();
        if (!Utils.isEmpty(source.overflowInv)) return false; // Already applied inventory

        final ArrayList<ItemStack> overflow = new ArrayList<>();
        final ItemStack[] armor_merged = dest.getArmorContents();
        final ItemStack[] storage_merged = dest.getStorageContents();
        final ItemStack[] extra_merged = dest.getExtraContents();

        // Try to auto-equip armor
        for (int i = 0; i < armor_merged.length; i++) {
            if (Utils.isEmpty(armor_merged[i])) {
                armor_merged[i] = source.armorInv[i];
                main.logger.logItemTaken(player, source.armorInv[i], file);
            } else if (!Utils.isEmpty(source.armorInv[i])) {
                overflow.add(source.armorInv[i]);
            }
            source.armorInv[i] = null;
        }

        // Try keep storage layout
        for (int i = 0; i < storage_merged.length; i++) {
            if (Utils.isEmpty(storage_merged[i])) {
                storage_merged[i] = source.storageInv[i];
                main.logger.logItemTaken(player, source.storageInv[i], file);
            } else if (!Utils.isEmpty(source.storageInv[i])) {
                overflow.add(source.storageInv[i]);
            }
            source.storageInv[i] = null;
        }

        // Try to merge extra (offhand?)
        for (int i = 0; i < extra_merged.length; i++) {
            if (Utils.isEmpty(extra_merged[i])) {
                extra_merged[i] = source.extraInv[i];
                main.logger.logItemTaken(player, source.extraInv[i], file);
            } else if (!Utils.isEmpty(source.extraInv[i])) {
                overflow.add(source.extraInv[i]);
            }
            source.extraInv[i] = null;
        }

        // Apply merged inventories
        dest.setArmorContents(armor_merged);
        dest.setStorageContents(storage_merged);
        dest.setExtraContents(extra_merged);

        // Try to place overflow items into empty storage slots
        final HashMap<Integer, ItemStack> unstorable = dest.addItem(overflow.toArray(new ItemStack[0]));
        source.overflowInv.clear();

        if (unstorable.isEmpty()) {
            main.logger.logLastItemTaken(player, file);
            return true;
        }

        /*source.overflowInv.addItem(unstorable.values()
                .toArray(new ItemStack[0]));*/
        source.storageInv = new ItemStack[source.storageInv.length];
        int i = 0;
        for (final ItemStack item : unstorable.values()) {
            if (item == null) continue;
            source.storageInv[i] = item;
            i++;
        }

        return false;
    }
}
