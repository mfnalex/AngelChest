package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.Main;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderInitListener implements Listener {

    private static final Main main = Main.getInstance();

    @EventHandler
    public void onInit(ItemsAdderLoadDataEvent event) {
        main.setItemsAdderLoaded(true);
    }

}
