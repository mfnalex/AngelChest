package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.hooks.ExecutableItemsHook;
import de.jeff_media.angelchest.hooks.InventoryPagesHook;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public final class HookUtils implements Listener {

    final @Nullable Plugin eliteMobsPlugin;
    final InventoryPagesHook inventoryPagesHook;
    final Main main;
    //ArrayList<Entity> hologramsToBeSpawned = new ArrayList<Entity>();
    //boolean hologramToBeSpawned = false;

    public HookUtils() {
        this.main = Main.getInstance();
        this.inventoryPagesHook = new InventoryPagesHook();
        this.eliteMobsPlugin = Bukkit.getPluginManager().getPlugin("EliteMobs");
    }

    boolean isDisabledMaterial(final ItemStack item) {
        if (item == null) return false;
        final String type = item.getType().name();
        for (final String mat : main.disabledMaterials) {
            if (mat.equalsIgnoreCase(type)) return true;
        }
        return false;
    }

    boolean isEliteMobsSoulBound(final ItemStack item) {

        if (item == null) return false;
        //main.debug("Checking if "+item.toString()+" is EliteMobs soulbound...");

        if (!item.hasItemMeta()) {
            //main.debug("NO: No item meta");
            return false;
        }
        if (eliteMobsPlugin == null) {
            //main.debug("NO: EliteMobs == null");
            return false;
        }


        final ItemMeta meta = item.getItemMeta();
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(new NamespacedKey(eliteMobsPlugin, "soulbind"), PersistentDataType.STRING)) {
            main.debug(item.toString() + " is a EliteMobs soulbound item");
            return true;
        }
        /*main.debug("NO: NamespacedKey not found.");
        main.debug("Debugging PDC:");
        for(NamespacedKey key : pdc.getKeys()) {
            main.debug("- "+key);
        }*/
        return false;
    }

    boolean isGenericSoulbound(final ItemStack item) {
        if (item == null) return false;
        if (!main.getConfig().getBoolean(Config.CHECK_GENERIC_SOULBOUND)) return false;

        if (!item.hasItemMeta()) return false;
        final ItemMeta meta = item.getItemMeta();

        if (meta.getLore() == null) {
            return false;
        }

        for (final String line : meta.getLore()) {
            if (line.toLowerCase().contains("soulbound")) {
                main.debug(item.toString() + "is a GENERIC SOULBOUND ITEM. Lore: " + line);
                return true;
            }
        }
        return false;
    }

    boolean isNativeSoulbound(final ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        final ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchants()) return false;
        for (final Enchantment enchant : meta.getEnchants().keySet()) {
            if (enchant.getKey().getKey().equals("soulbound")) {
                return true;
            }
        }
        return false;
    }

    boolean isSlimefunSoulbound(final ItemStack item) {
        if (item == null) return false;
        if (!main.getConfig().getBoolean(Config.USE_SLIMEFUN)) return false;

        try {
            Class.forName("io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils");
            return SlimefunUtils.isSoulbound(item);
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            main.getConfig().set(Config.USE_SLIMEFUN, false);
            return false;
        }
    }

    public boolean keepOnDeath(final ItemStack item) {
        if (item == null) return false;
        if (isSlimefunSoulbound(item)) return true;
        if (isEliteMobsSoulBound(item)) return true;
        if (isNativeSoulbound(item)) return true;
        if (ExecutableItemsHook.isKeptOnDeath(item)) return true;
        return false;
    }

    public boolean removeOnDeath(final ItemStack item) {
        if (item == null) return false;
        //if(isEliteMobsSoulBound(item)) return true;
        if (isDisabledMaterial(item)) return true;
        if (inventoryPagesHook.isButton(item)) return true;
        if (isGenericSoulbound(item)) return true;
        return false;
    }
}