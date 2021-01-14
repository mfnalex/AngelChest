package de.jeff_media.AngelChest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandReload implements CommandExecutor  {

    final Main main;

    CommandReload(Main main) {
        this.main=main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(!commandSender.hasPermission("angelchest.reload")) {
            commandSender.sendMessage(command.getPermissionMessage());
            return true;
        }

        ConfigUtils.reloadCompleteConfig(main,true);

        commandSender.sendMessage(ChatColor.GREEN+"AngelChest configuration has been reloaded.");

        return true;
    }
}
