package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.config.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandReload implements CommandExecutor  {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(!commandSender.hasPermission("angelchest.reload")) {
            commandSender.sendMessage(command.getPermissionMessage());
            return true;
        }

        ConfigUtils.reloadCompleteConfig(true);

        commandSender.sendMessage(ChatColor.GREEN+"AngelChest configuration has been reloaded.");

        return true;
    }
}
