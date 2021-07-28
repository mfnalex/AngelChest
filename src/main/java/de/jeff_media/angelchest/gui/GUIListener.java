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
import java.util.HashSet;
import java.util.Objects;

public final class GUIListener implements @NotNull Listener {

    private final Main main;
    // An AngelChest that has already been interacted with on this tick will not be interactable with until next tick
    private final HashSet<AngelChest> alreadyInteracted = new HashSet<>();

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
//            if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryDragEvent): cancelled -> true");
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(final InventoryInteractEvent event) {
        if (event.getInventory() == null) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
//        if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryInteractEvent): cancelled -> true");
        GUIHolder holder = (GUIHolder) event.getInventory().getHolder();
        if(!holder.isReadOnlyPreview() && holder.getContext()==GUIContext.PREVIEW_MENU) {

        } else {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(final InventoryMoveItemEvent event) {
        if (event.getSource() != null) {
            if (event.getSource().getHolder() instanceof GUIHolder) {
//                if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryMoveItemEvent): cancelled -> true (1)");
                event.setCancelled(true);
            }
        }
        if (event.getDestination() != null) {
            if (event.getDestination().getHolder() instanceof GUIHolder) {
//                if (main.debug) main.debug("[GUIListener] " + "cancel(InventoryMoveItemEvent): cancelled -> true (2)");
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
//            if (main.debug) main.debug("Prevented ChestSort from sorting AngelChest GUI");
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
//                    //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: abort: generic");
                }
            }
        }

        if (event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof GUIHolder) {
            final GUIHolder holder = (GUIHolder) event.getView().getTopInventory().getHolder();
            if (holder.getContext() == GUIContext.PREVIEW_MENU) {
                if (!holder.isReadOnlyPreview()) {
                    if (event.getClickedInventory() != null && event.getView().getTopInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
//                        //if(main.debug) main.debug("[GUIListener] " + "onGUIClick: abort: this is a writeable preview context");
                        return;
                    }
                }
            }

        }

        if (!(event.getWhoClicked() instanceof Player)) return;
        final InventoryView view = event.getView();

        if (event.getInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.getInventory().getHolder() instanceof GUIHolder) {
//                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (1)");
                event.setCancelled(true);
            }
        }

        if (event.getClickedInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.getClickedInventory().getHolder() instanceof GUIHolder) {
//                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (2)");
                event.setCancelled(true);
            }
        }

        if (view == null) return;
        if (view.getTopInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (view.getTopInventory().getHolder() instanceof GUIHolder) {
//                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (3)");
                event.setCancelled(true);
            }
        }

        if (view.getBottomInventory() != null && event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (view.getBottomInventory().getHolder() instanceof GUIHolder) {
//                //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (4)");
                event.setCancelled(true);
            }
        }

        if (view.getTopInventory() != null && event.getClickedInventory() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof GUIHolder && event.getClickedInventory() != null && event.getView().getBottomInventory() != null && event.getClickedInventory().equals(event.getView().getBottomInventory()) && event.isShiftClick()) {
//            //if(main.debug) main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (5)");
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
                if (player.hasPermission(Permissions.PROTECT) && holder.getAngelChest().isProtected) {
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


//        main.debug("========================================");
//        main.debug(event.getAction().name());
        if(event.getAction()==InventoryAction.HOTBAR_SWAP) {
//            if(main.debug) main.debug("  Cursor: " + event.getCursor());
//            if(main.debug) main.debug("  CurrentItem: " + event.getCurrentItem());
//            if(main.debug) main.debug("  Hotbar Button: " + event.getHotbarButton()+"\n");
        }



        if (!(event.getWhoClicked() instanceof Player)) {
//            if(main.debug) main.debug("Return: getWhoClicked != Player");
            return;
        }
        final Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) {
//            if(main.debug) main.debug("Return: getClicked Inv is null");
            return;
        }
        if (!(event.getClickedInventory().getHolder() instanceof GUIHolder)) {
//            if(main.debug) main.debug("Return: clicked inventory is no GUIHolder");
            return;
        }
        final GUIHolder guiHolder = (GUIHolder) event.getClickedInventory().getHolder();
        if (guiHolder.getContext() != GUIContext.PREVIEW_MENU) {
//            if(main.debug) main.debug("Return: GUICOntext is not PREVIEW");
            return;
        }

        // TODO DEBUG DUPE START
        /*if(event.getClick() == ClickType.SHIFT_LEFT) {
            PlayerListener.fastLoot(player, guiHolder.getAngelChest(), true);
        }*/
        // TODO DEBUG DUPE END


        if (guiHolder.isReadOnlyPreview()) {

            if (event.getSlot() == GUI.SLOT_PREVIEW_BACK) {
//                if(main.debug) main.debug("[GUIListener] "+"Preview -> Back");
                final GUIHolder newHolder = new GUIHolder(player, GUIContext.MAIN_MENU, guiHolder.getChestIdStartingAt1() + 1);
                main.guiManager.showChestGUI(player, newHolder, newHolder.getChestIdStartingAt1());
                return;
            }

//            if(main.debug) main.debug("Return: This is a read only preview");
            return;
        }

        // Only continue if cursor is empty
        if (isAngelChestPreviewGUI(event.getView())) {
//            if(main.debug) main.debug("[GUIListener] "+"1");
//            main.debug(event.getCursor()+"");
//            if(event.getCursor()!=null) main.debug(event.getCursor().getType()+"");
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
//                if(main.debug) main.debug("[GUIListener] "+"2");
                if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder) {
//                    if(main.debug) main.debug("[GUIListener] "+"3!!");
                    event.setCancelled(true);
                    return;
                }
            }
        }

//        main.debug("Continuing");

        if (GUIUtils.isLootableInPreview(event.getSlot())) {

//            if(main.debug) main.debug("[GUIListener] "+"onPreviewGUIClick: cancelled -> false");

            final ItemStack clickedItem = event.getCurrentItem();
            //event.getClickedInventory().remove(clickedItem);
            final int clickedSlot = event.getSlot();

            final AngelChest angelChest = guiHolder.getAngelChest();

            if(alreadyInteracted.contains(angelChest)) {
                main.debug("Preventing double interaction on same tick");
                event.setCancelled(true);
                return;
            }

            if(angelChest.isLooted) {
                event.setCancelled(true);
                main.getLogger().warning("GUI click made in already looted chest - possible duplication attempt, or player is just lagging very hard: " + player.getName());
                return;
            }

            final File logfile = main.logger.getLogFile(angelChest.logfile);
            boolean cancel = true;

            if (clickedSlot == GUI.SLOT_PREVIEW_XP) {
                main.logger.logXPTaken(player, angelChest.experience, logfile);
                Utils.applyXp(player, angelChest);
                event.getClickedInventory().setItem(GUI.SLOT_PREVIEW_XP, null);
            } else {
                // TEST START
                //event.getClickedInventory().setItem(clickedSlot, null);
                if (clickedItem == null) {
//                    if(main.debug) main.debug("Return: ClickedItem == null");
                    event.setCancelled(true);
                    return;
                }
                if(event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
//                    if(main.debug) main.debug("Return: InventoryAction == HOTBAR_MOVE_AND_READD");
                    event.setCancelled(true);
                    return;
                }
//                main.debug("1");
                //final HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(clickedItem);
                //for (final ItemStack leftOver : leftOvers.values()) {
                //    event.getClickedInventory().setItem(clickedSlot, leftOver);
                //}
                // TEST END

                if(!GUIUtils.isPlaceholder(clickedItem)) {

                    System.out.println("Cursor: "+event.getCursor());
                    System.out.println("Currentitem: "+event.getCurrentItem());
                    System.out.println("Slot: "+event.getSlot());
                    System.out.println("Item at Slot: " + event.getClickedInventory().getItem(event.getSlot()));

                    InventoryClickEvent clickEvent = new InventoryClickEvent(event.getView(), InventoryType.SlotType.CONTAINER,0,ClickType.SHIFT_LEFT,InventoryAction.NOTHING);
                    Bukkit.getPluginManager().callEvent(clickEvent);

                    /*if(Objects.equals(event.getCurrentItem(), event.getClickedInventory().getItem(event.getSlot()))) {
                        System.out.println("CurrentItem == Item at Slot");
                        ItemStack tmp = event.getClickedInventory().getItem(event.getSlot()).clone();
                        event.getClickedInventory().getItem(event.getSlot()).setAmount(0);
                        event.setCurrentItem(tmp);
                        GUIUtils.savePreviewInventoryToChest(event.getClickedInventory(),angelChest);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                            main.guiManager.updatePreviewInvs(player, angelChest);
                        },1);
                    } else {
                        System.out.println(event.getClickedInventory().getItem(event.getSlot()) + " != " + event.getCurrentItem());
                    }*/

//                    main.debug("2");
                    System.out.println("asd1");
                    cancel = false;
                    ItemStack logItem = clickedItem.clone();
                    if(event.isRightClick()) {
                        logItem.setAmount(logItem.getAmount()/2);
                        System.out.println("asd2");
                    }
                    if(event.getAction()==InventoryAction.HOTBAR_SWAP) {
                        System.out.println("asd3");
                        ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                        if(hotbarItem!=null && hotbarItem.getAmount() > 0) {
//                            if(main.debug) main.debug("Return: Hotbar_Swap but hotbar slot not empty");
                            event.setCancelled(true);
                            System.out.println("asd4");
                            return;
                        } else {
//                            if(main.debug) main.debug("Hotbar swap, cloning item...");
                            player.getInventory().setItem(event.getHotbarButton(), clickedItem.clone());
                            event.getClickedInventory().setItem(event.getSlot(),null);
                            cancel = true;
                        }
                    }
//                    if(main.debug) main.debug("[GUIListener] " + "Adding " + logItem);
                    main.logger.logItemTaken(player, logItem, logfile);
                }

            }
            GUIUtils.savePreviewInventoryToChest(event.getClickedInventory(), angelChest);
            //GUIUtils.printPreviewIntentory(event.getClickedInventory().getContents());
            //DEBUG main.guiManager.updatePreviewInvs(player, angelChest);

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                //DEBUG main.guiManager.updatePreviewInvs(player, angelChest);
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    alreadyInteracted.remove(angelChest);
                }, 1);
                if (angelChest.isEmpty()) {
                    main.logger.logLastItemTaken(player, logfile);
                    for (final HumanEntity viewer : event.getClickedInventory().getViewers().toArray(new HumanEntity[0])) {
                        viewer.closeInventory();
                    }
                    if (Stepsister.allows(PremiumFeatures.GENERIC)) { // Don't add feature here
                        if (!player.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_CHEST)) {
                            final Player tmpPlayer = Bukkit.getPlayer(angelChest.owner);
                            if (tmpPlayer != null) {
                                Messages.send(tmpPlayer, main.messages.MSG_EMPTIED.replace("{player}", player.getName()));
                            }
                        }
                    }
                    //Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                        angelChest.destroy(false);
                        angelChest.remove();
                    //}, 1L);
                }
            },1);
            if(cancel) {
                event.setCancelled(true);
            } else {
                alreadyInteracted.add(angelChest);
            }
        } else {
            event.setCancelled(true);
        }
    }
}
