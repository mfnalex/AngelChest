package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Hooks into InventoryPages
 */
public final class InventoryPagesHook {

    final Main main;
    boolean disabled = false;
    YamlConfiguration inventoryPagesConfig;
    Material prevMat, nextMat, noPageMat;
    String prevName, nextName, noPageName;

    public InventoryPagesHook() {
        this.main = Main.getInstance();

        final File inventoryPagesConfigFile = new File(main.getDataFolder() + File.separator + ".." + File.separator + "InventoryPages" + File.separator + "config.yml");

        if (!inventoryPagesConfigFile.exists()) {
            disabled = true;
            return;
        }

        inventoryPagesConfig = YamlConfiguration.loadConfiguration(inventoryPagesConfigFile);

        main.getLogger().info("Succesfully hooked into InventoryPages");

        /*prevSlot = inventoryPagesConfig.getInt("items.prev.position")+9;
        nextSlot = inventoryPagesConfig.getInt("items.next.position")+9;*/

        prevMat = Material.valueOf(inventoryPagesConfig.getString("items.prev.id"));
        nextMat = Material.valueOf(inventoryPagesConfig.getString("items.next.id"));
        noPageMat = Material.valueOf(inventoryPagesConfig.getString("items.noPage.id"));

        prevName = ChatColor.translateAlternateColorCodes('&', inventoryPagesConfig.getString("items.prev.name"));
        nextName = ChatColor.translateAlternateColorCodes('&', inventoryPagesConfig.getString("items.next.name"));
        noPageName = ChatColor.translateAlternateColorCodes('&', inventoryPagesConfig.getString("items.noPage.name"));

        //plugin.getLogger().info("Prev Button: " + prevSlot + "," + prevMat.name() + "," + prevName);
        //plugin.getLogger().info("Next Button: " + nextSlot + "," + nextMat.name() + "," + nextName);

    }

    /**
     * Checks if an ItemStack belongs to InventoryPages.
     *
     * @param item The ItemStack to check
     * @return true if it belongs to InventoryPages, otherwise false
     */
    public boolean isButton(@Nullable final ItemStack item/*, int slot, @NotNull Inventory inv*/) {

        if (disabled) return false;

        if (item == null) return false;
        if (!item.hasItemMeta()) return false;

        /*if(!(inv instanceof PlayerInventory)) {
            return false;
        }*/

        // When using &f as color, we manually have to add this to the string because it gets removed by InventoryPages
        if (prevName.startsWith("§f")) prevName = prevName.substring(2);
        if (nextName.startsWith("§f")) nextName = nextName.substring(2);
        if (noPageName.startsWith("§f")) noPageName = noPageName.substring(2);

        //if(slot == prevSlot ) {
        if (item.getType() == prevMat && (item.getItemMeta().getDisplayName().equals(prevName))) {
            return true;
        }
        if (item.getType() == noPageMat && item.getItemMeta().getDisplayName().equals(noPageName)) {
            return true;
        }
        //}

        //if(slot == nextSlot  ) {
        if (item.getType() == nextMat && item.getItemMeta().getDisplayName().equals(nextName)) {
            return true;
        }
        if (item.getType() == noPageMat && item.getItemMeta().getDisplayName().equals(noPageName)) {
            return true;
        }
        //}

        return false;
    }

}
