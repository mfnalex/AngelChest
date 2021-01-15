package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.utils.AngelChestCommandUtils;
import de.jeff_media.AngelChestPlus.utils.HeadCreator;
import de.jeff_media.AngelChestPlus.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIManager {

    private final Main main;
    private static final String BUTTON_BACK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0=";
    private static final String BUTTON_TP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=";
    private static final String BUTTON_FETCH = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=";
    private static final String BUTTON_UNLOCK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19";

    public GUIManager(Main main ) {
        this.main=main;

    }

    private ItemStack getChestItem(AngelChest angelChest, int id) {
        ItemStack item = new ItemStack(main.chestMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getChestItemName(angelChest,id));
        meta.setLore(getChestItemLore(angelChest,id));
        item.setItemMeta(meta);
        return item;
    }

    private String getChestItemName(AngelChest angelChest, int id) {
        return String.format("§6AngelChest #%d",id);
    }

    private List<String> getChestItemLore(AngelChest angelChest, int id) {
        String lore[] = new String[]{
                String.format("§aX: §f%d", angelChest.block.getX()),
                String.format("§aY: §f%d", angelChest.block.getY()),
                String.format("§aZ: §f%d", angelChest.block.getZ()),
                String.format("§aWorld: §f%s", angelChest.block.getWorld().getName())
        };
        return Arrays.asList(lore);
    }

    private static int getInventorySize(int numberOfChests) {
        if(numberOfChests<=9) return 9;
        if(numberOfChests<=18) return 18;
        if(numberOfChests<=27) return 27;
        if(numberOfChests<=36) return 36;
        if(numberOfChests<=45) return 45;
        return 54;
    }

    public void showMainGUI(Player player) {
        AngelChestGUIHolder holder = new AngelChestGUIHolder(player, GUIContext.MAIN_MENU, main);
        int inventorySize = getInventorySize(holder.getNumberOfAngelChests());
        Inventory inventory = Bukkit.createInventory(holder,inventorySize,"§4[§cAngelChest§4]");

        int id = 1;
        for(AngelChest chest : holder.getAngelChests()) {
            if(id > inventorySize) break;

            ItemStack item = getChestItem(chest, id);
            inventory.setItem(id-1,item);

            id++;
        }

        player.openInventory(inventory);
    }

    public void showChestGUI(Player player, AngelChestGUIHolder holder, int id) {
        AngelChest angelChest = holder.getAngelChests().get(id-1);
        Inventory inventory = Bukkit.createInventory(new AngelChestGUIHolder(player,GUIContext.CHEST_MENU, main),9,"§4[§cAngelChest§4] §c#"+id);

        inventory.setItem(GUI.SLOT_BACK,getBackButton());
        inventory.setItem(GUI.SLOT_INFO,getInfoButton(angelChest,id));
        inventory.setItem(GUI.SLOT_TP,getTPButton());
        inventory.setItem(GUI.SLOT_FETCH,getFetchButton());
        inventory.setItem(GUI.SLOT_UNLOCK,getUnlockButton());
        player.openInventory(inventory);
    }

    private ItemStack getBackButton() {
        return getButton(BUTTON_BACK,"&6Back",null);
    }

    private ItemStack getInfoButton(AngelChest angelChest, int id) {
        return getButton(Material.PAPER, "§6Info",getChestItemLore(angelChest,id));
    }

    private ItemStack getTPButton() {
        return getButton(BUTTON_TP,"§6Teleport",null);
    }

    private ItemStack getFetchButton() {
        return getButton(BUTTON_FETCH,"§6Fetch",null);
    }

    private ItemStack getUnlockButton() {
        return getButton(BUTTON_UNLOCK, "§6Unlock", null);
    }

    private ItemStack getButton(Material material, String name, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if(lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getButton(String head, String name, @Nullable List<String> lore) {
        ItemStack item = HeadCreator.getHead(head);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if(lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }





}
