package de.jeff_media.angelchest.hooks;

import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.inventory.ItemStack;

public class AdvancedEnchantmentsHook {

    public static boolean hasWhiteScroll(ItemStack item) {
        return AEAPI.hasWhitescroll(item);
    }

}
