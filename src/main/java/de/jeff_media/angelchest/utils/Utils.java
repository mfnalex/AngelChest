package de.jeff_media.angelchest.utils;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
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


public final class Utils {

    public static boolean isEmpty(final Inventory inv) {

        if (inv.getContents() == null)
            return true;
        if (inv.getContents().length == 0)
            return true;

        for (final ItemStack itemStack : inv.getContents()) {
            if (itemStack == null)
                continue;
            if (itemStack.getAmount() == 0)
                continue;
            if (itemStack.getType() == Material.AIR)
                continue;
            return false;
        }

        return true;

    }

    /**
     * Checks whether an ItemStack is null/empty or not
     *
     * @param itemStack ItemStack to check or null
     * @return true if ItemStack is null, amount is 0 or material is AIR
     */
    public static boolean isEmpty(@Nullable final ItemStack itemStack) {
        if (itemStack == null)
            return true;
        if (itemStack.getAmount() == 0)
            return true;
        return itemStack.getType() == Material.AIR;
    }

    /**
     * Checks whether a world is enabled or if it's on the blacklist
     *
     * @param world World to check
     * @return true if world is enabled, false if it's on the blacklist
     */
    public static boolean isWorldEnabled(final World world) {
        for (final String worldName : Main.getInstance().disabledWorlds) {
            if (world.getName().equalsIgnoreCase(worldName)) {
                return false;
            }
        }
        return true;
    }

    public static EventPriority getEventPriority(final String configuredPriority) {
        return Enums.getIfPresent(EventPriority.class, configuredPriority).or(EventPriority.NORMAL);
    }

    public static void dropItems(final Block block, final ItemStack[] invContent) {
        for (final ItemStack itemStack : invContent) {
            if (isEmpty(itemStack))
                continue;
            block.getWorld().dropItem(block.getLocation(), itemStack);
        }
    }

    public static void dropExp(final Block block, final int xp) {
        final ExperienceOrb orb = (ExperienceOrb) block.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
        orb.setExperience(xp);
    }

    public static void sendDelayedMessage(final Player p, final String message, final long delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            if (p == null)
                return;
            if (!(p instanceof Player))
                return;
            if (!p.isOnline())
                return;
            p.sendMessage(message);
        }, delay);
    }

    public static void applyXp(final Player p, final AngelChest angelChest) {
        if (angelChest.experience > 0) {
            p.giveExp(angelChest.experience);
            angelChest.experience = 0;
        }
    }

}