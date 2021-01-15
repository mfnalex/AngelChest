package de.jeff_media.AngelChestPlus.gui;

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
        }

    }

    private void onGUIClickMainMenu(InventoryClickEvent event, Player player, AngelChestGUIHolder holder, int clickedSlot) {
        int clickedID = clickedSlot+1;
        if(clickedID > holder.getNumberOfAngelChests()) return;
        main.guiManager.showChestGUI(player,holder, clickedID);
    }

    private void onGUIClickChestMenu(InventoryClickEvent event, Player player, AngelChestGUIHolder holder, int clickedSlot) {
        switch (clickedSlot) {
            case GUI.SLOT_BACK:
                main.guiManager.showMainGUI(player);
                break;

            case GUI.SLOT_TP:
                CommandUtils.fetchOrTeleport(main,player,holder.getAngelChest(), holder.getChestId(), TeleportAction.TELEPORT_TO_CHEST,false);
                //CommandUtils.teleportPlayerToChest(main,player,holder.getAngelChests().get(holder.getChestId()-1),new String[] {});
                break;

            case GUI.SLOT_FETCH:
                CommandUtils.fetchOrTeleport(main,player,holder.getAngelChest(), holder.getChestId(), TeleportAction.FETCH_CHEST,false);
                break;

            default:
                break;
        }
    }
}
