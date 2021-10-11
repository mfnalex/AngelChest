package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.Main;
import de.jeff_media.jefflib.ItemStackUtils;
import de.jeff_media.jefflib.PDCUtils;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class ItemManager {

    private static final Main main = Main.getInstance();

    private HashMap<String, ItemStack> items;

    public ItemManager() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "items.yml"));

        items = new HashMap<>();

        for(String itemId : yaml.getKeys(false)) {
            ItemStack item = ItemStackUtils.fromConfigurationSection(yaml.getConfigurationSection(itemId));
            PDCUtils.set(item, "token", PersistentDataType.STRING,itemId);
            items.put(itemId, item);
        }
    }

    @Nullable
    public ItemStack getItem(String itemId) {
        return items.get(itemId);
    }

}
