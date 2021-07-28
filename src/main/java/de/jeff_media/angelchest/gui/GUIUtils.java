package de.jeff_media.angelchest.gui;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.data.AngelChest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public final class GUIUtils {

    /**
     * Checks whether the clicked slot belongs to the spaces reserved for the player's items
     *
     * @param slot slotnumber
     * @return true if it belongs to the player inventory, false if it belongs to the GUI
     */
    public static boolean isLootableInPreview(final int slot) {
        return (slot >= GUI.SLOT_PREVIEW_ARMOR_START && slot < GUI.PREVIEW_ARMOR_SIZE + GUI.PREVIEW_ARMOR_SIZE) || (slot >= GUI.SLOT_PREVIEW_OFFHAND && slot < GUI.SLOT_PREVIEW_OFFHAND + GUI.PREVIEW_OFFHAND_SIZE) || (slot >= GUI.SLOT_PREVIEW_STORAGE_START && slot < GUI.SLOT_PREVIEW_STORAGE_START + GUI.PREVIEW_STORAGE_SIZE) || (slot >= GUI.SLOT_PREVIEW_HOTBAR_START && slot < GUI.SLOT_PREVIEW_HOTBAR_START + GUI.PREVIEW_HOTBAR_SIZE) || slot == GUI.SLOT_PREVIEW_XP;

    }

    private static ItemStack getPlaceholder() {
        ItemStack placeholder = new ItemStack(Enums.getIfPresent(Material.class, Main.getInstance().getConfig().getString(Config.GUI_BUTTON_PREVIEW_PLACEHOLDER)).or(Material.GRAY_STAINED_GLASS_PANE));
        if(placeholder.getType().isAir()) placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName("§8");
        meta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(),"placeholder"), PersistentDataType.BYTE, (byte) 1);
        placeholder.setItemMeta(meta);
        return placeholder;
    }

    public static boolean isPlaceholder(ItemStack item) {
        if(!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Main.getInstance(),"placeholder"), PersistentDataType.BYTE);
    }

    public static void loadChestIntoPreviewInventory(final AngelChest angelChest, final Inventory inventory) {

        final ItemStack placeholder = getPlaceholder();

        //ItemMeta meta = placeholder.hasItemMeta() ? placeholder.getItemMeta() : Bukkit.getItemFactory().getItemMeta(placeholder.getType());
        //meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        //placeholder.setItemMeta(meta);

        final ItemStack[] itemStacks = new ItemStack[54];
        for (int i = 0; i < 54; i++) {
            itemStacks[i] = placeholder.clone();
        }

        System.arraycopy(angelChest.armorInv, 0, itemStacks, 2, GUI.PREVIEW_ARMOR_SIZE);
        System.arraycopy(angelChest.extraInv, 0, itemStacks, 7, GUI.PREVIEW_OFFHAND_SIZE);
        System.arraycopy(angelChest.storageInv, 0, itemStacks, 45, GUI.PREVIEW_HOTBAR_SIZE);
        System.arraycopy(angelChest.storageInv, 9, itemStacks, 9, GUI.PREVIEW_STORAGE_SIZE);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, itemStacks[i]);
        }
    }

    public static void savePreviewInventoryToChest(final Inventory inventory, final AngelChest angelChest) {
        Objects.requireNonNull(angelChest, "AngelChest is null!");
        angelChest.armorInv = new ItemStack[GUI.PREVIEW_ARMOR_SIZE];
        angelChest.extraInv = new ItemStack[GUI.PREVIEW_OFFHAND_SIZE];
        angelChest.storageInv = new ItemStack[GUI.PREVIEW_STORAGE_SIZE + GUI.PREVIEW_HOTBAR_SIZE];
        for (int i = 0; i < GUI.PREVIEW_ARMOR_SIZE; i++) {
            angelChest.armorInv[i] = inventory.getItem(i + GUI.SLOT_PREVIEW_ARMOR_START);
        }
        for (int i = 0; i < GUI.PREVIEW_OFFHAND_SIZE; i++) {
            angelChest.extraInv[i] = inventory.getItem(i + GUI.SLOT_PREVIEW_OFFHAND);
        }
        for (int i = 0; i < GUI.PREVIEW_HOTBAR_SIZE; i++) {
            angelChest.storageInv[i] = inventory.getItem(i + GUI.SLOT_PREVIEW_HOTBAR_START);
        }
        for (int i = 0; i < GUI.PREVIEW_STORAGE_SIZE; i++) {
            angelChest.storageInv[i + GUI.PREVIEW_HOTBAR_SIZE] = inventory.getItem(i + GUI.SLOT_PREVIEW_STORAGE_START);
        }
    }

}
