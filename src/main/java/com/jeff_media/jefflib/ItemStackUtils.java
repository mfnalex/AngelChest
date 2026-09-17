package com.jeff_media.jefflib;

import de.jeff_media.angelchest.utils.HeadCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ItemStackUtils {

    private ItemStackUtils() {
    }

    public static ItemStack fromConfigurationSection(final ConfigurationSection config) {
        return fromConfigurationSection(config, new HashMap<>());
    }

    public static ItemStack fromConfigurationSection(final ConfigurationSection config, final HashMap<String, String> placeholders) {
        final String materialName = config.getString("material", "BARRIER").toUpperCase(Locale.ROOT);
        final int amount = config.getInt("amount", 1);
        final ItemStack item;
        if (materialName.equals("PLAYER_HEAD") && config.isString("base64")) {
            item = HeadCreator.getHead(Objects.requireNonNull(config.getString("base64")));
        } else {
            final Material material = Material.matchMaterial(materialName);
            item = new ItemStack(material == null ? Material.BARRIER : material, amount);
        }

        final ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (config.isSet("lore")) {
            final List<String> lore = config.isString("lore")
                    ? List.of(TextUtils.format(TextUtils.replaceInString(config.getString("lore"), placeholders)))
                    : TextUtils.format(TextUtils.replaceInString(config.getStringList("lore"), placeholders), null);
            meta.setLore(lore);
        }
        if (config.isSet("display-name")) meta.setDisplayName(TextUtils.format(TextUtils.replaceInString(config.getString("display-name"), placeholders)));
        if (config.isInt("custom-model-data")) meta.setCustomModelData(config.getInt("custom-model-data"));
        if (meta instanceof Damageable damageable) damageable.setDamage(config.getInt("damage", 0));

        if (config.isConfigurationSection("enchantments")) {
            for (String name : Objects.requireNonNull(config.getConfigurationSection("enchantments")).getKeys(false)) {
                final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(name));
                if (enchantment == null) throw new IllegalArgumentException("Unknown enchantment: " + name);
                meta.addEnchant(enchantment, config.getConfigurationSection("enchantments").getInt(name, 1), true);
            }
        }
        if (config.getBoolean("prevent-stacking", false)) {
            meta.getPersistentDataContainer().set(PDCUtils.getKey("prevent-stacking"), PersistentDataType.STRING, UUID.randomUUID().toString());
        }
        item.setItemMeta(meta);
        return item;
    }
}
