package de.jeff_media.AngelChest;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandDebug implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        String args = String.join(" ",strings);

        String toSend = PlaceholderAPI.setPlaceholders((Player) commandSender,args);

        commandSender.sendMessage(toSend);

        return true;
    }
}
