package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.jefflib.PDCUtils;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtils {

    public static boolean checkForAndRemoveOneItem(String itemId, PlayerInventory inventory) {
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType() == Material.AIR || item.getAmount() == 0) continue;
            if(PDCUtils.has(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING)) {
                if(PDCUtils.get(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING).equals(itemId)) {
                    item.setAmount(item.getAmount() - 1);
                    inventory.setItem(i, item);
                    return true;
                }
            }
        }
        return false;
    }
}
