package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.AngelChestMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

/**
 * Handles hologram / armor stand related events related to AngelChest
 */
public final class HologramListener implements Listener {

    final AngelChestMain main;

    public HologramListener() {
        this.main = AngelChestMain.getInstance();
    }

    /**
     * Prevents AngelChest holograms from being manipulated
     *
     * @param event PlayerArmorStandManipulateEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void manipulate(final PlayerArmorStandManipulateEvent event) {
        //if (event.getRightClicked().isVisible()) return;
        if (!main.isAngelChestHologram(event.getRightClicked())) return;
        event.setCancelled(true);
    }

}