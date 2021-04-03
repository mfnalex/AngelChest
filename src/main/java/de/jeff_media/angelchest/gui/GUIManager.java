package de.jeff_media.angelchest.gui;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.enums.Features;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public final class GUIManager {

    private final Main main;

    public GUIManager() {
        this.main = Main.getInstance();

    }

    private static int getInventorySize(final int numberOfChests) {
        if (numberOfChests <= 9) return 9;
        if (numberOfChests <= 18) return 18;
        if (numberOfChests <= 27) return 27;
        if (numberOfChests <= 36) return 36;
        if (numberOfChests <= 45) return 45;
        return 54;
    }

    public void updatePreviewInvs(final Player originalPlayer, final AngelChest angelChest) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(originalPlayer)) continue;
            if (player.getOpenInventory() == null) continue;
            if (player.getOpenInventory().getTopInventory() == null) continue;
            if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder)) continue;
            final GUIHolder guiHolder = (GUIHolder) player.getOpenInventory().getTopInventory().getHolder();
            if (guiHolder.getSpecialAngelChest() != null && guiHolder.getSpecialAngelChest().equals(angelChest)) {
                main.debug("This AngelChest " + angelChest.toString() + " is also in use by " + player.getName() + ", updating...");
                if (!angelChest.isEmpty()) {
                    showPreviewGUI(player, angelChest, guiHolder.isReadOnlyPreview(), false);
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    public void updateGUI(final Player player, final int brokenChestId) {
        if (player.getOpenInventory() == null) return;
        if (player.getOpenInventory().getTopInventory() == null) return;
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder)) return;

        final GUIHolder holder = (GUIHolder) player.getOpenInventory().getTopInventory().getHolder();

        if (holder.getContext() == GUIContext.MAIN_MENU) {
            showMainGUI(player);
            return;
        }

        final int selectedChest = holder.getChestIdStartingAt1();
        if (selectedChest < brokenChestId) {
            //noinspection UnnecessaryReturnStatement
            return;
        } else if (brokenChestId == selectedChest) {
            showMainGUI(player);
        } else {
            holder.setChestIdStartingAt1(holder.getChestIdStartingAt1() - 1);
            if (holder.getContext() == GUIContext.CHEST_MENU) {
                showChestGUI(player, holder, holder.getChestIdStartingAt1());
            } else if (holder.getContext() == GUIContext.CONFIRM_MENU) {
                showConfirmGUI(player, holder, holder.getAction());
            }
        }
    }

    private ItemStack getChestItem(final AngelChest angelChest, final int id) {
        final ItemStack item;
        final Material material = main.getChestMaterial(angelChest);
        if (material == Material.PLAYER_HEAD) {
            if (main.getConfig().getBoolean(Config.HEAD_USES_PLAYER_NAME)) {
                item = HeadCreator.getPlayerHead(angelChest.owner);
            } else {
                item = HeadCreator.getHead(main.getConfig().getString(Config.CUSTOM_HEAD_BASE64));
            }
        } else {
            item = new ItemStack(main.getChestMaterial(angelChest));
        }

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getChestItemName(angelChest, id));
        meta.setLore(getChestItemLore(angelChest, id));
        item.setItemMeta(meta);
        return item;
    }

    private String getChestItemName(@SuppressWarnings("unused") final AngelChest angelChest, final int id) {
        return String.format("§6AngelChest #%d", id);
    }

    private List<String> getChestItemLore(final AngelChest angelChest, @SuppressWarnings("unused") final int id) {

        final String[] lore = new String[]{
                String.format("§4%s", CommandUtils.getTimeLeft(angelChest)),
                String.format("§aX: §f%d", angelChest.block.getX()),
                String.format("§aY: §f%d", angelChest.block.getY()),
                String.format("§aZ: §f%d", angelChest.block.getZ()),
                String.format(/*"§aWorld: "+*/"§f%s", angelChest.block.getWorld().getName())
        };
        return Arrays.asList(lore);
    }

    private boolean hasOpen(final Player player, final Inventory inventory) {
        if (player.getOpenInventory() == null) return false;
        if (player.getOpenInventory().getTopInventory() == null) return false;
        return player.getOpenInventory().getTopInventory().equals(inventory);
    }

    public void showLatestChestGUI(final Player player) {
        final GUIHolder holder = new GUIHolder(player, GUIContext.MAIN_MENU);
        final int latestChest = holder.getNumberOfAngelChests();
        final int inventorySize = getInventorySize(latestChest);
        final Inventory inventory = Bukkit.createInventory(holder, inventorySize, main.messages.GUI_TITLE_MAIN);
        holder.setInventory(inventory);

        holder.setChestIdStartingAt1(latestChest);
        main.guiManager.showChestGUI(player, holder, latestChest);
    }

    public void showMainGUI(final Player player) {
        final GUIHolder holder = new GUIHolder(player, GUIContext.MAIN_MENU);
        final int inventorySize = getInventorySize(holder.getNumberOfAngelChests());
        final Inventory inventory = Bukkit.createInventory(holder, inventorySize, main.messages.GUI_TITLE_MAIN);
        holder.setInventory(inventory);

        if (AngelChestUtils.getAllAngelChestsFromPlayer(player).size() == 1) {
            holder.setChestIdStartingAt1(1);
            main.guiManager.showChestGUI(player, holder, 1);
            return;
        }

        int id = 1;
        for (final AngelChest chest : holder.getAngelChests()) {
            if (id > inventorySize) break;

            final ItemStack item = getChestItem(chest, id);
            inventory.setItem(id - 1, item);

            id++;
        }

        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            if (hasOpen(player, inventory)) {
                showMainGUI(player);
            }
        }, Ticks.fromSeconds(1));
    }

    public void showPreviewGUI(final Player player, final AngelChest angelChest, final boolean isPreview, final boolean firstOpened) {
        final GUIHolder holder = new GUIHolder(player, GUIContext.PREVIEW_MENU);
        final Inventory inventory = Bukkit.createInventory(holder, 54, main.messages.GUI_TITLE_MAIN);
        holder.setInventory(inventory);
        holder.setChestIdStartingAt1(AngelChestUtils.getAllAngelChestsFromPlayer(angelChest.owner).indexOf(angelChest));
        holder.setAngelChest(angelChest);
        //holder.setChestIdStartingAt1(Utils.getAllAngelChestsFromPlayer(angelChest.owner,main).indexOf(angelChest)+1);
        /*inventory = */
        GUIUtils.loadChestIntoPreviewInventory(holder.getAngelChest(), inventory);

        if (isPreview) {
            inventory.setItem(GUI.SLOT_PREVIEW_BACK, getBackButton());
            holder.setReadOnlyPreview(true);
        }
        if (angelChest.experience > 0 || angelChest.levels > 0) {
            inventory.setItem(GUI.SLOT_PREVIEW_XP, getButton(Material.EXPERIENCE_BOTTLE, "§6" + XPUtils.xpToString(angelChest.experience), null));
        }

        if (Daddy.allows(Features.GENERIC)) { // Don't add feature here
            if (!isPreview && firstOpened && !player.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_CHEST)) {
                if (Bukkit.getPlayer(angelChest.owner) != null) {
                    Bukkit.getPlayer(angelChest.owner).sendMessage(main.messages.MSG_OPENED.replaceAll("\\{player}", player.getName()));
                }
            }
        }

        player.openInventory(inventory);
    }

    public void showChestGUI(final Player player, final GUIHolder holder, final int id) {
        final AngelChest angelChest = holder.getAngelChest();
        final GUIHolder newHolder = new GUIHolder(player, GUIContext.CHEST_MENU, id);
        final Inventory inventory = Bukkit.createInventory(newHolder, 9, getTitle(holder.getAngelChest(), holder.getChestIdStartingAt1()));
        newHolder.setInventory(inventory);

        inventory.setItem(GUI.SLOT_CHEST_BACK, getBackButton());
        inventory.setItem(GUI.SLOT_CHEST_INFO, getInfoButton(angelChest, id));
        if (player.hasPermission(Permissions.TP)) inventory.setItem(GUI.SLOT_CHEST_TP, getTPButton());
        if (player.hasPermission(Permissions.FETCH)) inventory.setItem(GUI.SLOT_CHEST_FETCH, getFetchButton());
        if (player.hasPermission(Permissions.PROTECT) && angelChest.isProtected)
            inventory.setItem(GUI.SLOT_CHEST_UNLOCK, getUnlockButton());
        if (player.hasPermission(Permissions.PREVIEW)) inventory.setItem(GUI.SLOT_CHEST_PREVIEW, getPreviewButton());
        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            if (hasOpen(player, inventory)) {
                showChestGUI(player, holder, holder.getChestIdStartingAt1());
            }
        }, Ticks.fromSeconds(1));
    }

    public void showConfirmGUI(final Player player, final GUIHolder holder, final CommandAction action) {
        final GUIHolder newHolder = new GUIHolder(player, GUIContext.CONFIRM_MENU, holder.getChestIdStartingAt1());
        newHolder.setAction(action);
        final Inventory inventory = Bukkit.createInventory(newHolder, 9, getTitle(holder.getAngelChest(), holder.getChestIdStartingAt1()));
        newHolder.setInventory(inventory);

        inventory.setItem(GUI.SLOT_CONFIRM_INFO, getConfirmInfoButton(action.getPrice(player)));
        inventory.setItem(GUI.SLOT_CONFIRM_ACCEPT, getConfirmAcceptButton());
        inventory.setItem(GUI.SLOT_CONFIRM_DECLINE, getConfirmDeclineButton());
        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            try {
                if (hasOpen(player, inventory)) {
                    showConfirmGUI(player, holder, holder.getAction());
                }
            } catch (final NullPointerException ignored) {
                //main.debug("Null in repeating task in showConfirmGUI");
                // TODO: No idea why it happens, but everything still works normally lol so fuck it.
            }
        }, Ticks.fromSeconds(1));
    }

    private ItemStack getPreviewButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_PREVIEW), main.messages.GUI_PREVIEW, null);
    }

    private ItemStack getBackButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_BACK), main.messages.GUI_BACK, null);
    }

    private ItemStack getInfoButton(final AngelChest angelChest, final int id) {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_INFO), main.messages.GUI_INFO, getChestItemLore(angelChest, id));
    }

    private ItemStack getTPButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_TELEPORT), main.messages.GUI_TELEPORT, null);
    }

    private ItemStack getFetchButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_FETCH), main.messages.GUI_FETCH, null);
    }

    private ItemStack getUnlockButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_UNLOCK), main.messages.GUI_UNLOCK, null);
    }

    private ItemStack getConfirmAcceptButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_ACCEPT), main.messages.GUI_ACCEPT, null);
    }

    private ItemStack getConfirmDeclineButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_DECLINE), main.messages.GUI_DECLINE, null);
    }

    private ItemStack getConfirmInfoButton(final double price) {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_INFO), main.messages.GUI_INFO,
                getLore(main.messages.GUI_INFO_LORE
                        .replaceAll("\\{price}", String.valueOf(price))
                        .replaceAll("\\{currency}", CommandUtils.getCurrency(price))));
    }

    @SuppressWarnings("SameParameterValue")
    private ItemStack getButton(final Material material, final String name, @Nullable final List<String> lore) {
        return getButton(material.name(), name, lore);
    }

    private ItemStack getButton(final String materialOrBase64, final String name, @Nullable final List<String> lore) {
        final ItemStack item;
        final Material material = Enums.getIfPresent(Material.class, materialOrBase64.toUpperCase()).orNull();
        if (material != null) {
            item = new ItemStack(material);
        } else {
            item = HeadCreator.getHead(materialOrBase64);
        }
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        //meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> getLore(final String text) {
        return Arrays.asList(text.split("\n"));
    }

    private String getTitle(final AngelChest chest, final int id) {
        return main.messages.GUI_TITLE_CHEST.replaceAll("\\{id}", String.valueOf(id))
                .replaceAll("\\{time}", CommandUtils.getTimeLeft(chest));

    }


}
