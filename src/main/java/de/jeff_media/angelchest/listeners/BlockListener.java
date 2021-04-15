package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.io.File;

/**
 * Listens to block related events, e.g. messing with the actual Block where an AngelChest is located
 */
public final class BlockListener implements Listener {

    final Main main;

    public BlockListener() {
        this.main = Main.getInstance();
    }

    /**
     * Called when an AngelChest's block is being broken.
     * Handles protecting the chest, or dropping it's content if it was not protected or if the user
     * was allowed to break the chest.
     *
     * @param event BlockBreakEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!main.isAngelChest(event.getBlock())) return;
        final AngelChest angelChest = main.getAngelChest(event.getBlock());
        assert angelChest != null;
        if (!angelChest.owner.equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission(Permissions.PROTECT_IGNORE) && angelChest.isProtected) {
            Messages.send(event.getPlayer(),main.messages.MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS);
            event.setCancelled(true);
            return;
        }
        if (!angelChest.hasPaidForOpening(event.getPlayer())) {
            return;
        }
        angelChest.destroy(false);
        angelChest.remove();
    }

    /**
     * Prevent all block explosions from destroying AngelChest blocks
     *
     * @param event BlockExplodeEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockExplode(final BlockExplodeEvent event) {
        event.blockList().removeIf(main::isAngelChest);
    }

    /**
     * Prevents the AngelChest block from being broken by breaking the block below it
     *
     * @param event BlockBreakEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBreakingBlockThatThisIsAttachedTo(final BlockBreakEvent event) {
        if (!main.isAngelChest(event.getBlock().getRelative(BlockFace.UP))) return;
        if (event.getBlock().getRelative(BlockFace.UP).getPistonMoveReaction() != PistonMoveReaction.BREAK) return;

        event.setCancelled(true);
        main.debug("Preventing BlockBreakEvent because it interferes with AngelChest.");

    }

    /**
     * Called when a bucket is emptied inside the block of an AngelChest
     *
     * @param event PlayerBucketEmptyEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (main.isAngelChest(event.getBlock())) {
            event.setCancelled(true);
        } else {
            return;
        }

        // The client thinks the player was removed anyway, so it will show up as a "regular" head.
        // Gotta reload the AngelChest to fix this
        final AngelChest ac = main.getAngelChest(event.getBlock());
        if (ac == null) return;
        final File file = ac.saveToFile(true);
        main.angelChests.put(event.getBlock(), new AngelChest(file));
    }

    /**
     * Prevents all entity explosions from destroying AngelChest blocks
     *
     * @param event EntityExplodeEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        event.blockList().removeIf(main::isAngelChest);
    }

    /**
     * Prevent liquids and dragon eggs from destroying AngelChest blocks
     *
     * @param event BlockFromToEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onLiquidDestroysChest(final BlockFromToEvent event) {
        // Despite the name, this event only fires when liquid or a teleporting dragon egg changes a block
        if (main.isAngelChest(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

}