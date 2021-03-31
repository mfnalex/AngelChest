package de.jeff_media.AngelChest.commands;

import de.jeff_media.AngelChest.CommandManager;
import de.jeff_media.AngelChest.data.AngelChest;
import de.jeff_media.AngelChest.enums.TeleportAction;
import de.jeff_media.AngelChest.Main;
import de.jeff_media.AngelChest.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CommandFetchOrTeleport implements CommandExecutor {

    final Main main;

    public CommandFetchOrTeleport() {
        this.main = Main.getInstance();
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
            sender.sendMessage(main.messages.MSG_NO_PERMISSION);
            return true;
        }

        Triplet<Integer,AngelChest,Player> chestResult = CommandUtils.argIdx2AngelChest(main, p, p, args);
        if(chestResult == null) {
            return true;
        }

        int chestIdStartingAt1 = chestResult.getValue0();
        AngelChest angelChest = chestResult.getValue1();
        Player player = chestResult.getValue2();
        CommandUtils.fetchOrTeleport(main,player,angelChest,chestIdStartingAt1,action,true);

        return true;
    }
}
