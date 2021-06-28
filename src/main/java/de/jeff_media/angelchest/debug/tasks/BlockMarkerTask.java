package de.jeff_media.angelchest.debug.tasks;

import de.jeff_media.angelchest.data.Graveyard;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class BlockMarkerTask extends BukkitRunnable {

    private final Graveyard graveyard;
    private final Collection<Block> blocks;
    private final Player player;
    int count = 0;

    public BlockMarkerTask(Graveyard graveyard, Player player) {
        this.player = player;
        this.graveyard = graveyard;
        blocks = graveyard.getFreeSpots();
    }

    @Override
    public void run() {
        if(blocks.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No free grave found in graveyard " + graveyard.getName());
            cancel();
            return;
        }
        for(Block block : blocks) {
            Location loc = block.getLocation().add(0.5,0.5,0.5);
            player.spawnParticle(Particle.BARRIER, loc, 1);
        }
        count++;
        if(count > 5) cancel();
    }
}
