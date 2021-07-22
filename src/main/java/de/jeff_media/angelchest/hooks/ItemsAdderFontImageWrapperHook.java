package de.jeff_media.angelchest.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ItemsAdderFontImageWrapperHook {

    private static final Plugin itemsAdderPlugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");

    public static String translate(String text) {
        if(itemsAdderPlugin == null) return text;
        return dev.lone.itemsadder.api.FontImages.FontImageWrapper.replaceFontImages(text);
    }

}
