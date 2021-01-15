package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.jetbrains.annotations.NotNull;

public class GUIListener implements @NotNull Listener {

    private final Main main;

    public GUIListener(Main main) {
        this.main=main;
    }

    @EventHandler
    public void cancel(InventoryDragEvent event) {
        if(event.getInventory() == null) return;
        if(!(event.getInventory().getHolder() instanceof AngelChestGUIHolder)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void cancel(InventoryInteractEvent event) {
        if(event.getInventory() == null) return;
        if(!(event.getInventory().getHolder() instanceof AngelChestGUIHolder)) return;
        event.setCancelled(true);
    }

/*    @EventHandler
    public void cancel(InventoryMoveItemEvent event) {
        if(event.getInitiator() != null) {
            if(event.getInitiator().getHolder() instanceof AngelChestGUIHolder) {
                event.setCancelled(true);
            }
        }
        if(!(event.getInventory().getHolder() instanceof AngelChestGUIHolder)) return;
        event.setCancelled(true);
    }*/

    @EventHandler(priority= EventPriority.LOWEST)
    public void onGUIClick(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player)) return;

        if(event.getInventory() == null) return;
        if(!(event.getClickedInventory().getHolder() instanceof AngelChestGUIHolder)) return;
        event.setCancelled(true);
        //event.setResult(Event.Result.DENY);

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
                break;
        }
    }
}
