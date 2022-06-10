package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.CommandArgument;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.utils.AngelChestUtils;
import de.jeff_media.angelchest.utils.CommandUtils;
import de.jeff_media.daddy.Chicken;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Handles /aclist
 */
public final class CommandList implements CommandExecutor {

    final Main main;

    public CommandList() {
        this.main = Main.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender requester, final Command command, @NotNull final String alias, final String[] args) {

        if (!command.getName().equalsIgnoreCase("aclist")) return false;

        if (!requester.hasPermission(Permissions.USE)) {
            Messages.send(requester, main.messages.MSG_NO_PERMISSION);
            return true;
        }

        final CommandArgument commandArgument = CommandArgument.parse(CommandAction.LIST_CHESTS, requester, args);
        if (commandArgument == null) return true;

        // Only send this message if the player has chests
        if (!AngelChestUtils.getAllAngelChestsFromPlayer(commandArgument.getAffectedPlayer()).isEmpty()) {
            Messages.send(requester, main.messages.MSG_ANGELCHEST_LOCATION);
        }

        CommandUtils.sendListOfAngelChests(main, requester, commandArgument.getAffectedPlayer());

        return true;
    }
}