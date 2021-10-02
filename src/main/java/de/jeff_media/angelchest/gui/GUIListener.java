package de.jeff_media.angelchest.gui;

import de.jeff_media.ChestSortAPI.ChestSortEvent;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.enums.EconomyStatus;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.listeners.PlayerListener;
import de.jeff_media.angelchest.utils.CommandUtils;
import de.jeff_media.angelchest.utils.Utils;
import de.jeff_media.daddy.Stepsister;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public final class GUIListener implements @NotNull Listener {

    private final Main main;

    public GUIListener() {
        this.main = Main.getInstance();
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(final InventoryDragEvent event) {
        if (event.getInventory() == null) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        int minSlot = 999;
        for (final int i : event.getRawSlots()) {
            minSlot = Math.min(i, minSlot);
        }
        // No other way to detect top or bottom inventory
        if (minSlot < 54) {
            if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryDragEvent): cancelled -> true");
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(final InventoryInteractEvent event) {
        if (event.getInventory() == null) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryInteractEvent): cancelled -> true");
        event.setCancelled(true);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(final InventoryMoveItemEvent event) {
        if (event.getSource() != null) {
            if (event.getSource().getHolder() instanceof GUIHolder) {
                if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryMoveItemEvent): cancelled -> true (1)");
                event.setCancelled(true);
            }
        }
        if (event.getDestination() != null) {
            if (event.getDestination().getHolder() instanceof GUIHolder) {
                if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryMoveItemEvent): cancelled -> true (2)");
                event.setCancelled(true);
            }
        }
        //if(main.debug) main.debug("[GUIListener] "+"cancel(InventoryMoveItemEvent): cancelled -> true (3)");
        //event.setCancelled(true);
    }

    @SuppressWarnings("unused")
    private void confirmOrTeleport(final InventoryClickEvent event, final Player player, final GUIHolder holder, final CommandAction action) {
        if (main.getConfig().getBoolean(Config.CONFIRM) && action.getPrice(player) > 0.0d && main.economyStatus == EconomyStatus.ACTIVE) {
            main.guiManager.showConfirmGUI(player, holder, action);
        } else {
            CommandUtils.fetchOrTeleport(main, player, holder.getAngelChest(), holder.getChestIdStartingAt1(), action, false);
            player.closeInventory();
        }
    }

    public boolean isAngelChestPreviewGUI(final InventoryView view) {
        if (view.getTopInventory() != null) {
            return view.getTopInventory().getHolder() instanceof GUIHolder;
        }
        return false;
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestSortEvent(final ChestSortEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof GUIHolder) {
            if (main.debug) main.debug("Prevented ChestSort from sorting AngelChest GUI");
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGUIClick(final InventoryClickEvent event) {

        // Cancel all clicks in GUI except Preview Menu
        if (event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof GUIHolder) {
            final GUIHolder tmpGUIHolder = (GUIHolder) event.getView().getTopInventory().getHolder();
            if (tmpGUIHolder.getContext() != null) {
                if (tmpGUIHolder.getContext() != GUIContext.PREVIEW_MENU || (tmpGUIHolder.getContext() == GUIContext.PREVIEW_MENU && tmpGUIHolder.isReadOnlyPreview())) {
                    event.setCancelled(true);
                    //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: abort: generic");
                }
            }
        }

        if (event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof GUIHolder) {
            final GUIHolder holder = (GUIHolder) event.getView().getTopInventory().getHolder();
            if (holder.getContext() == GUIContext.PREVIEW_MENU) {
                if (!holder.isReadOnlyPreview()) {
                    if (event.getClickedInventory() != null && event.getView().getTopInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                        //if(main.debug) main.debug("[GUIListener] " + "onGUIClick: abort: this is a writeable preview context");
                        return;
                    }
                }
            }

        }

        if (!(event.getWhoClicked() instanceof Player)) return;
        final InventoryView view = event.getView();

        if (event.getInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.getInventory().getHolder() instanceof GUIHolder) {
                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (1)");
                event.setCancelled(true);
            }
        }

        if (event.getClickedInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.getClickedInventory().getHolder() instanceof GUIHolder) {
                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (2)");
                event.setCancelled(true);
            }
        }

        if (view == null) return;
        if (view.getTopInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (view.getTopInventory().getHolder() instanceof GUIHolder) {
                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (3)");
                event.setCancelled(true);
            }
        }

        if (view.getBottomInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (view.getBottomInventory().getHolder() instanceof GUIHolder) {
                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (4)");
                event.setCancelled(true);
            }
        }

        if (view.getTopInventory() != null && event.getClickedInventory() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof GUIHolder && event.getClickedInventory() != null && event.getView().getBottomInventory() != null && event.getClickedInventory().equals(event.getView().getBottomInventory()) && event.isShiftClick()) {
            //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (5)");
            event.setCancelled(true);
        }

        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof GUIHolder)) return;

        final GUIHolder holder = (GUIHolder) event.getClickedInventory().getHolder();
        final Player player = (Player) event.getWhoClicked();
        final int clickedSlot = event.getSlot();

        switch (holder.getContext()) {
            case MAIN_MENU:
                onGUIClickMainMenu(event, player, holder, clickedSlot);
                break;
            case CHEST_MENU:
                onGUIClickChestMenu(event, player, holder, clickedSlot);
                break;
            case CONFIRM_MENU:
                onGUIClickConfirmMenu(event, player, holder, clickedSlot);
                break;
        }

    }

    private void onGUIClickChestMenu(final InventoryClickEvent event, final Player player, final GUIHolder holder, final int clickedSlot) {
        switch (clickedSlot) {
            case GUI.SLOT_CHEST_BACK:
                main.guiManager.showMainGUI(player);
                break;

            case GUI.SLOT_CHEST_TP:
                if (player.hasPermission(Permissions.TP)) {
                    confirmOrTeleport(event, player, holder, CommandAction.TELEPORT_TO_CHEST);
                }
                break;

            case GUI.SLOT_CHEST_FETCH:
                if (player.hasPermission(Permissions.FETCH)) {
                    confirmOrTeleport(event, player, holder, CommandAction.FETCH_CHEST);
                }
                break;

            case GUI.SLOT_CHEST_UNLOCK:
                if (player.hasPermission(Permissions.PROTECT) && holder.getAngelChest().isProtected && player.hasPermission(Permissions.UNLOCK)) {
                    CommandUtils.unlockSingleChest(main, player, holder.getAngelChest());
                }
                break;

            case GUI.SLOT_CHEST_PREVIEW:
                if (player.hasPermission(Permissions.PREVIEW)) {
                    main.guiManager.showPreviewGUI(player, holder.getAngelChest(), true, false);
                }

            default:
                break;
        }
    }

    @SuppressWarnings("unused")
    private void onGUIClickConfirmMenu(final InventoryClickEvent event, final Player player, final GUIHolder holder, final int clickedSlot) {
        final CommandAction action = holder.getAction();
        switch (clickedSlot) {
            case GUI.SLOT_CONFIRM_ACCEPT:
                CommandUtils.fetchOrTeleport(main, player, holder.getAngelChest(), holder.getChestIdStartingAt1(), action, false);
                player.closeInventory();
                break;
            case GUI.SLOT_CONFIRM_DECLINE:
                player.closeInventory();
                player.closeInventory();
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unused")
    private void onGUIClickMainMenu(final InventoryClickEvent event, final Player player, final GUIHolder holder, final int clickedSlot) {
        final int clickedID = clickedSlot + 1;
        if (clickedID > holder.getNumberOfAngelChests()) return;
        holder.setChestIdStartingAt1(clickedID);
        main.guiManager.showChestGUI(player, holder, clickedID);
    }

    /*
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPreviewGUIClickSave(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Inventory inventory = event.getClickedInventory();
            if (inventory != null) {
                if (inventory.getHolder() instanceof GUIHolder) {
                    GUIHolder guiHolder = (GUIHolder) inventory.getHolder();
                    AngelChest angelChest = guiHolder.getAngelChest();
                    GUIUtils.printPreviewIntentory(inventory.getContents());
                    GUIUtils.savePreviewInventoryToChest(inventory, angelChest, main);
                    if(main.debug) main.debug("[GUIListener] "+"Saved new chest contents!");
                }
            }
        }
    */
    @SuppressWarnings({"DefaultAnnotationParam", "unused"})
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPreviewGUIClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        final Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) {
            //if(main.debug) main.debug("Return: getClicked Inv is null");
            return;
        }
        if (!(event.getClickedInventory().getHolder() instanceof GUIHolder)) {
            //if(main.debug) main.debug("Return: clicked inventory is no GUIHolder");
            return;
        }
        final GUIHolder guiHolder = (GUIHolder) event.getClickedInventory().getHolder();
        if (guiHolder.getContext() != GUIContext.PREVIEW_MENU) {
            //if(main.debug) main.debug("Return: GUICOntext is not PREVIEW");
            return;
        }

        // TODO DEBUG DUPE START
        /*if(event.getClick() == ClickType.SHIFT_LEFT) {
            PlayerListener.fastLoot(player, guiHolder.getAngelChest(), true);
        }*/
        // TODO DEBUG DUPE END


        if (guiHolder.isReadOnlyPreview()) {

            if (event.getSlot() == GUI.SLOT_PREVIEW_BACK) {
                //if(main.debug) main.debug("[GUIListener] "+"Preview -> Back");
                final GUIHolder newHolder = new GUIHolder(player, GUIContext.MAIN_MENU, guiHolder.getChestIdStartingAt1() + 1);
                main.guiManager.showChestGUI(player, newHolder, newHolder.getChestIdStartingAt1());
                return;
            }

            //if(main.debug) main.debug("Return: This is a read only preview");
            return;
        }

        // Only continue if cursor is empty
        if (isAngelChestPreviewGUI(event.getView())) {
            //if(main.debug) main.debug("[GUIListener] "+"1");
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                //if(main.debug) main.debug("[GUIListener] "+"2");
                if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder) {
                    //if(main.debug) main.debug("[GUIListener] "+"3!!");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (GUIUtils.isLootableInPreview(event.getSlot())) {

            //if(main.debug) main.debug("[GUIListener] "+"onPreviewGUIClick: cancelled -> false");

            final ItemStack clickedItem = event.getCurrentItem();
            //event.getClickedInventory().remove(clickedItem);
            final int clickedSlot = event.getSlot();

            final AngelChest angelChest = guiHolder.getAngelChest();

            if(angelChest.isLooted) {
                event.setCancelled(true);
                main.getLogger().warning("GUI click made in already looted chest - possible duplication attempt, or player is just lagging very hard: " + player.getName());
                return;
            }

            final File logfile = main.logger.getLogFile(angelChest.logfile);

            if (clickedSlot == GUI.SLOT_PREVIEW_XP) {
                main.logger.logXPTaken(player, angelChest.experience, logfile);
                Utils.applyXp(player, angelChest);
                event.getClickedInventory().setItem(GUI.SLOT_PREVIEW_XP, null);
            } else {
                // TEST START
                event.getClickedInventory().setItem(clickedSlot, null);
                if (clickedItem == null) {
                    return;
                }
                //if(main.debug) main.debug("[GUIListener] " + "Adding " + clickedItem.toString());
                main.logger.logItemTaken(player, clickedItem, logfile);

                final HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(clickedItem);
                for (final ItemStack leftOver : leftOvers.values()) {
                    event.getClickedInventory().setItem(clickedSlot, leftOver);
                }
                // TEST END
            }
            GUIUtils.savePreviewInventoryToChest(event.getClickedInventory(), angelChest);
            main.guiManager.updatePreviewInvs(player, angelChest);
            //GUIUtils.printPreviewIntentory(event.getClickedInventory().getContents());

            if (angelChest.isEmpty()) {
                main.logger.logLastItemTaken(player, logfile);
                for (final HumanEntity viewer : event.getClickedInventory().getViewers().toArray(new HumanEntity[0])) {
                    viewer.closeInventory();
                    if(viewer instanceof Player) {
                        ((Player)viewer).updateInventory();
                    }
                }
                if (Stepsister.allows(PremiumFeatures.GENERIC)) { // Don't add feature here
                    if (!player.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_CHEST)) {
                        final Player tmpPlayer = Bukkit.getPlayer(angelChest.owner);
                        if (tmpPlayer != null) {
                            Messages.send(tmpPlayer, main.messages.MSG_EMPTIED.replace("{player}", player.getName()));
                        }
                    }
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    angelChest.destroy(false, false);
                    angelChest.remove();
                }, 1L);
            }

            event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }
}
