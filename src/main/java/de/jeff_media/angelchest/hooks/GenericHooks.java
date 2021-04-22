package de.jeff_media.angelchest.hooks;

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

public final class GenericHooks implements Listener {

    //final @Nullable Plugin eliteMobsPlugin;
    final InventoryPagesHook inventoryPagesHook;
    final Main main;
    private  @Nullable Plugin eliteMobsPlugin = null;
    private Boolean isEliteMobsInstalled = null;
    //ArrayList<Entity> hologramsToBeSpawned = new ArrayList<Entity>();
    //boolean hologramToBeSpawned = false;

    public GenericHooks() {
        this.main = Main.getInstance();
        this.inventoryPagesHook = new InventoryPagesHook();
    }

    boolean isDisabledMaterial(final ItemStack item) {
        if (item == null) return false;
        final String type = item.getType().name();
        for (final String mat : main.disabledMaterials) {
            if (mat.equalsIgnoreCase(type)) return true;
        }
        return false;
    }

    /*
     * Okay, the EliteMobs Soulbound is a bit different from other plugins.
     * Other plugins lets you keep those items. EliteMobs only makes it unusable for other players
     * and it should drop normally. So it makes sense to just let those items stay in the chest, right?
     */
    boolean isEliteMobsSoulBound(final ItemStack item) {

        if(isEliteMobsInstalled == null) {
            main.debug("Checking if EliteMobs is installed...");
            eliteMobsPlugin = Bukkit.getPluginManager().getPlugin("EliteMobs");
            if (eliteMobsPlugin == null) {
                isEliteMobsInstalled = false;
                main.debug("It's not. Disabling EliteMobs integration.");
                return false;
            } else {
                main.debug("It is. Enabling EliteMobs integration via PDC values.");
                isEliteMobsInstalled = true;
            }
        }

        if(isEliteMobsInstalled == false) return false;

        if (item == null) return false;

        if (!item.hasItemMeta()) {
            return false;
        }

        final ItemMeta meta = item.getItemMeta();
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(new NamespacedKey(eliteMobsPlugin, "soulbind"), PersistentDataType.STRING)) {
            main.debug(item.toString() + " is a EliteMobs soulbound item, which means we must treat it like a normal item because EliteMobs soulbound items are NOT kept on death, they drop like normal items!");
            return true;
        }
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
        if(main.getConfig().getBoolean(Config.EXEMPT_ELITEMOBS_SOULBOUND_ITEMS_FROM_GENERIC_SOULBOUND_DETECTION)) {
            if (isEliteMobsSoulBound(item)) {
                return false; // EliteMobs Soulbound items have Soulboud in the lore BUT ARE NOT KEPT ON DEATH BUT DROP NORMALLY
            }
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
            if (enchant.getKey().getKey().equalsIgnoreCase("soulbound")) {
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
        //if (isEliteMobsSoulBound(item)) return true; // EliteMobs soulbound items are normally NOT kept on death but drop normally
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