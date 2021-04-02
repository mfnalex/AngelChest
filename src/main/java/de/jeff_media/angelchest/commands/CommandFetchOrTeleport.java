package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.TeleportAction;
import de.jeff_media.angelchest.utils.CommandUtils;
import org.bukkit.Bukkit;
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

        TeleportAction action;
        switch(command.getName()) {
            case "actp": action = TeleportAction.TELEPORT_TO_CHEST; break;
            case "acfetch": action = TeleportAction.FETCH_CHEST; break;
            default: return false;
        }

        String chestOwnerName = null;
        Player chestOwner = null;
        String chest = null;

        if(args.length>=2) {
            if(args[0].equalsIgnoreCase(requester.getName()) || requester.hasPermission(Permissions.OTHERS)) {
                chest = args[1];
                chestOwner = Bukkit.getPlayer(args[0]);
                if (chestOwner == null) {
                    requester.sendMessage(String.format(main.messages.MSG_UNKNOWN_PLAYER, args[0]));
                    return true;
                }
            } else {
                requester.sendMessage(main.messages.MSG_NO_PERMISSION);
                return true;
            }
        } else if(args.length==1) {
            chest = args[0];
        } else {
            chest = null;
        }

        if(chestOwner==null) {
            if(requester instanceof Player) {
                chestOwner = (Player) requester;
            } else {
                requester.sendMessage(main.messages.MSG_MUST_SPECIFY_PLAYER);
                return true;
            }
        }

        if(!requester.hasPermission(action.getPermission())) {
            requester.sendMessage(main.messages.MSG_NO_PERMISSION);
            return true;
        }

        System.out.println("Chest = " + chest);
        System.out.println("Sender = " + requester);
        System.out.println("Owner = " + chestOwner);

        Triplet<Integer, AngelChest,Player> chestResult = CommandUtils.argIdx2AngelChest(main, requester, chestOwner, chest);
        if(chestResult == null) {
            return true;
        }

        int chestIdStartingAt1 = chestResult.getValue0();
        AngelChest angelChest = chestResult.getValue1();
        //Player player = chestResult.getValue2();
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
                Player candidate = Bukkit.getPlayer(args[0]);
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
