package de.jeff_media.AngelChest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {

    final Main main;

    WorldListener(Main main) {
        this.main=main;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        main.loadAllAngelChestsFromFile();
    }
}
