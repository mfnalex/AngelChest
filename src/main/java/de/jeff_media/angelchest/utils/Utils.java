package de.jeff_media.angelchest.utils;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;


public final class Utils {

    public static void applyXp(final Player p, final AngelChest angelChest) {
        if (angelChest.experience > 0) {
            p.giveExp(angelChest.experience);
            angelChest.experience = 0;
        }
    }

    public static void dropExp(final Block block, final int xp) {
        final ExperienceOrb orb = (ExperienceOrb) block.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
        orb.setExperience(xp);
    }

    public static void dropItems(final Block block, final ItemStack[] invContent) {
        for (final ItemStack itemStack : invContent) {
            if (isEmpty(itemStack)) continue;
            block.getWorld().dropItem(block.getLocation(), itemStack);
        }
    }

    public static EventPriority getEventPriority(final String configuredPriority) {
        return Enums.getIfPresent(EventPriority.class, configuredPriority.toUpperCase(Locale.ROOT)).or(EventPriority.NORMAL);
    }

    public static boolean isEmpty(final Inventory inv) {

        if (inv.getContents() == null) return true;
        if (inv.getContents().length == 0) return true;

        for (final ItemStack itemStack : inv.getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getAmount() == 0) continue;
            if (itemStack.getType() == Material.AIR) continue;
            return false;
        }

        return true;

    }

    public static int getMaxOpenDistance(Player player) {
        int confDefault = AngelChestMain.getInstance().getConfig().getInt(Config.MAX_OPEN_DISTANCE);
        //System.out.println("confDefault = " + confDefault);
        int maxPerm = player.getEffectivePermissions().stream().filter(info -> info.getValue() && info.getPermission().startsWith(Permissions.OPEN + ".")).map(info -> info.getPermission().substring((Permissions.OPEN + ".").length())).mapToInt(value -> {
            //System.out.println("Found permission: " + value);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return -1;
            }
        }).max().orElse(-1);
        //System.out.println("maxPerm = " + maxPerm);
        if(maxPerm == -1) {
            //System.out.println("it's -1, returning confDefault: " + confDefault);
            return confDefault;
        }
        //System.out.println("returning maxPerm: " + maxPerm);
        return maxPerm;

    }

    /**
     * Checks whether an ItemStack is null/empty or not
     *
     * @param itemStack ItemStack to check or null
     * @return true if ItemStack is null, amount is 0 or material is AIR
     */
    public static boolean isEmpty(@Nullable final ItemStack itemStack) {
        if (itemStack == null) return true;
        if (itemStack.getAmount() == 0) return true;
        return itemStack.getType() == Material.AIR;
    }

    /**
     * Checks whether a world is enabled or if it's on the blacklist
     *
     * @param world World to check
     * @return true if world is enabled, false if it's on the blacklist
     */
    public static boolean isWorldEnabled(final World world) {
        for (final String worldName : AngelChestMain.getInstance().disabledWorlds) {
            if (world.getName().equalsIgnoreCase(worldName)) {
                return false;
            }
        }
        return true;
    }

    public static void sendDelayedMessage(final Player p, final String message, final long delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(AngelChestMain.getInstance(), () -> {
            if (p == null) return;
            if (!(p instanceof Player)) return;
            if (!p.isOnline()) return;
            Messages.send(p, message);
        }, delay);
    }

}