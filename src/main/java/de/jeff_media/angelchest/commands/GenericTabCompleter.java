package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericTabCompleter implements TabCompleter {

    private final Main main;

    public GenericTabCompleter() {
        this.main = Main.getInstance();
    }

    private int getChests(UUID uuid) {
        return main.getAllAngelChestsFromPlayer(Bukkit.getOfflinePlayer(uuid)).size();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        UUID uuid = commandSender instanceof Player ? ((Player)commandSender).getUniqueId() : Main.consoleSenderUUID;
        if(args.length==1) {
            for(int i = 1; i <= getChests(uuid); i++) {
                if(String.valueOf(i).startsWith(args[0])) {
                    list.add(String.valueOf(i));
                }
            }
            if(commandSender.hasPermission(Permissions.OTHERS)) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.getName().startsWith(args[0])) {
                        list.add(player.getName());
                    }
                }
            }
        } else if(args.length==2) {
            if(!commandSender.hasPermission(Permissions.OTHERS)) {
                return null;
            } else {
                OfflinePlayer candidate = Bukkit.getOfflinePlayer(args[0]);
                if(candidate == null) return null;
                uuid = candidate.getUniqueId();
                for(int i = 1; i <= getChests(uuid); i++) {
                    if(String.valueOf(i).startsWith(args[1])) {
                        list.add(String.valueOf(i));
                    }
                }
            }
        } else {
            return null;
        }
        return list;
    }

}
