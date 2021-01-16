package de.jeff_media.AngelChestPlus.listeners;

import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.Chunk;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
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

    /*@EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {

    }*/

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        Chunk chunk = e.getChunk();
        for(Entity entity : chunk.getEntities()) {
            if(!(entity instanceof ArmorStand)) {
                continue;
            }

            ArmorStand armorStand = (ArmorStand) entity;
        }


    }
}
