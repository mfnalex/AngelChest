package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.config.Config;
import de.jeff_media.AngelChestPlus.Permissions;
import de.jeff_media.AngelChestPlus.TeleportAction;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
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
import org.jetbrains.annotations.NotNull;

public class GUIListener implements @NotNull Listener {

    private final Main main;

    public GUIListener(Main main) {
        this.main=main;
    }

    @EventHandler(priority= EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(InventoryDragEvent event) {
        if(event.getInventory() == null) return;
        if(!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        main.debug("cancel(InventoryDragEvent): cancelled -> true");
        event.setCancelled(true);
    }

    @EventHandler(priority= EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(InventoryInteractEvent event) {
        if(event.getInventory() == null) return;
        if(!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        main.debug("cancel(InventoryInteractEvent): cancelled -> true");
        event.setCancelled(true);
    }

    @EventHandler(priority= EventPriority.HIGH, ignoreCancelled = true)
    public void cancel(InventoryMoveItemEvent event) {
        if(event.getSource() != null) {
            if(event.getSource().getHolder() instanceof GUIHolder) {
                main.debug("cancel(InventoryMoveItemEvent): cancelled -> true (1)");
                event.setCancelled(true);
            }
        }
        if(event.getDestination() != null) {
            if(event.getDestination().getHolder() instanceof GUIHolder) {
                main.debug("cancel(InventoryMoveItemEvent): cancelled -> true (2)");
                event.setCancelled(true);
            }
        }
        main.debug("cancel(InventoryMoveItemEvent): cancelled -> true (3)");
        event.setCancelled(true);
    }

    @EventHandler(priority =  EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPreviewGUIClick(InventoryClickEvent event) {
        System.out.println("Current item: "+event.getCurrentItem());
        System.out.println("Cursor: "+event.getCursor());

        if(!event.isCancelled()) return;
        if(event.getClickedInventory() == null) return;
        if(!(event.getClickedInventory().getHolder() instanceof GUIHolder)) return;
        GUIHolder guiHolder = (GUIHolder) event.getClickedInventory().getHolder();
        if(guiHolder.getContext() != GUIContext.PREVIEW_MENU) return;
        if(guiHolder.isReadOnlyPreview()) return;

        // Only continue if cursor is empty
        if(isAngelChestPreviewGUI(event.getView())) {
            main.debug("1");
            if(event.getCursor()!=null && event.getCursor().getType() != Material.AIR) {
                main.debug("2");
                if(event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GUIHolder) {
                    main.debug("3!!");
                    return;
                }
            }
        }
        if(GUIUtils.isLootableInPreview(event.getSlot())) {
            main.debug("onPreviewGUIClick: cancelled -> false");
            event.setCancelled(false);
        }
    }

    public boolean isAngelChestPreviewGUI(InventoryView view) {
        if(view.getTopInventory() != null) {
            if(view.getTopInventory().getHolder() instanceof GUIHolder) return true;
        }
        return false;
    }

    @EventHandler(priority= EventPriority.HIGH, ignoreCancelled = true)
    public void onGUIClick(InventoryClickEvent event) {

        if(event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory().getHolder() instanceof GUIHolder) {
            GUIHolder holder = (GUIHolder) event.getView().getTopInventory().getHolder();
            if(holder.getContext() == GUIContext.PREVIEW_MENU) {
                if(!holder.isReadOnlyPreview()) {
                    main.debug("onGUIClick: abort: this is a writeable preview context");
                    return;
                }
            }
        }

        if(!(event.getWhoClicked() instanceof Player)) return;
        InventoryView view = event.getView();

        if(event.getInventory() != null) {
            if(event.getInventory().getHolder() instanceof GUIHolder) {
                main.debug("onGUIClick: cancelled -> true (1)");
                event.setCancelled(true);
            }
        }

        if(event.getClickedInventory() != null) {
            if(event.getClickedInventory().getHolder() instanceof GUIHolder) {
                main.debug("onGUIClick: cancelled -> true (2)");
                event.setCancelled(true);
            }
        }

        if(view == null) return;
        if(view.getTopInventory() != null) {
            if(view.getTopInventory().getHolder() instanceof GUIHolder) {
                main.debug("onGUIClick: cancelled -> true (3)");
                event.setCancelled(true);
            }
        }

        if(view.getBottomInventory() != null) {
            if(view.getBottomInventory().getHolder() instanceof GUIHolder) {
                main.debug("onGUIClick: cancelled -> true (4)");
                event.setCancelled(true);
            }
        }

        if(event.getClickedInventory() == null) return;
        if(!(event.getClickedInventory().getHolder() instanceof GUIHolder)) return;

        GUIHolder holder = (GUIHolder) event.getClickedInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int clickedSlot = event.getSlot();

        switch (holder.getContext()) {
            case MAIN_MENU:
                onGUIClickMainMenu(event,player,holder,clickedSlot);
                break;
            case CHEST_MENU:
                onGUIClickChestMenu(event,player,holder,clickedSlot);
                break;
            case CONFIRM_MENU:
                onGUIClickConfirmMenu(event,player,holder,clickedSlot);
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
        int clickedID = clickedSlot+1;
        if(clickedID > holder.getNumberOfAngelChests()) return;
        holder.setChestIdStartingAt1(clickedID);
        main.guiManager.showChestGUI(player,holder, clickedID);
    }

    private void onGUIClickChestMenu(InventoryClickEvent event, Player player, GUIHolder holder, int clickedSlot) {
        switch (clickedSlot) {
            case GUI.SLOT_CHEST_BACK:
                main.guiManager.showMainGUI(player);
                break;

            case GUI.SLOT_CHEST_TP:
                if(player.hasPermission(Permissions.ALLOW_TELEPORT)) {
                    confirmOrTeleport(event, player, holder, TeleportAction.TELEPORT_TO_CHEST);
                }
                break;

            case GUI.SLOT_CHEST_FETCH:
                if(player.hasPermission(Permissions.ALLOW_FETCH)) {
                    confirmOrTeleport(event, player, holder, TeleportAction.FETCH_CHEST);
                }
                break;

            case GUI.SLOT_CHEST_UNLOCK:
                if(player.hasPermission(Permissions.ALLOW_PROTECT) && holder.getAngelChest().isProtected) {
                    CommandUtils.unlockSingleChest(main, player, player, holder.getAngelChest());
                }
                break;

            case GUI.SLOT_CHEST_PREVIEW:
                if(player.hasPermission(Permissions.ALLOW_PREVIEW)) {
                    main.guiManager.showPreviewGUI(player, holder.getAngelChest(), true);
                }

            default:
                break;
        }
    }

    private void confirmOrTeleport(InventoryClickEvent event, Player player, GUIHolder holder, TeleportAction action) {
        if(main.getConfig().getBoolean(Config.CONFIRM)) {
            main.guiManager.showConfirmGUI(player,holder,action);
        } else {
            CommandUtils.fetchOrTeleport(main, player, holder.getAngelChest(), holder.getChestIdStartingAt1(), action, false);
            player.closeInventory();
        }
    }
}
