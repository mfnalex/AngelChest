package de.jeff_media.AngelChest.utils;

import de.jeff_media.AngelChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class BlacklistUtils {

    private final static String indent = "  ";

    public static String[] addToBlacklist(ItemStack item, String name) {

        Main main = Main.getInstance();

        File blacklistFile = new File(main.getDataFolder(),"blacklist.yml");

        if(main.itemBlacklist.containsKey(name)) {
            return null;
        }

        if(!blacklistFile.exists()) {
            try {
                blacklistFile.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        Material material = item.getType();
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(material);
        String metaName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        List<String> metaLore = meta.hasLore() ? meta.getLore() : null;

        StringBuilder stringBuilder = new StringBuilder(name).append(":").append(System.lineSeparator());
        stringBuilder.append(indent).append("material: ").append(material.name()).append(System.lineSeparator());
        if(metaName != null) {
            stringBuilder.append(indent).append("nameExact: ").append("\"").append(metaName).append("\"").append(System.lineSeparator());
        }
        if(metaLore != null) {
            stringBuilder.append(indent).append("loreExact:").append(System.lineSeparator());
            for(String line : metaLore) {
                stringBuilder.append(indent).append(indent).append("- \"").append(line.replace("&","&&").replace('§','&')).append("\"").append(System.lineSeparator());
            }
        }
        String[] lines = stringBuilder.toString().split(System.lineSeparator());
        FileUtils.appendLines(blacklistFile,lines);
        return lines;
    }

}
