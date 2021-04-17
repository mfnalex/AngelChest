package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.CommandArgument;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.utils.CommandUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

public final class CommandFetchOrTeleport implements CommandExecutor {

    private final Main main = Main.getInstance();

    @Override
    public boolean onCommand(@NotNull final CommandSender requester, final Command command, @NotNull final String alias, final String[] args) {

        final CommandAction action;
        switch (command.getName()) {
            case "actp":
                action = CommandAction.TELEPORT_TO_CHEST;
                break;
            case "acfetch":
                action = CommandAction.FETCH_CHEST;
                break;
            default:
                return false;
        }

        if (!requester.hasPermission(action.getPermission())) {
            Messages.send(requester,main.messages.MSG_NO_PERMISSION);
            return true;
        }

        final CommandArgument commandArgument = CommandArgument.parse(action, requester, args);
        if (commandArgument == null) {
            return true;
        }

        final Triplet<Integer, AngelChest, OfflinePlayer> chestResult = CommandUtils.argIdx2AngelChest(main, requester, commandArgument.getAffectedPlayer(), commandArgument.getChest());
        if (chestResult == null) {
            return true;
        }

        final int chestIdStartingAt1 = chestResult.getValue0();
        final AngelChest angelChest = chestResult.getValue1();
        CommandUtils.fetchOrTeleport(main, (Player) requester, angelChest, chestIdStartingAt1, action, true);

        return true;
    }
}
