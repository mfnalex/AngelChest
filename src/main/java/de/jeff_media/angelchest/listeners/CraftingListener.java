package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.jefflib.PDCUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.persistence.PersistentDataType;

public class CraftingListener implements Listener {

    private static final Main main = Main.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(PrepareItemCraftEvent event) {
        //System.out.println("Prepare craft event");
        for(ItemStack item : event.getInventory().getMatrix()) {
            if(item == null || item.getType().isAir() || item.getAmount()==0) continue;
            //System.out.println("Item: " + item);
            if(PDCUtils.has(item, NBTTags.IS_TOKEN_ITEM,PersistentDataType.STRING)) {
                //System.out.println("Cancelling!");
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        main.getItemManager().autodiscover(event.getPlayer());
    }
}
