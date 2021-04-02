package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.ConfigUtils;
import de.jeff_media.angelchest.config.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class CommandReload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {

        if (!commandSender.hasPermission(Permissions.RELOAD)) {
            commandSender.sendMessage(Main.getInstance().messages.MSG_NO_PERMISSION);
            return true;
        }

        ConfigUtils.reloadCompleteConfig(true);

        commandSender.sendMessage(ChatColor.GREEN + "AngelChest configuration has been reloaded.");

        return true;
    }
}
