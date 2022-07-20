package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.handlers.ItemManager;
import de.jeff_media.angelchest.nbt.NBTTags;
import com.jeff_media.jefflib.PDCUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtils {

    private static final Main main = Main.getInstance();

    public static boolean checkForAndRemoveOneItem(String itemId, PlayerInventory inventory, ItemStack vanillaItem) {

        if(main.debug) {
            main.debug("Checking if player has the following custom item:");
            main.debug("ItemID: " + itemId);
            main.debug("Exact item: " + vanillaItem);
        }

        //ItemStack priceItem = Main.getInstance().getItemManager().getItem(itemId);
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType() == Material.AIR || item.getAmount() == 0) continue;

            if(main.debug) {
                main.debug("  Checking item " + item);
            }

            // Custom saved items
            if(vanillaItem != null && vanillaItem.isSimilar(item)) {
                item.setAmount(item.getAmount() - 1);
                inventory.setItem(i, item);
                return true;
            }

            // Vanilla items
            if(itemId == null && Main.getInstance().getItemManager().isVanillaItem(vanillaItem) && item.isSimilar(vanillaItem)) {
                item.setAmount(item.getAmount() - 1);
                inventory.setItem(i, item);
                return true;
            }

            // Normal custom items
            if(PDCUtils.has(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING)) {
                if(PDCUtils.get(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING).equals(itemId)) {
                    item.setAmount(item.getAmount() - 1);
                    inventory.setItem(i, item);
                    return true;
                }
            }
        }
        main.debug("  No, this is not the custom item.");
        return false;
    }
}
