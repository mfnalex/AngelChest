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
	
	public CommandUnlock( ) {
		this.main = Main.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender requester, @NotNull Command command, @NotNull String alias, String[] args) {

		/*if(!(requester instanceof Player)) {
			requester.sendMessage(main.messages.MSG_PLAYERSONLY);
			return true;
		}*/

		if(!requester.hasPermission(Permissions.USE) || !requester.hasPermission(Permissions.PROTECT)) {
			requester.sendMessage(main.messages.MSG_NO_PERMISSION);
			return true;
		}

		CommandArgument commandArgument = CommandArgument.parse(CommandAction.UNLOCK_CHEST,requester, args);
		if(commandArgument == null) return true;

		Triplet<Integer, AngelChest, OfflinePlayer> chestResult = CommandUtils.argIdx2AngelChest(main, commandArgument.getRequester(), commandArgument.getAffectedPlayer(), args.length == 0 ? null : args[0]);
		if(chestResult == null) {
			return true;
		}

		//int chestIdStartingAt1 = chestResult.getValue0();
		AngelChest angelChest = chestResult.getValue1();
		OfflinePlayer player = chestResult.getValue2();
		CommandUtils.unlockSingleChest(main,requester,player,angelChest);

		return true;
	}
}