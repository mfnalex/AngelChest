package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.hooks.OraxenHook;
import de.jeff_media.angelchest.utils.HeadCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
public class MagicMaterial {

    private static final Main main = Main.getInstance();

    @Getter @NotNull private Material material;
    @Getter @Nullable private String customName;
    @Getter @Nullable private CustomItemPlugin plugin;
    @Getter @Nullable private BlockData blockData;

    public void placeInWorld(Block block, UUID uuid, AngelChest angelChest) {

        // Oraxen
        if(plugin == CustomItemPlugin.ORAXEN) {
            if(OraxenHook.place(block, customName)) {
                System.out.println("Oraxen block placed successfully");
                return;
            } else {
                System.out.println("Couldn't place oraxen block, falling back to normal Material...");
            }
        }

        // Set it manually
        block.setType(material);

        // Material is PLAYER_HEAD, so either use the custom texture, or the player skin's texture
        if (material == Material.PLAYER_HEAD) {
            HeadCreator.createHeadInWorld(block, uuid);
        }

        if(blockData != null) {
            block.setBlockData(blockData);
        }
    }

    @Override
    public String toString() {
        return "MagicMaterial{" +
                "material=" + material +
                ", customName='" + customName + '\'' +
                ", plugin=" + plugin +
                ", blockData=" + blockData +
                '}';
    }

    public ItemStack getItemStack(final AngelChest angelChest) {

        if(plugin == CustomItemPlugin.ORAXEN) {
            return OraxenHook.getItemStack(customName);
        }

        if (material == Material.PLAYER_HEAD) {
            if (main.getConfig().getBoolean(Config.HEAD_USES_PLAYER_NAME)) {
                return HeadCreator.getPlayerHead(angelChest.owner);
            } else {
                return HeadCreator.getHead(main.getConfig().getString(Config.CUSTOM_HEAD_BASE64));
            }
        } else {
            return new ItemStack(main.getChestMaterial(angelChest).getMaterial());
        }
    }

    public enum CustomItemPlugin {
        ORAXEN
    }

    public static MagicMaterial fromString(String itemName, Material defaultMaterial) {
        final Main main = Main.getInstance();

        // BlockData
        if(itemName.contains(":") && itemName.contains("[") && itemName.contains("]")) {
            try {
                BlockData blockData = Bukkit.createBlockData(itemName);
                return new MagicMaterial(defaultMaterial, null, null, blockData);
            } catch (IllegalArgumentException exception) {
                main.getLogger().warning("Invalid BlockData: " + itemName + " - falling back to " + defaultMaterial.name());
                return new MagicMaterial(defaultMaterial, null, null, null);
            }
        }

        // Custom item plugins
        if(itemName.contains(":")) {
            String plugin = itemName.split(":")[0];
            String customItem = itemName.split(":")[1];
            switch(plugin.toLowerCase(Locale.ROOT)) {
                case "oraxen":
                    try {
                        ItemStack oraxenItem = OraxenHook.getItemStack(customItem);
                        if(oraxenItem == null) {
                            main.getLogger().warning("Could not find Oraxen item: " + customItem + " - falling back to " + defaultMaterial.name());
                            return new MagicMaterial(defaultMaterial, null, null, null);
                        } else {
                            return new MagicMaterial(oraxenItem.getType(), customItem, MagicMaterial.CustomItemPlugin.ORAXEN, null);
                        }
                    } catch (Throwable t) {
                        main.getLogger().warning("Error while hooking into Oraxen, falling back to " + defaultMaterial.name());
                        t.printStackTrace();
                        return new MagicMaterial(defaultMaterial, null, null, null);
                    }
            }
        }

        if (Material.getMaterial(itemName.toUpperCase()) == null) {
            main.getLogger().warning("Invalid Material: " + itemName + " - falling back to " + defaultMaterial.name());
            return new MagicMaterial(defaultMaterial, null, null, null);
        } else {
            MagicMaterial temp = new MagicMaterial(Material.getMaterial(itemName.toUpperCase()),null,null, null);
            if (!temp.getMaterial().isBlock()) {
                main.getLogger().warning("Not a block: " + itemName + " - falling back to " + defaultMaterial.name());
                return new MagicMaterial(defaultMaterial, null, null, null);
            } else {
                return temp;
            }
        }
    }

}
