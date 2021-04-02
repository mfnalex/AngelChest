package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.CommandArgument;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CommandFetchOrTeleport implements CommandExecutor, TabCompleter {

    final Main main;

    public CommandFetchOrTeleport() {
        this.main = Main.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender requester, Command command, @NotNull String alias, String[] args) {

        CommandAction action;
        switch(command.getName()) {
            case "actp": action = CommandAction.TELEPORT_TO_CHEST; break;
            case "acfetch": action = CommandAction.FETCH_CHEST; break;
            default: return false;
        }

        if(!requester.hasPermission(action.getPermission())) {
            requester.sendMessage(main.messages.MSG_NO_PERMISSION);
            return true;
        }

        CommandArgument commandArgument = CommandArgument.parse(action,requester, args);
        if(commandArgument==null) {
            return true;
        }

        Triplet<Integer, AngelChest,OfflinePlayer> chestResult = CommandUtils.argIdx2AngelChest(main, requester, commandArgument.getAffectedPlayer(), commandArgument.getChest());
        if(chestResult == null) {
            return true;
        }

        int chestIdStartingAt1 = chestResult.getValue0();
        AngelChest angelChest = chestResult.getValue1();
        CommandUtils.fetchOrTeleport(main,(Player) requester,angelChest,chestIdStartingAt1,action,true);

        return true;
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
