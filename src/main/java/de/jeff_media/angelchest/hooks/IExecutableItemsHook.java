package de.jeff_media.angelchest.hooks;

import org.bukkit.inventory.ItemStack;

public class IExecutableItemsHook {

    public boolean isKeptOnDeath(ItemStack item) {
        return false;
    }
}
