package de.jeff_media.AngelChest.commands;

import de.jeff_media.AngelChest.config.Permissions;
import de.jeff_media.AngelChest.data.AngelChest;
import de.jeff_media.AngelChest.Main;
import de.jeff_media.AngelChest.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

public final class CommandUnlock implements CommandExecutor {
	
	final Main main;
	
	public CommandUnlock( ) {
		this.main = Main.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

		if(!(sender instanceof Player)) {
			sender.sendMessage(main.messages.MSG_PLAYERSONLY);
			return true;
		}

		Player p = (Player) sender;

		if(!sender.hasPermission(Permissions.ALLOW_USE) || !sender.hasPermission(Permissions.ALLOW_PROTECT)) {
			sender.sendMessage(main.messages.MSG_NO_PERMISSION);
			return true;
		}

		Triplet<Integer, AngelChest,Player> chestResult = CommandUtils.argIdx2AngelChest(main, p, p, args);
		if(chestResult == null) {
			return true;
		}

		//int chestIdStartingAt1 = chestResult.getValue0();
		AngelChest angelChest = chestResult.getValue1();
		Player player = chestResult.getValue2();
		CommandUtils.unlockSingleChest(main,player,player,angelChest);

		return true;
	}
}