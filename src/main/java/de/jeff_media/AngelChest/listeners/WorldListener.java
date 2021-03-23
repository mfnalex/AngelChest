package de.jeff_media.AngelChest.listeners;

import de.jeff_media.AngelChest.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {

    final Main main;

    // TODO: Is this needed? It's currently not used

    public WorldListener() {
        this.main=Main.getInstance();
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onWorldLoad(@SuppressWarnings("unused") WorldLoadEvent e) {
        main.loadAllAngelChestsFromFile();
    }

    /*@EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {

    }*/

    /*@EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        Chunk chunk = e.getChunk();
        for(Entity entity : chunk.getEntities()) {
            if(!(entity instanceof ArmorStand)) {
                continue;
            }

            //ArmorStand armorStand = (ArmorStand) entity;
        }
    }*/
}
