package de.jeff_media.AngelChest.listeners;

import de.jeff_media.AngelChest.Main;
import org.bukkit.event.Listener;

public class WorldListener implements Listener {

    final Main main;

    public WorldListener() {
        this.main=Main.getInstance();
    }


    /*@SuppressWarnings("unused")
    @EventHandler
    public void onWorldLoad(@SuppressWarnings("unused") WorldLoadEvent e) {
        main.loadAllAngelChestsFromFile();
    }*/

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
