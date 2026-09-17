package com.jeff_media.jefflib.pluginhooks;

import com.jeff_media.jefflib.EnchantmentUtils;
import com.jeff_media.jefflib.PDCUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class McMMOUtils {

    private static final NamespacedKey SUPER_ABILITY_KEY = PDCUtils.getKeyFromString("mcmmo", "super_ability_boosted");

    private McMMOUtils() {
    }

    public static void removeSuperAbilityBoost(final ItemStack item) {
        if (!isSuperAbilityBoosted(item)) return;
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.removeEnchant(EnchantmentUtils.DIG_SPEED_ENCHANTMENT);
        final int originalLevel = PDCUtils.getOrDefault(item, SUPER_ABILITY_KEY, PersistentDataType.INTEGER, 0);
        if (originalLevel > 0) meta.addEnchant(EnchantmentUtils.DIG_SPEED_ENCHANTMENT, originalLevel, true);
        item.setItemMeta(meta);
        PDCUtils.remove(item, SUPER_ABILITY_KEY);
    }

    private static boolean isSuperAbilityBoosted(final ItemStack item) {
        return item != null && item.hasItemMeta() && PDCUtils.has(item, SUPER_ABILITY_KEY, PersistentDataType.INTEGER);
    }
}
