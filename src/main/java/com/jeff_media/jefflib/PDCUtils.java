package com.jeff_media.jefflib;

import de.jeff_media.angelchest.AngelChestMain;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

/** Small PDC facade kept source-compatible with the former JeffLib calls. */
public final class PDCUtils {

    private PDCUtils() {
    }

    private static NamespacedKey key(final String key) {
        return new NamespacedKey(Objects.requireNonNull(AngelChestMain.getInstance(), "AngelChest is not initialized"), key);
    }

    public static NamespacedKey getKey(final String key) {
        return key(key);
    }

    public static NamespacedKey getKeyFromString(final String namespace, final String key) {
        return NamespacedKey.fromString(namespace + ":" + key);
    }

    public static <T, Z> void set(final PersistentDataHolder holder, final String key, final PersistentDataType<T, Z> type, final Z value) {
        set(holder, key(key), type, value);
    }

    public static <T, Z> void set(final PersistentDataHolder holder, final NamespacedKey key, final PersistentDataType<T, Z> type, final Z value) {
        holder.getPersistentDataContainer().set(key, type, value);
    }

    public static <T, Z> void set(final ItemStack item, final String key, final PersistentDataType<T, Z> type, final Z value) {
        set(item, key(key), type, value);
    }

    public static <T, Z> void set(final ItemStack item, final NamespacedKey key, final PersistentDataType<T, Z> type, final Z value) {
        final ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta == null) return;
        meta.getPersistentDataContainer().set(key, type, value);
        item.setItemMeta(meta);
    }

    public static <T, Z> Z get(final PersistentDataHolder holder, final String key, final PersistentDataType<T, Z> type) {
        return get(holder, key(key), type);
    }

    public static <T, Z> Z get(final PersistentDataHolder holder, final NamespacedKey key, final PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().get(key, type);
    }

    public static <T, Z> Z get(final ItemStack item, final String key, final PersistentDataType<T, Z> type) {
        return get(item, key(key), type);
    }

    public static <T, Z> Z get(final ItemStack item, final NamespacedKey key, final PersistentDataType<T, Z> type) {
        if (!item.hasItemMeta() || item.getItemMeta() == null) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, type);
    }

    public static <T, Z> Z getOrDefault(final PersistentDataHolder holder, final String key, final PersistentDataType<T, Z> type, final Z defaultValue) {
        return holder.getPersistentDataContainer().getOrDefault(key(key), type, defaultValue);
    }

    public static <T, Z> Z getOrDefault(final ItemStack item, final NamespacedKey key, final PersistentDataType<T, Z> type, final Z defaultValue) {
        if (!item.hasItemMeta() || item.getItemMeta() == null) return defaultValue;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(key, type, defaultValue);
    }

    public static <T, Z> Z getOrDefault(final ItemStack item, final String key, final PersistentDataType<T, Z> type, final Z defaultValue) {
        return getOrDefault(item, key(key), type, defaultValue);
    }

    public static <T, Z> boolean has(final PersistentDataHolder holder, final String key, final PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().has(key(key), type);
    }

    public static <T, Z> boolean has(final PersistentDataHolder holder, final NamespacedKey key, final PersistentDataType<T, Z> type) {
        return holder.getPersistentDataContainer().has(key, type);
    }

    public static boolean has(final PersistentDataHolder holder, final NamespacedKey key) {
        return holder.getPersistentDataContainer().getKeys().contains(key);
    }

    public static <T, Z> boolean has(final ItemStack item, final String key, final PersistentDataType<T, Z> type) {
        return has(item, key(key), type);
    }

    public static <T, Z> boolean has(final ItemStack item, final NamespacedKey key, final PersistentDataType<T, Z> type) {
        return item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().has(key, type);
    }

    public static boolean has(final ItemStack item, final NamespacedKey key) {
        return item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().getKeys().contains(key);
    }

    public static void remove(final PersistentDataHolder holder, final String key) {
        holder.getPersistentDataContainer().remove(key(key));
    }

    public static void remove(final PersistentDataHolder holder, final NamespacedKey key) {
        holder.getPersistentDataContainer().remove(key);
    }

    public static void remove(final ItemStack item, final String key) {
        remove(item, key(key));
    }

    public static void remove(final ItemStack item, final NamespacedKey key) {
        if (!item.hasItemMeta() || item.getItemMeta() == null) return;
        item.getItemMeta().getPersistentDataContainer().remove(key);
        item.setItemMeta(item.getItemMeta());
    }

    public static Set<NamespacedKey> getKeys(final PersistentDataHolder holder) {
        return holder.getPersistentDataContainer().getKeys();
    }
}
