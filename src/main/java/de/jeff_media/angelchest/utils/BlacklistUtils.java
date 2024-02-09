package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.AngelChestMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class BlacklistUtils {

    private static final String indent = "  ";

    public static String[] addToBlacklist(final ItemStack item, final String name) {

        final AngelChestMain main = AngelChestMain.getInstance();

        final File blacklistFile = new File(main.getDataFolder(), "blacklist.yml");

        if (main.itemBlacklist.containsKey(name)) {
            return null;
        }

        if (!blacklistFile.exists()) {
            try {
                blacklistFile.createNewFile();
            } catch (final IOException ioException) {
                ioException.printStackTrace();
            }
        }

        final Material material = item.getType();
        final ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(material);
        final List<String> enchantments = new ArrayList<>();
        for (final Enchantment enchantment : meta.getEnchants().keySet()) {
            enchantments.add(enchantment.getKey().getKey());
        }
        assert meta != null;
        final String metaName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        final List<String> metaLore = meta.hasLore() ? meta.getLore() : null;
        final int modelData = meta.hasCustomModelData() ? meta.getCustomModelData() : -1;

        final List<String> pdcKeys = new ArrayList<>();
        for (final NamespacedKey namespacedKey : meta.getPersistentDataContainer().getKeys()) {
            pdcKeys.add(namespacedKey.toString());
        }

        final StringBuilder stringBuilder = new StringBuilder(name).append(":").append(System.lineSeparator());
        stringBuilder.append(indent).append("material: ").append(material.name()).append(System.lineSeparator());
        if (metaName != null) {
            stringBuilder.append(indent).append("nameExact: ").append("\"").append(metaName).append("\"").append(System.lineSeparator());
        }
        if (metaLore != null) {
            stringBuilder.append(indent).append("loreExact:").append(System.lineSeparator());
            for (final String line : metaLore) {
                stringBuilder.append(indent).append(indent).append("- \"").append(line.replace("&", "&&").replace('§', '&')).append("\"").append(System.lineSeparator());
            }
        }
        if (!enchantments.isEmpty()) {
            stringBuilder.append(indent).append("enchantments: ").append(System.lineSeparator());
            for (final String enchantment : enchantments) {
                stringBuilder.append(indent).append(indent).append("- ").append(enchantment).append(System.lineSeparator());
            }
        }
        if(!pdcKeys.isEmpty()) {
            stringBuilder.append(indent).append("pdcKeys: ").append(System.lineSeparator());
            for (final String pdcKey : pdcKeys) {
                stringBuilder.append(indent).append(indent).append("- ").append(pdcKey).append(System.lineSeparator());
            }
        }
        stringBuilder.append(indent).append("customModelData:").append(System.lineSeparator());
        stringBuilder.append(indent).append(indent).append("min: ").append(modelData).append(System.lineSeparator());
        stringBuilder.append(indent).append(indent).append("max: ").append(modelData).append(System.lineSeparator());
        final String[] lines = stringBuilder.toString().split(System.lineSeparator());
        FileUtils.appendLines(blacklistFile, lines);
        return lines;
    }

}
