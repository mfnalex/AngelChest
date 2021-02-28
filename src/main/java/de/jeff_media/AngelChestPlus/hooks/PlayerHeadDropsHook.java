/*
package de.jeff_media.AngelChestPlus.hooks;

import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PlayerHeadDropsHook {

    public static void applyPlayerHeadDrops(PlayerInventory inventory, List<ItemStack> drops) {

        Main main = Main.getInstance();

        for(ItemStack item : drops) {
            if(item == null) {
                main.debug("false: null");
                continue;
            }
            main.debug("Checking if drop is Vanilla Tweaks Player Head Drop: "+item.toString());
            if(!item.getType().name().equals("PLAYER_HEAD")) {
                main.debug("false: type != PLAYER_HEAD");
                continue;
            }
            if(!item.hasItemMeta()) {
                main.debug("false: no item meta");
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if(meta.getLore()==null) {
                main.debug("false: lore == null");
                continue;
            }
            if(meta.getLore().size()!=1) {
                main.debug("false: lore size != 1");
                continue;
            }
            String lore1 = ChatColor.stripColor(meta.getLore().get(0));
            main.debug("lore: "+lore1);
            if(!lore1.startsWith("Killed by ")) {
                main.debug("false: lore line 0 wrong prefix");
                continue;
            }
            main.debug("AngelChest detected Player Head Drops head");
            inventory.addItem(item);
        }

    }

}
*/
