package de.jeff_media.AngelChest;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class PistonListener implements Listener {

    Main main;

    PistonListener(Main main) {
        this.main=main;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtent(BlockPistonExtendEvent e) {
        List<Block> affectedBlocks = e.getBlocks();

        if(affectedBlocks==null) {
            return;
        }

        for(Block block : affectedBlocks) {
            if(main.isAngelChest(block) || main.isAngelChest(block.getRelative(BlockFace.UP))) {
                e.setCancelled(true);
                main.debug("BlockPistonExtendEvent cancelled because AngelChest is affected");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        List<Block> affectedBlocks = e.getBlocks();

        if(affectedBlocks==null) {
            return;
        }

        for(Block block : affectedBlocks) {
            if(main.isAngelChest(block) || main.isAngelChest(block.getRelative(BlockFace.UP))) {
                e.setCancelled(true);
                main.debug("BlockPistonRetractEvent cancelled because AngelChest is affected");
                return;
            }
        }
    }
}
