package de.jeff_media.angelchest.utils;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.data.AngelChest;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;


public final class Utils {

    public static boolean isEmpty(Inventory inv) {

        if (inv.getContents() == null)
            return true;
        if (inv.getContents().length == 0)
            return true;

        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack == null)
                continue;
            if (itemStack.getAmount() == 0)
                continue;
            if (itemStack.getType().equals(Material.AIR))
                continue;
            return false;
        }

        return true;

    }

    /**
     * Checks whether an ItemStack is null/empty or not
     * @param itemStack ItemStack to check or null
     * @return true if ItemStack is null, amount is 0 or material is AIR
     */
    public static boolean isEmpty(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return true;
        if (itemStack.getAmount() == 0)
            return true;
        return itemStack.getType().equals(Material.AIR);
    }

    /**
     * Checks whether a world is enabled or if it's on the blacklist
     * @param world World to check
     * @return true if world is enabled, false if it's on the blacklist
     */
    public static boolean isWorldEnabled(World world) {
        for (String worldName : Main.getInstance().disabledWorlds) {
            if (world.getName().equalsIgnoreCase(worldName)) {
                return false;
            }
        }
        return true;
    }

    public static EventPriority getEventPriority(String configuredPriority) {
        return Enums.getIfPresent(EventPriority.class, configuredPriority).or(EventPriority.NORMAL);
    }

    public static void dropItems(Block block, ItemStack[] invContent) {
        for (ItemStack itemStack : invContent) {
            if (Utils.isEmpty(itemStack))
                continue;
            block.getWorld().dropItem(block.getLocation(), itemStack);
        }
    }

    public static void dropExp(Block block, int xp) {
        ExperienceOrb orb = (ExperienceOrb) block.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
        orb.setExperience(xp);
    }

    /*public static void dropItems(Block block, Inventory inv) {
        dropItems(block, inv.getContents());
        inv.clear();
    }*/

    public static void sendDelayedMessage(Player p, String message, long delay) {
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

    public static void applyXp(Player p, AngelChest angelChest) {
        if (angelChest.experience > 0) {
            p.giveExp(angelChest.experience);
            angelChest.experience = 0;
        }
    }

    public static Integer parseInteger(String string) {
        if(string.matches("^[0-9]+$")) {
            return Integer.parseInt(string);
        }
        return null;
    }

    /*
    public static boolean itemStacksAreEqual(ItemStack i1, ItemStack i2) {
		if(i1.getType() != i2.getType()) return false;
		if(i1.getAmount() != i2.getAmount()) return false;
		if(i1.hasItemMeta() != i2.hasItemMeta()) return false;
		ItemMeta m1 = i1.getItemMeta();
		ItemMeta m2 = i2.getItemMeta();
		if(m1.getDisplayName() != null) {
			if(!m1.getDisplayName().equals(m2.getDisplayName())) return false;
		} else {
			if(m2.getDisplayName() != null) return false;
		}

		}
	}*/
}