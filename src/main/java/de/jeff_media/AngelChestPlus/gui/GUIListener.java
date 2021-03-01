package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.config.Config;
import de.jeff_media.AngelChestPlus.config.Permissions;
import de.jeff_media.AngelChestPlus.enums.TeleportAction;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
import de.jeff_media.AngelChestPlus.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class GUIListener implements @NotNull Listener {

    private final Main main;

    public GUIListener() {
        this.main = Main.getInstance();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(InventoryDragEvent event) {
        if (event.getInventory() == null) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        main.debug("[GUIListener] "+"cancel(InventoryDragEvent): cancelled -> true");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(InventoryInteractEvent event) {
        if (event.getInventory() == null) return;
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        main.debug("[GUIListener] "+"cancel(InventoryInteractEvent): cancelled -> true");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(InventoryMoveItemEvent event) {
        if (event.getSource() != null) {
            if (event.getSource().getHolder() instanceof GUIHolder) {
                main.debug("[GUIListener] "+"cancel(InventoryMoveItemEvent): cancelled -> true (1)");
                event.setCancelled(true);
            }
        }
        if (event.getDestination() != null) {
            if (event.getDestination().getHolder() instanceof GUIHolder) {
                main.debug("[GUIListener] "+"cancel(InventoryMoveItemEvent): cancelled -> true (2)");
                event.setCancelled(true);
            }
        }
        //main.debug("[GUIListener] "+"cancel(InventoryMoveItemEvent): cancelled -> true (3)");
        //event.setCancelled(true);
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
                main.debug("[GUIListener] "+"Saved new chest contents!");
            }
        }
    }
*/
    @SuppressWarnings("DefaultAnnotationParam")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPreviewGUIClick(InventoryClickEvent event) {
        /*System.out.println("Current item: " + event.getCurrentItem());
        System.out.println("Cursor: " + event.getCursor());*/

        /*if(!event.isCancelled()) {
            System.out.println("Return: event is not cancelled");
            return;
        }*/
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) {
            main.debug("Return: getClicked Inv is null");
            return;
        }
        if (!(event.getClickedInventory().getHolder() instanceof GUIHolder)) {
            main.debug("Return: clicked inventory is no GUIHolder");
            return;
        }
        GUIHolder guiHolder = (GUIHolder) event.getClickedInventory().getHolder();
        if (guiHolder.getContext() != GUIContext.PREVIEW_MENU) {
            main.debug("Return: GUICOntext is not PREVIEW");
            return;
        }



        if (guiHolder.isReadOnlyPreview()) {

            if(event.getSlot() == GUI.SLOT_PREVIEW_BACK) {
                main.debug("[GUIListener] "+"Preview -> Back");
                GUIHolder newHolder = new GUIHolder(player,GUIContext.MAIN_MENU, guiHolder.getChestIdStartingAt1()+1);
                main.guiManager.showChestGUI(player, newHolder, newHolder.getChestIdStartingAt1());
                return;
            }

            main.debug("Return: This is a read only preview");
            return;
        }

        // Only continue if cursor is empty
        if (isAngelChestPreviewGUI(event.getView())) {
            main.debug("[GUIListener] "+"1");
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                main.debug("[GUIListener] "+"2");
                if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder) {
                    main.debug("[GUIListener] "+"3!!");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (GUIUtils.isLootableInPreview(event.getSlot())) {
            main.debug("[GUIListener] "+"onPreviewGUIClick: cancelled -> false");

            ItemStack clickedItem = event.getCurrentItem();
            //event.getClickedInventory().remove(clickedItem);
            int clickedSlot = event.getSlot();

            File logfile = main.logger.getLogFile(guiHolder.getAngelChest().logfile);

            if(clickedSlot == GUI.SLOT_PREVIEW_XP) {
                main.logger.logXPTaken(player,guiHolder.getAngelChest().experience,logfile);
                Utils.applyXp(player, guiHolder.getAngelChest());
                event.getClickedInventory().setItem(GUI.SLOT_PREVIEW_XP,null);
            } else {

                event.getClickedInventory().setItem(clickedSlot, null);
                if(clickedItem!=null) {
                    main.debug("[GUIListener] " + "Adding " + clickedItem.toString());
                    main.logger.logItemTaken(player,clickedItem,logfile);
                }
                HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(clickedItem);
                for (ItemStack leftOver : leftOvers.values()) {
                    event.getClickedInventory().setItem(clickedSlot, leftOver);
                }
            }
            GUIUtils.savePreviewInventoryToChest(event.getClickedInventory(), guiHolder.getAngelChest());
            main.guiManager.updatePreviewInvs(player, guiHolder.getAngelChest());
            //GUIUtils.printPreviewIntentory(event.getClickedInventory().getContents());
            event.setCancelled(true);
        }
    }

    public boolean isAngelChestPreviewGUI(InventoryView view) {
        if (view.getTopInventory() != null) {
            return view.getTopInventory().getHolder() instanceof GUIHolder;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGUIClick(InventoryClickEvent event) {

        if (event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof GUIHolder) {
            GUIHolder holder = (GUIHolder) event.getView().getTopInventory().getHolder();
            if (holder.getContext() == GUIContext.PREVIEW_MENU) {
                if (!holder.isReadOnlyPreview()) {
                    main.debug("[GUIListener] "+"onGUIClick: abort: this is a writeable preview context");
                    return;
                }
            }
        }

        if (!(event.getWhoClicked() instanceof Player)) return;
        InventoryView view = event.getView();

        if (event.getInventory() != null) {
            if (event.getInventory().getHolder() instanceof GUIHolder) {
                main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (1)");
                event.setCancelled(true);
            }
        }

        if (event.getClickedInventory() != null) {
            if (event.getClickedInventory().getHolder() instanceof GUIHolder) {
                main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (2)");
                event.setCancelled(true);
            }
        }

        if (view == null) return;
        if (view.getTopInventory() != null) {
            if (view.getTopInventory().getHolder() instanceof GUIHolder) {
                main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (3)");
                event.setCancelled(true);
            }
        }

        if (view.getBottomInventory() != null) {
            if (view.getBottomInventory().getHolder() instanceof GUIHolder) {
                main.debug("[GUIListener] "+"onGUIClick: cancelled -> true (4)");
                event.setCancelled(true);
            }
        }

        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof GUIHolder)) return;

        GUIHolder holder = (GUIHolder) event.getClickedInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int clickedSlot = event.getSlot();

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

    private void onGUIClickConfirmMenu(InventoryClickEvent event, Player player, GUIHolder holder, int clickedSlot) {
        TeleportAction action = holder.getAction();
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

    private void onGUIClickMainMenu(InventoryClickEvent event, Player player, GUIHolder holder, int clickedSlot) {
        int clickedID = clickedSlot + 1;
        if (clickedID > holder.getNumberOfAngelChests()) return;
        holder.setChestIdStartingAt1(clickedID);
        main.guiManager.showChestGUI(player, holder, clickedID);
    }

    private void onGUIClickChestMenu(InventoryClickEvent event, Player player, GUIHolder holder, int clickedSlot) {
        switch (clickedSlot) {
            case GUI.SLOT_CHEST_BACK:
                main.guiManager.showMainGUI(player);
                break;

            case GUI.SLOT_CHEST_TP:
                if (player.hasPermission(Permissions.ALLOW_TELEPORT)) {
                    confirmOrTeleport(event, player, holder, TeleportAction.TELEPORT_TO_CHEST);
                }
                break;

            case GUI.SLOT_CHEST_FETCH:
                if (player.hasPermission(Permissions.ALLOW_FETCH)) {
                    confirmOrTeleport(event, player, holder, TeleportAction.FETCH_CHEST);
                }
                break;

            case GUI.SLOT_CHEST_UNLOCK:
                if (player.hasPermission(Permissions.ALLOW_PROTECT) && holder.getAngelChest().isProtected) {
                    CommandUtils.unlockSingleChest(main, player, player, holder.getAngelChest());
                }
                break;

            case GUI.SLOT_CHEST_PREVIEW:
                if (player.hasPermission(Permissions.ALLOW_PREVIEW)) {
                    main.guiManager.showPreviewGUI(player, holder.getAngelChest(), true);
                }

            default:
                break;
        }
    }

    private void confirmOrTeleport(InventoryClickEvent event, Player player, GUIHolder holder, TeleportAction action) {
        if (main.getConfig().getBoolean(Config.CONFIRM) && action.getPrice(player)>0.0d) {
            main.guiManager.showConfirmGUI(player, holder, action);
        } else {
            CommandUtils.fetchOrTeleport(main, player, holder.getAngelChest(), holder.getChestIdStartingAt1(), action, false);
            player.closeInventory();
        }
    }
}
