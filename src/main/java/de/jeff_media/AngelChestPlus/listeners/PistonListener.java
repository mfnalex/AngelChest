package de.jeff_media.AngelChestPlus.listeners;

import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

/**
 * Handles piston related events that could affect AngelChests
 */
public class PistonListener implements Listener {

    final Main main;

    public PistonListener(Main main) {
        this.main=main;
    }

    /**
     * Prevents pistons from moving or destroying AngelChest blocks
     * @param event BlockPistonExtendEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtent(BlockPistonExtendEvent event) {

        Block block1 = event.getBlock();
        if (main.isAngelChest(block1)) {
            event.setCancelled(true);
            return;
        }

        List<Block> affectedBlocks = event.getBlocks();

        if(affectedBlocks==null) {
            return;
        }

        for(Block block : affectedBlocks) {
            if(main.isAngelChest(block) || main.isAngelChest(block.getRelative(BlockFace.UP))) {
                event.setCancelled(true);
                main.debug("BlockPistonExtendEvent cancelled because AngelChest is affected");
                return;
            }
        }
    }

    /**
     * Prevents pistons from moving or destroying AngelChest blocks
     * @param event BlockPistonRetractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {

        Block block1 = event.getBlock();
        if (main.isAngelChest(block1)) {
            event.setCancelled(true);
            return;
        }

        List<Block> affectedBlocks = event.getBlocks();

        if(affectedBlocks==null) {
            return;
        }

        for(Block block : affectedBlocks) {
            if(main.isAngelChest(block) || main.isAngelChest(block.getRelative(BlockFace.UP))) {
                event.setCancelled(true);
                main.debug("BlockPistonRetractEvent cancelled because AngelChest is affected");
                return;
            }
        }
    }
}
