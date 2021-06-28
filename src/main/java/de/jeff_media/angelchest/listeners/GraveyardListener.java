package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.angelchest.handlers.GraveyardManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class GraveyardListener implements Listener {

    private static final int CHECK_RADIUS = 3;
    private final Main main = Main.getInstance();

    private void update(Block block) {
        if(!GraveyardManager.hasGraveyard(block.getWorld())) return;
        for(Graveyard graveyard : GraveyardManager.getGraveyards(block.getWorld())) {
            if(main.debug) {
                main.debug("Block changed within Graveyard " + graveyard.getName() + ", updating cached grave locations...");
            }
            if(graveyard.getWorldBoundingBox().contains(block)) {
                for(int x = -CHECK_RADIUS; x <= CHECK_RADIUS; x++) {
                    for(int y = -CHECK_RADIUS; y <= CHECK_RADIUS; y++) {
                        for(int z = -CHECK_RADIUS; z <= CHECK_RADIUS; z++) {
                            Block candidate = block.getRelative(x,y,z);
                            if(!graveyard.getWorldBoundingBox().contains(block)) continue;
                            if(graveyard.isValidSpawnOn(candidate)) {
                                graveyard.getCachedValidGraveLocations().add(candidate);
                            } else {
                                graveyard.getCachedValidGraveLocations().remove(candidate);
                            }
                        }
                    }
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        update(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        update(event.getBlock());
    }

}
