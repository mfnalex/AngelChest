package de.jeff_media.AngelChest.commands;

import de.jeff_media.AngelChest.config.ConfigUtils;
import de.jeff_media.AngelChest.config.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class CommandReload implements CommandExecutor  {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(!commandSender.hasPermission(Permissions.ALLOW_RELOAD)) {
            commandSender.sendMessage(command.getPermissionMessage());
            return true;
        }

        ConfigUtils.reloadCompleteConfig(true);

        commandSender.sendMessage(ChatColor.GREEN+"AngelChest configuration has been reloaded.");

        return true;
    }
}
