package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.TeleportAction;
import de.jeff_media.AngelChestPlus.commands.CommandFetchOrTeleport;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
import de.jeff_media.AngelChestPlus.utils.HeadCreator;
import de.jeff_media.AngelChestPlus.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GUIManager {

    private final Main main;
    private static final String BUTTON_BACK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0=";
    private static final String BUTTON_TP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=";
    private static final String BUTTON_FETCH = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZlYjM5ZDcxZWY4ZTZhNDI2NDY1OTMzOTNhNTc1M2NlMjZhMWJlZTI3YTBjYThhMzJjYjYzN2IxZmZhZSJ9fX0=";
    private static final String BUTTON_UNLOCK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFkOTQzZDA2MzM0N2Y5NWFiOWU5ZmE3NTc5MmRhODRlYzY2NWViZDIyYjA1MGJkYmE1MTlmZjdkYTYxZGIifX19";
    private static final String BUTTON_CONFIRM = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZlNTIyZDkxODI1MjE0OWU2ZWRlMmVkZjNmZTBmMmMyYzU4ZmVlNmFjMTFjYjg4YzYxNzIwNzIxOGFlNDU5NSJ9fX0=";
    private static final String BUTTON_CONFIRM_ACCEPT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UyYTUzMGY0MjcyNmZhN2EzMWVmYWI4ZTQzZGFkZWUxODg5MzdjZjgyNGFmODhlYThlNGM5M2E0OWM1NzI5NCJ9fX0=";
    private static final String BUTTON_CONFIRM_DECLINE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTljZGI5YWYzOGNmNDFkYWE1M2JjOGNkYTc2NjVjNTA5NjMyZDE0ZTY3OGYwZjE5ZjI2M2Y0NmU1NDFkOGEzMCJ9fX0=";

    private static final String TITLE_MAIN = "§4§l[§c§lAngelChest§4§l]";
    private static final String TITLE_CHEST = "§4§l[§c§lAngelChest§4§l] §c#{id} §4| §c{time}";


    public GUIManager(Main main) {
        this.main = main;
    }

    /**
     * Updates the players GUI when a chest is broken or created
     *
     * @param player
     * @param brokenChestId
     */
    public void updateGUI(Player player, int brokenChestId) {
        if (player.getOpenInventory() == null) return;
        if (player.getOpenInventory().getTopInventory() == null) return;
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof AngelChestGUIHolder)) return;

        main.debug("GUI: Update");
        main.debug("Broken Chest ID: "+brokenChestId);

        AngelChestGUIHolder holder = (AngelChestGUIHolder) player.getOpenInventory().getTopInventory().getHolder();

        if (holder.getContext() == GUIContext.MAIN_MENU) {
            showMainGUI(player);
            main.debug("GUI: Main menu, restarting it");
            return;
        }

        int selectedChest = holder.getChestIdStartingAt1();
        if (selectedChest < brokenChestId) {
            main.debug("GUI: Selected chest was before destroyed, nothing to do");
        } else if (brokenChestId == selectedChest) {
            showMainGUI(player);
            main.debug("GUI: Selected chest is the one destroyed, going back to main menu");
        } else {
            main.debug("GUI: Selected chest is after the destroyed, adjusting ID");
            holder.setChestIdStartingAt1(holder.getChestIdStartingAt1() - 1);
            if(holder.getContext()==GUIContext.CHEST_MENU) {
                showChestGUI(player,holder,holder.getChestIdStartingAt1());
            } else if(holder.getContext()==GUIContext.CONFIRM_MENU) {
                showConfirmGUI(player,holder,holder.getAction());
            }
        }
        return;
    }

    private ItemStack getChestItem(AngelChest angelChest, int id) {
        ItemStack item = new ItemStack(main.chestMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getChestItemName(angelChest, id));
        meta.setLore(getChestItemLore(angelChest, id));
        item.setItemMeta(meta);
        return item;
    }

    private String getChestItemName(AngelChest angelChest, int id) {
        return String.format("§6AngelChest #%d", id);
    }

    private List<String> getChestItemLore(AngelChest angelChest, int id) {

        String lore[] = new String[]{
                String.format("§4%s",CommandUtils.getTimeLeft(angelChest)),
                String.format("§aX: §f%d", angelChest.block.getX()),
                String.format("§aY: §f%d", angelChest.block.getY()),
                String.format("§aZ: §f%d", angelChest.block.getZ()),
                String.format("§aWorld: §f%s", angelChest.block.getWorld().getName())
        };
        return Arrays.asList(lore);
    }

    private static int getInventorySize(int numberOfChests) {
        if (numberOfChests <= 9) return 9;
        if (numberOfChests <= 18) return 18;
        if (numberOfChests <= 27) return 27;
        if (numberOfChests <= 36) return 36;
        if (numberOfChests <= 45) return 45;
        return 54;
    }

    private boolean hasOpen(Player player, Inventory inventory) {
        if(player.getOpenInventory() == null) return false;
        if(player.getOpenInventory().getTopInventory() == null) return false;
        return player.getOpenInventory().getTopInventory().equals(inventory);
    }

    public void showMainGUI(Player player) {
        AngelChestGUIHolder holder = new AngelChestGUIHolder(player, GUIContext.MAIN_MENU, main);
        int inventorySize = getInventorySize(holder.getNumberOfAngelChests());
        Inventory inventory = Bukkit.createInventory(holder, inventorySize, TITLE_MAIN);
        holder.setInventory(inventory);

        if (Utils.getAllAngelChestsFromPlayer(player, main).size() == 1) {
            holder.setChestIdStartingAt1(1);
            main.guiManager.showChestGUI(player, holder, 1);
            return;
        }

        int id = 1;
        for (AngelChest chest : holder.getAngelChests()) {
            if (id > inventorySize) break;

            ItemStack item = getChestItem(chest, id);
            inventory.setItem(id - 1, item);

            id++;
        }

        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
            if(hasOpen(player,inventory)) {
                showMainGUI(player);
            }
        },20l);
    }

    public void showChestGUI(Player player, AngelChestGUIHolder holder, int id) {
        AngelChest angelChest = holder.getAngelChest();
        AngelChestGUIHolder newHolder = new AngelChestGUIHolder(player, GUIContext.CHEST_MENU, main, id);
        Inventory inventory = Bukkit.createInventory(newHolder, 9, getTitle(holder.getAngelChest(),holder.getChestIdStartingAt1()));
        newHolder.setInventory(inventory);

        inventory.setItem(GUI.SLOT_BACK, getBackButton());
        inventory.setItem(GUI.SLOT_INFO, getInfoButton(angelChest, id));
        inventory.setItem(GUI.SLOT_TP, getTPButton());
        inventory.setItem(GUI.SLOT_FETCH, getFetchButton());
        inventory.setItem(GUI.SLOT_UNLOCK, getUnlockButton());
        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
            if(hasOpen(player,inventory)) {
                showChestGUI(player, holder, holder.getChestIdStartingAt1());
            }
        },20l);
    }

    public void showConfirmGUI(Player player, AngelChestGUIHolder holder, TeleportAction action) {
        AngelChestGUIHolder newHolder = new AngelChestGUIHolder(player, GUIContext.CONFIRM_MENU, main, holder.getChestIdStartingAt1());
        newHolder.setAction(action);
        Inventory inventory = Bukkit.createInventory(newHolder, 9, getTitle(holder.getAngelChest(),holder.getChestIdStartingAt1()));
        newHolder.setInventory(inventory);

        inventory.setItem(GUI.SLOT_CONFIRM_INFO, getConfirmInfoButton(action.getPrice(main)));
        inventory.setItem(GUI.SLOT_CONFIRM_ACCEPT, getConfirmAcceptButton());
        inventory.setItem(GUI.SLOT_CONFIRM_DECLINE, getConfirmDeclineButton());
        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
            if(hasOpen(player,inventory)) {
                showConfirmGUI(player, holder, holder.getAction());
            }
        },20l);
    }

    private ItemStack getBackButton() {
        return getButton(BUTTON_BACK, "§6Back", null);
    }

    private ItemStack getInfoButton(AngelChest angelChest, int id) {
        return getButton(Material.PAPER, "§6Info", getChestItemLore(angelChest, id));
    }

    private ItemStack getTPButton() {
        return getButton(BUTTON_TP, "§6Teleport", null);
    }

    private ItemStack getFetchButton() {
        return getButton(BUTTON_FETCH, "§6Fetch", null);
    }

    private ItemStack getUnlockButton() {
        return getButton(BUTTON_UNLOCK, "§6Unlock", null);
    }

    private ItemStack getConfirmAcceptButton() {
        return getButton(BUTTON_CONFIRM_ACCEPT, "§aAccept", null);
    }

    private ItemStack getConfirmDeclineButton() {
        return getButton(BUTTON_CONFIRM_DECLINE, "§cDecline", null);
    }

    private ItemStack getConfirmInfoButton(double price) {
        return getButton(BUTTON_CONFIRM, "§6Info",
                getLore("§6You are about to spend {price}{currency}."
                        .replaceAll("\\{price}", String.valueOf(price))
                        .replaceAll("\\{currency}", CommandUtils.getCurrency(price, main))));
    }

    private ItemStack getButton(Material material, String name, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getButton(String head, String name, @Nullable List<String> lore) {
        ItemStack item = HeadCreator.getHead(head);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> getLore(String text) {
        return Arrays.asList(text.split("\n"));
    }

    private String getTitle(AngelChest chest, int id) {
        return TITLE_CHEST.replaceAll("\\{id}", String.valueOf(id))
                        .replaceAll("\\{time}",CommandUtils.getTimeLeft(chest));

    }


}
