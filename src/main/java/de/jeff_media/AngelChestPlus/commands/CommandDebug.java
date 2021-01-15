package de.jeff_media.AngelChestPlus.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandDebug implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        for(Entity entity : ((Player) (commandSender)).getNearbyEntities(20,20,20)) {
            if(entity instanceof ArmorStand) {
                commandSender.sendMessage(entity.getUniqueId().toString()+": "+entity.getCustomName());
            }
        }

        return true;
    }
}
