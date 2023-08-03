package de.jeff_media.angelchest.gui;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.events.AngelChestOpenEvent;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy_Stepsister;
import com.jeff_media.jefflib.TextUtils;
import com.jeff_media.jefflib.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GUIManager {

    private final AngelChestMain main;

    public GUIManager() {
        this.main = AngelChestMain.getInstance();
    }

    private static int getInventorySize(final int numberOfChests) {
        if (numberOfChests <= 9) return 9;
        if (numberOfChests <= 18) return 18;
        if (numberOfChests <= 27) return 27;
        if (numberOfChests <= 36) return 36;
        if (numberOfChests <= 45) return 45;
        return 54;
    }

    private ItemStack getBackButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_BACK), main.messages.GUI_BACK, null);
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

    private ItemStack getChestItem(final AngelChest angelChest, final int id) {
        ItemStack item = new ItemStack(main.getChestMaterial(angelChest).getMaterial());

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getChestItemName(angelChest, id));
        meta.setLore(getChestItemLore(angelChest, id));
        item.setItemMeta(meta);
        return item;
    }

    private List<String> getChestItemLore(final AngelChest angelChest, @SuppressWarnings("unused") final int id) {
        List<String> lore = new ArrayList<>();
        for (String line : main.messages.GUI_CHEST_LORE.split("\n")) {
            lore.add(replacePlaceholders(line, angelChest, id));
        }
        return lore;
    }

    private String getChestItemName(@SuppressWarnings("unused") final AngelChest angelChest, final int id) {
        return replacePlaceholders(main.messages.GUI_CHEST_NAME, angelChest, id);
    }

    private String replacePlaceholders(final String line, final AngelChest angelChest, final int id) {
        OfflinePlayer player = null;
        if(angelChest.owner != null) {
            player = Bukkit.getOfflinePlayer(angelChest.owner);
        }
        return TextUtils.format(line.replace("{x}", String.valueOf(angelChest.getBlock().getX()))
                .replace("{y}", String.valueOf(angelChest.getBlock().getY()))
                .replace("{z}", String.valueOf(angelChest.getBlock().getZ()))
                .replace("{id}", String.valueOf(id))
                .replace("{world}", angelChest.getWorld().getName())
                .replace("{time}", CommandUtils.getTimeLeft(angelChest)),player);
    }

    private ItemStack getConfirmAcceptButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_ACCEPT), main.messages.GUI_ACCEPT, null);
    }

    private ItemStack getConfirmDeclineButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_DECLINE), main.messages.GUI_DECLINE, null);
    }

    private ItemStack getConfirmInfoButton(Player player, final double price) {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_INFO), main.messages.GUI_INFO, getLore(replaceBalancePlaceholders(player, price, main.messages.GUI_INFO_LORE)));
    }

    private ItemStack getFetchButton(Player player) {
        double price = main.groupManager.getFetchPricePerPlayer(player);
        ItemStack button = getButton(main.getConfig().getString(Config.GUI_BUTTON_FETCH), replaceBalancePlaceholders(player, price, main.messages.GUI_FETCH), null);
        LoreUtils.applyLore(button, LoreUtils.applyNewlines(replaceBalancePlaceholders(player, price, main.getConfig().getString(Config.GUI_FETCH_LORE))));
        return button;
    }

    private ItemStack getInfoButton(final AngelChest angelChest, final int id) {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_INFO), main.messages.GUI_INFO, getChestItemLore(angelChest, id));
    }

    private List<String> getLore(final String text) {
        return Arrays.asList(text.split("\n"));
    }

    private ItemStack getPreviewButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_PREVIEW), main.messages.GUI_PREVIEW, null);
    }

    private String replaceBalancePlaceholders(Player player, double price, String text) {
        String currency = CommandUtils.getCurrency(price);
        return text.replace("{price}",main.getCurrencyFormatter().format(price)).replace("{currency}",currency).replace("{balance}",main.getCurrencyFormatter().format(CommandUtils.getBalance(player)));
    }

    private ItemStack getTPButton(Player player) {
        double price = main.groupManager.getTeleportPricePerPlayer(player);
        ItemStack button = getButton(main.getConfig().getString(Config.GUI_BUTTON_TELEPORT), replaceBalancePlaceholders(player, price, main.messages.GUI_TELEPORT), null);
        LoreUtils.applyLore(button, LoreUtils.applyNewlines(replaceBalancePlaceholders(player, price, main.getConfig().getString(Config.GUI_TELEPORT_LORE))));
        return button;
    }

    private String getTitle(final AngelChest chest, final int id) {
        return main.messages.GUI_TITLE_CHEST.replace("{id}", String.valueOf(id)).replace("{time}", CommandUtils.getTimeLeft(chest)).replace("{player}",Bukkit.getOfflinePlayer(chest.owner).getName());

    }

    private ItemStack getUnlockButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_UNLOCK), main.messages.GUI_UNLOCK, null);
    }

    private boolean hasOpen(final Player player, final Inventory inventory) {
        if (player.getOpenInventory() == null) return false;
        if (player.getOpenInventory().getTopInventory() == null) return false;
        return player.getOpenInventory().getTopInventory().equals(inventory);
    }

    public void showChestGUI(final Player player, final GUIHolder holder, final int id) {
        final AngelChest angelChest = holder.getAngelChest();
        final GUIHolder newHolder = new GUIHolder(player, GUIContext.CHEST_MENU, id);
        final Inventory inventory = Bukkit.createInventory(newHolder, 9, getTitle(holder.getAngelChest(), holder.getChestIdStartingAt1()));
        newHolder.setInventory(inventory);

        inventory.setItem(GUI.SLOT_CHEST_BACK, getBackButton());
        inventory.setItem(GUI.SLOT_CHEST_INFO, getInfoButton(angelChest, id));
        if (player.hasPermission(Permissions.TP)) inventory.setItem(GUI.SLOT_CHEST_TP, getTPButton(player));
        if (player.hasPermission(Permissions.FETCH)) inventory.setItem(GUI.SLOT_CHEST_FETCH, getFetchButton(player));
        if (player.hasPermission(Permissions.PROTECT) && angelChest.isProtected && player.hasPermission(Permissions.UNLOCK))
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

        inventory.setItem(GUI.SLOT_CONFIRM_INFO, getConfirmInfoButton(player, action.getPrice(player)));
        inventory.setItem(GUI.SLOT_CONFIRM_ACCEPT, getConfirmAcceptButton());
        inventory.setItem(GUI.SLOT_CONFIRM_DECLINE, getConfirmDeclineButton());
        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            try {
                if (hasOpen(player, inventory)) {
                    showConfirmGUI(player, holder, holder.getAction());
                }
            } catch (final NullPointerException ignored) {
                //if(main.debug) main.debug("Null in repeating task in showConfirmGUI");
            }
        }, Ticks.fromSeconds(1));
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
        final Inventory inventory = Bukkit.createInventory(holder, inventorySize, main.messages.GUI_TITLE_MAIN.replace("{player}",player.getName()));
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
        if(!isPreview) {
            AngelChestOpenEvent openEvent = new AngelChestOpenEvent(angelChest, player, AngelChestOpenEvent.Reason.OPEN_GUI);
            Bukkit.getPluginManager().callEvent(openEvent);
            if(openEvent.isCancelled()) {
                main.debug("AngelChestOpenEvent (Open GUI) was cancelled.");
                return;
            }
        }
        final GUIHolder holder = new GUIHolder(player, GUIContext.PREVIEW_MENU);
        final Inventory inventory = Bukkit.createInventory(holder, 54, main.messages.GUI_TITLE_MAIN.replace("{player}",player.getName()));
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

        if (Daddy_Stepsister.allows(PremiumFeatures.GENERIC)) { // Don't add feature here
            if (!isPreview && firstOpened && !player.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_CHEST)) {
                final Player tmpPlayer = Bukkit.getPlayer(angelChest.owner);
                if (tmpPlayer != null) {
                    Messages.send(tmpPlayer, main.messages.MSG_OPENED.replace("{player}", player.getName()));
                }
            }
        }

        player.openInventory(inventory);
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

    public void updatePreviewInvs(final @Nullable Player originalPlayer, final AngelChest angelChest) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(originalPlayer)) continue;
            if (player.getOpenInventory() == null) continue;
            if (player.getOpenInventory().getTopInventory() == null) continue;
            if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder)) continue;
            final GUIHolder guiHolder = (GUIHolder) player.getOpenInventory().getTopInventory().getHolder();
            if (guiHolder.getSpecialAngelChest() != null && guiHolder.getSpecialAngelChest().equals(angelChest)) {

                if (main.debug)
                    main.debug("This AngelChest " + angelChest + " is also in use by " + player.getName() + ", updating...");
                if (!angelChest.isEmpty()) {
                    showPreviewGUI(player, angelChest, guiHolder.isReadOnlyPreview(), false);
                } else {
                    player.closeInventory();
                }
            }
        }
    }


}
