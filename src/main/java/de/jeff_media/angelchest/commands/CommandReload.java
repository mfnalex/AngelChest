package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.ConfigUtils;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import com.jeff_media.jefflib.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Handles /acreload
 */
public final class CommandReload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {

        if (!commandSender.hasPermission(Permissions.RELOAD)) {
            Messages.send(commandSender, Main.getInstance().messages.MSG_NO_PERMISSION);
            return true;
        }
        Messages.send(commandSender, ChatColor.GRAY + "Reloading AngelChest configuration...");
        TimeUtils.startTimings("configreload");
        ConfigUtils.reloadCompleteConfig(true);
        long duration = TimeUtils.endTimings("configreload", false);
        Messages.send(commandSender, ChatColor.GREEN + "AngelChest configuration has been reloaded in §6" + TimeUtils.formatNanoseconds(duration) + "§a.");

        return true;
    }
}
