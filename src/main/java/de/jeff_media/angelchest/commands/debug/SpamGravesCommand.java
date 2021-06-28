package de.jeff_media.angelchest.commands.debug;

import de.jeff_media.angelchest.Main;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SpamGravesCommand {

    public static void run(Player player, String[] args) {
        int maxCount;
        try {
            maxCount = Integer.parseInt(args[2]);
        } catch (Exception exception) {
            player.sendMessage("§c" + args[1] + " is not a valid integer.");
            return;
        }
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if(count == maxCount) {
                    cancel();
                    return;
                }
                if(player.isDead()) {
                    player.spigot().respawn();
                    count++;
                } else {
                    player.getInventory().addItem(new ItemStack(Material.DIRT));
                    player.setHealth(0);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

}
