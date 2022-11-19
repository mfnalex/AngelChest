package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.gui.GUIHolder;
import de.jeff_media.chestsort.api.ChestSortEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChestSortListener implements Listener {

    private static final Main main = Main.getInstance();

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestSortEvent(final ChestSortEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof GUIHolder) {
            if (main.debug) main.debug("Prevented ChestSort from sorting AngelChest GUI");
            event.setCancelled(true);
        }
    }

}
