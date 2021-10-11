package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.jefflib.PDCUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.persistence.PersistentDataType;

public class CraftingListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        for(ItemStack item : event.getInventory().getMatrix()) {
            if(item == null || item.getType().isAir() || item.getAmount()==0) continue;
            if(PDCUtils.has(item, NBTTags.IS_TOKEN_ITEM,PersistentDataType.BYTE)) {
                event.setCancelled(true);
            }
            return;
        }
    }
}
