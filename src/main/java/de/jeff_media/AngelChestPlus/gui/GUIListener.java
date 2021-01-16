package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.Config;
import de.jeff_media.AngelChestPlus.Permissions;
import de.jeff_media.AngelChestPlus.TeleportAction;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
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

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void cancel(InventoryDragEvent event) {
        if(event.getInventory() == null) return;
        if(!(event.getInventory().getHolder() instanceof AngelChestGUIHolder)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void cancel(InventoryInteractEvent event) {
        if(event.getInventory() == null) return;
        if(!(event.getInventory().getHolder() instanceof AngelChestGUIHolder)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void cancel(InventoryMoveItemEvent event) {
        if(event.getSource() != null) {
            if(event.getSource().getHolder() instanceof AngelChestGUIHolder) {
                event.setCancelled(true);
            }
        }
        if(event.getDestination() != null) {
            if(event.getDestination().getHolder() instanceof AngelChestGUIHolder) {
                event.setCancelled(true);
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGUIClick(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player)) return;
        InventoryView view = event.getView();

        if(event.getInventory() != null) {
            if(event.getInventory().getHolder() instanceof AngelChestGUIHolder) {
                event.setCancelled(true);
            }
        }

        if(event.getClickedInventory() != null) {
            if(event.getClickedInventory().getHolder() instanceof AngelChestGUIHolder) {
                event.setCancelled(true);
            }
        }

        if(view == null) return;
        if(view.getTopInventory() != null) {
            if(view.getTopInventory().getHolder() instanceof AngelChestGUIHolder) {
                event.setCancelled(true);
            }
        }

        if(view.getBottomInventory() != null) {
            if(view.getBottomInventory().getHolder() instanceof AngelChestGUIHolder) {
                event.setCancelled(true);
            }
        }

        if(event.getClickedInventory() == null) return;
        if(!(event.getClickedInventory().getHolder() instanceof AngelChestGUIHolder)) return;

        AngelChestGUIHolder holder = (AngelChestGUIHolder) event.getClickedInventory().getHolder();
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

    private void onGUIClickConfirmMenu(InventoryClickEvent event, Player player, AngelChestGUIHolder holder, int clickedSlot) {
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

    private void onGUIClickMainMenu(InventoryClickEvent event, Player player, AngelChestGUIHolder holder, int clickedSlot) {
        int clickedID = clickedSlot+1;
        if(clickedID > holder.getNumberOfAngelChests()) return;
        holder.setChestIdStartingAt1(clickedID);
        main.guiManager.showChestGUI(player,holder, clickedID);
    }

    private void onGUIClickChestMenu(InventoryClickEvent event, Player player, AngelChestGUIHolder holder, int clickedSlot) {
        switch (clickedSlot) {
            case GUI.SLOT_BACK:
                main.guiManager.showMainGUI(player);
                break;

            case GUI.SLOT_TP:
                if(player.hasPermission(Permissions.ALLOW_TELEPORT)) {
                    confirmOrTeleport(event, player, holder, TeleportAction.TELEPORT_TO_CHEST);
                }
                break;

            case GUI.SLOT_FETCH:
                if(player.hasPermission(Permissions.ALLOW_FETCH)) {
                    confirmOrTeleport(event, player, holder, TeleportAction.FETCH_CHEST);
                }
                break;

            case GUI.SLOT_UNLOCK:
                if(player.hasPermission(Permissions.ALLOW_PROTECT) && holder.getAngelChest().isProtected) {
                    CommandUtils.unlockSingleChest(main, player, player, holder.getAngelChest());
                }
                break;

            case GUI.SLOT_PREVIEW:
                if(player.hasPermission(Permissions.ALLOW_PREVIEW)) {
                    main.guiManager.showPreviewGUI(player, holder.getAngelChest());
                }

            default:
                break;
        }
    }

    private void confirmOrTeleport(InventoryClickEvent event, Player player, AngelChestGUIHolder holder, TeleportAction action) {
        if(main.getConfig().getBoolean(Config.CONFIRM)) {
            main.guiManager.showConfirmGUI(player,holder,action);
        } else {
            CommandUtils.fetchOrTeleport(main, player, holder.getAngelChest(), holder.getChestIdStartingAt1(), action, false);
            player.closeInventory();
        }
    }
}
