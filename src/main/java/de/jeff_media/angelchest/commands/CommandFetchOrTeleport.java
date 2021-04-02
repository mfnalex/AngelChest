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

public final class CommandFetchOrTeleport implements CommandExecutor {

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
}
