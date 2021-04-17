package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.Main;
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
public final class PistonListener implements Listener {

    private final Main main = Main.getInstance();

    /**
     * Prevents pistons from moving or destroying AngelChest blocks
     *
     * @param event BlockPistonExtendEvent
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtent(final BlockPistonExtendEvent event) {

        final Block block1 = event.getBlock();
        if (main.isAngelChest(block1)) {
            event.setCancelled(true);
            return;
        }

        final List<Block> affectedBlocks = event.getBlocks();

        if (affectedBlocks == null) {
            return;
        }

        for (final Block block : affectedBlocks) {
            if (main.isAngelChest(block) || main.isAngelChest(block.getRelative(BlockFace.UP))) {
                event.setCancelled(true);
                main.debug("BlockPistonExtendEvent cancelled because AngelChest is affected");
                return;
            }
        }
    }

    /**
     * Prevents pistons from moving or destroying AngelChest blocks
     *
     * @param event BlockPistonRetractEvent
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event) {

        final Block block1 = event.getBlock();
        if (main.isAngelChest(block1)) {
            event.setCancelled(true);
            return;
        }

        final List<Block> affectedBlocks = event.getBlocks();

        if (affectedBlocks == null) {
            return;
        }

        for (final Block block : affectedBlocks) {
            if (main.isAngelChest(block) || main.isAngelChest(block.getRelative(BlockFace.UP))) {
                event.setCancelled(true);
                main.debug("BlockPistonRetractEvent cancelled because AngelChest is affected");
                return;
            }
        }
    }
}
