package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.CommandArgument;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.utils.CommandUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

public final class CommandUnlock implements CommandExecutor {

    final Main main;

    public CommandUnlock() {
        this.main = Main.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender requester, @NotNull final Command command, @NotNull final String alias, final String[] args) {

		/*if(!(requester instanceof Player)) {
			requester.sendMessage(main.messages.MSG_PLAYERSONLY);
			return true;
		}*/

        if (!requester.hasPermission(Permissions.USE) || !requester.hasPermission(Permissions.PROTECT)) {
            requester.sendMessage(main.messages.MSG_NO_PERMISSION);
            return true;
        }

        final CommandArgument commandArgument = CommandArgument.parse(CommandAction.UNLOCK_CHEST, requester, args);
        if (commandArgument == null) return true;

        final Triplet<Integer, AngelChest, OfflinePlayer> chestResult = CommandUtils.argIdx2AngelChest(main, commandArgument.getRequester(), commandArgument.getAffectedPlayer(), commandArgument.getChest());
        if (chestResult == null) {
            return true;
        }

        //int chestIdStartingAt1 = chestResult.getValue0();
        final AngelChest angelChest = chestResult.getValue1();
        //OfflinePlayer player = chestResult.getValue2();
        CommandUtils.unlockSingleChest(main, requester, angelChest);

        return true;
    }
}