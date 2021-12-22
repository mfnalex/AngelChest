package de.jeff_media.angelchest.debug.tasks;

import de.jeff_media.angelchest.data.Graveyard;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class BlockMarkerTask extends BukkitRunnable {

    private final Graveyard graveyard;
    private final Collection<Block> blocks;
    private final Player player;
    int count = 0;
    boolean show = true;
    private final Material mat;
    private final BlockData visibleData;
    private final BlockData invisibleData;

    public BlockMarkerTask(Graveyard graveyard, Player player) {
        this.player = player;
        this.graveyard = graveyard;
        blocks = graveyard.getFreeSpots();
        mat = graveyard.hasCustomMaterial() ? graveyard.getCustomMaterial().getMaterial() : Material.GOLD_BLOCK;
        visibleData = Bukkit.createBlockData(mat);
        invisibleData = Bukkit.createBlockData(Material.AIR);
    }

    @Override
    public void run() {

        if(count >= 15) {
            cancel();
            for(Block block : blocks) {
                player.sendBlockChange(block.getLocation(),block.getBlockData());
            }
            return;
        }

        if(blocks.isEmpty()) {
            //player.sendMessage(ChatColor.RED + "No free grave found in graveyard " + graveyard.getName());
            cancel();
            return;
        }
        for(Block block : blocks) {
            player.sendBlockChange(block.getLocation(),show ? visibleData : invisibleData);
        }
        count++;
        show = !show;

    }
}
