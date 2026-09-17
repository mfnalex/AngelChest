package com.jeff_media.jefflib;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

/** Compatibility wrapper for the string PDC operations used by AngelChest. */
public final class NBTAPI {

    private NBTAPI() {
    }

    private static NamespacedKey key(final String key) {
        return PDCUtils.getKey(key);
    }

    public static boolean hasNBT(final PersistentDataHolder holder, final String key) {
        return holder.getPersistentDataContainer().has(key(key), PersistentDataType.STRING);
    }

    public static String getNBT(final PersistentDataHolder holder, final String key) {
        return holder.getPersistentDataContainer().get(key(key), PersistentDataType.STRING);
    }

    public static String getNBT(final PersistentDataHolder holder, final String key, final String defaultValue) {
        return holder.getPersistentDataContainer().getOrDefault(key(key), PersistentDataType.STRING, defaultValue);
    }

    public static boolean hasNBT(final ItemStack item, final String key) {
        return item.hasItemMeta() && item.getItemMeta() != null && hasNBT(item.getItemMeta(), key);
    }

    public static String getNBT(final ItemStack item, final String key) {
        return item.hasItemMeta() && item.getItemMeta() != null ? getNBT(item.getItemMeta(), key) : null;
    }

    public static void addNBT(final PersistentDataHolder holder, final String key, final String value) {
        holder.getPersistentDataContainer().set(key(key), PersistentDataType.STRING, Objects.requireNonNull(value, "Value must not be null"));
    }

    public static void addNBT(final ItemStack item, final String key, final String value) {
        final ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta == null) return;
        addNBT(meta, key, value);
        item.setItemMeta(meta);
    }

    public static void removeNBT(final PersistentDataHolder holder, final String key) {
        holder.getPersistentDataContainer().remove(key(key));
    }

    public static void removeNBT(final ItemStack item, final String key) {
        if (!item.hasItemMeta() || item.getItemMeta() == null) return;
        removeNBT(item.getItemMeta(), key);
        item.setItemMeta(item.getItemMeta());
    }
}
