package com.jeff_media.jefflib;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EnchantmentUtils {

    public static final Enchantment DIG_SPEED_ENCHANTMENT = Enchantment.EFFICIENCY;

    private EnchantmentUtils() {
    }

    public static boolean addGlowEffect(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.hasEnchantmentGlintOverride()) return false;
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return true;
    }
}
