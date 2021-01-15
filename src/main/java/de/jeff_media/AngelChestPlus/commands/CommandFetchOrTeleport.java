package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.TeleportAction;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

public class CommandFetchOrTeleport implements CommandExecutor {

    final Main main;

    public CommandFetchOrTeleport(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {

        TeleportAction action;
        switch(command.getName()) {
            case "actp": action = TeleportAction.TELEPORT_TO_CHEST; break;
            case "acfetch": action = TeleportAction.FETCH_CHEST; break;
            default: return false;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage(main.messages.MSG_PLAYERSONLY);
            return true;
        }

        Player p = (Player) sender;

        if(!sender.hasPermission(action.getPermission())) {
            sender.sendMessage(command.getPermissionMessage());
            return true;
        }

        Triplet<Integer,AngelChest,Player> chestResult = CommandUtils.argIdx2AngelChest(main, p, p, args);
        if(chestResult == null) {
            return true;
        }

        int id = chestResult.getValue0();
        AngelChest angelChest = chestResult.getValue1();
        Player player = chestResult.getValue2();
        CommandUtils.fetchOrTeleport(main,player,angelChest,id,action,true);

        return true;
    }
}
