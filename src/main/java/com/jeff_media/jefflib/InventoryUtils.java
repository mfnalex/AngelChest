package com.jeff_media.jefflib;

import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    public static boolean addOrDrop(final Player player, final ItemStack item) {
        final Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        leftovers.values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        return leftovers.isEmpty();
    }
}
