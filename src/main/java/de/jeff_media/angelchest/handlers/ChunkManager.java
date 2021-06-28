package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.Main;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.HashSet;

public class ChunkManager {

    private static final HashSet<Chunk> CHUNKS = new HashSet<>();
    private static final Main main = Main.getInstance();

    public static void keepLoaded(Block block) {
        if(CHUNKS.contains(block.getChunk())) return;
        CHUNKS.add(block.getChunk());
        block.getChunk().addPluginChunkTicket(main);
    }

    public static void reset() {
        for(Chunk chunk : CHUNKS) {
            chunk.removePluginChunkTicket(main);
        }
    }

}
