package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import org.bukkit.NamespacedKey;

public class EqualUtils {

    private static final NamespacedKey SLOT_KEY = new NamespacedKey(Main.getInstance(), "slot");

    {
        //CopyOnWriteArrayList
    }
//
//    public static void applySlotTags(PlayerInventory inv) {
//        for(int i = 0; i < inv.getSize(); i++) {
//            ItemStack item = inv.getItem(i);
//            if(item == null) continue;
//            PDCUtils.set(item, SLOT_KEY, PersistentDataType.INTEGER, i);
//        }
//    }
//
//    public static void removeSlotTags(PlayerInventory inv) {
//        for(int i = 0; i < inv.getSize(); i++) {
//            ItemStack item = inv.getItem(i);
//            if(item == null) continue;
//            PDCUtils.remove(item, SLOT_KEY);
//        }
//    }
//
//    public static <T> boolean contains(Collection<T> collection, T element) {
//        for(T t : collection) {
//            if(t == element) {
//                return true;
//            }
//        }
//        return false;
//    }

}
