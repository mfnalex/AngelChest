package de.jeff_media.AngelChest.commands;

import de.jeff_media.AngelChest.Main;
import de.jeff_media.AngelChest.config.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandVersion implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if(!commandSender.hasPermission(Permissions.VERSION)) {
            commandSender.sendMessage(command.getPermissionMessage());
            return true;
        }

        String[] version = new String[] {
                "§6",
                "§6===[§bAngelChest Version§6]===",
                "§eAngelChest" + (Main.getInstance().premium() ? "Plus " : " ") + Main.getInstance().getDescription().getVersion(),
                "§e" + Bukkit.getBukkitVersion(),
                "§e" + Bukkit.getVersion()
        };

        commandSender.sendMessage(version);

        return true;
    }
}
