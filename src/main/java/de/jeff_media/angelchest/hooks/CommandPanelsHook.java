package de.jeff_media.angelchest.hooks;

import com.jeff_media.jefflib.PDCUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class CommandPanelsHook {

    private static final NamespacedKey KEY = PDCUtils.getKeyFromString("commandpanels","commandpanelsitem");

    private static Boolean IS_COMMANDPANELS_INSTALLED = null;

    private static boolean isCommandPanelsInstalled() {
        if(IS_COMMANDPANELS_INSTALLED == null) {
            IS_COMMANDPANELS_INSTALLED = Bukkit.getPluginManager().getPlugin("CommandPanels") != null;
        }
        return IS_COMMANDPANELS_INSTALLED;
    }

    public static boolean isCommandPanelsIgnoredItem(ItemStack item) {
        if(!isCommandPanelsInstalled()) {
            return false;
        }
        if(item == null) return false;
        if(!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().getKeys().contains(KEY);
    }

}
