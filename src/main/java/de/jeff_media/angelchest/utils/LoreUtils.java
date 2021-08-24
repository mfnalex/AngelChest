package de.jeff_media.angelchest.utils;

import de.jeff_media.jefflib.TextUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LoreUtils {
    public static @Nullable List<String> applyNewlines(String text) {
        if(text == null || text.equals("") || text.length()==0) {
            return null;
        }
        List<String> lore = new ArrayList<>();
        String[] split = text.split("\\n");
        for(String line : split) {
            lore.add(TextUtils.format(line));
        }
        return lore;
    }

    public static void applyLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
