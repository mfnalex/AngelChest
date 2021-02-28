package de.jeff_media.AngelChestPlus.utils;

import de.jeff_media.AngelChestPlus.config.Config;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.hooks.InventoryPagesHook;
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

public class HookUtils implements Listener {

    final Main main;
    final InventoryPagesHook inventoryPagesHook;
    final @Nullable Plugin eliteMobsPlugin;
    //ArrayList<Entity> hologramsToBeSpawned = new ArrayList<Entity>();
    //boolean hologramToBeSpawned = false;

    public HookUtils(Main main) {
        this.main=main;
        this.inventoryPagesHook = new InventoryPagesHook(main);
        this.eliteMobsPlugin = Bukkit.getPluginManager().getPlugin("EliteMobs");
    }

    boolean isSlimefunSoulbound(ItemStack item) {
        if(item==null) return false;
        if(!main.getConfig().getBoolean(Config.USE_SLIMEFUN)) return false;

        try {
            Class.forName("io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils");
            return SlimefunUtils.isSoulbound(item);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            main.getConfig().set(Config.USE_SLIMEFUN,false);
            return false;
        }
    }

    boolean isNativeSoulbound(ItemStack item) {
        if(item==null) return false;
        if(!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if(!meta.hasEnchants()) return false;
        for(Enchantment enchant : meta.getEnchants().keySet()) {
            if(enchant.getKey().getKey().equals("soulbound")) {
                return true;
            }
        }
        return false;
    }

    boolean isEliteMobsSoulBound(ItemStack item) {

        if(item==null) return false;
        //main.debug("Checking if "+item.toString()+" is EliteMobs soulbound...");

        if(!item.hasItemMeta()) {
            //main.debug("NO: No item meta");
            return false;
        }
        if(eliteMobsPlugin==null) {
            //main.debug("NO: EliteMobs == null");
            return false;
        }


        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if(pdc.has(new NamespacedKey(eliteMobsPlugin,"soulbind"), PersistentDataType.STRING)) {
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

    boolean isGenericSoulbound(ItemStack item) {
        if(item==null) return false;
        if(!main.getConfig().getBoolean(Config.CHECK_GENERIC_SOULBOUND)) return false;

        if(!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();

        if(meta.getLore()==null) {
            return false;
        }

        for(String line : meta.getLore()) {
            if(line.toLowerCase().contains("soulbound")) {
                main.debug(item.toString() + "is a GENERIC SOULBOUND ITEM. Lore: " + line);
                return true;
            }
        }
        return false;
    }

    boolean isDisabledMaterial(ItemStack item) {
        if(item==null) return false;
        String type = item.getType().name();
        for(String mat : main.disabledMaterials) {
            if(mat.equalsIgnoreCase(type)) return true;
        }
        return false;
    }

    public boolean removeOnDeath(ItemStack item) {
        if(item==null) return false;
        //if(isEliteMobsSoulBound(item)) return true;
        if(isDisabledMaterial(item)) return true;
        if(inventoryPagesHook.isButton(item)) return true;
        if(isGenericSoulbound(item)) return true;
        return false;
    }

    public boolean keepOnDeath(ItemStack item) {
        if(item==null) return false;
        if( isSlimefunSoulbound(item)) return true;
        if(isEliteMobsSoulBound(item)) return true;
        if(isNativeSoulbound(item)) return true;
        return false;
    }

}