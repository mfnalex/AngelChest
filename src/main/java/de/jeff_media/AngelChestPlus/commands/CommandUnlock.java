package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.TeleportAction;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

public class CommandUnlock implements CommandExecutor {
	
	final Main main;
	
	public CommandUnlock(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

		if(!(sender instanceof Player)) {
			sender.sendMessage(main.messages.MSG_PLAYERSONLY);
			return true;
		}

		Player p = (Player) sender;

		if(!sender.hasPermission("angelchest.protect")) {
			sender.sendMessage(command.getPermissionMessage());
			return true;
		}

		Triplet<Integer, AngelChest,Player> chestResult = CommandUtils.argIdx2AngelChest(main, p, p, args);
		if(chestResult == null) {
			return true;
		}

		int chestIdStartingAt1 = chestResult.getValue0();
		AngelChest angelChest = chestResult.getValue1();
		Player player = chestResult.getValue2();
		CommandUtils.unlockSingleChest(main,player,player,angelChest);

		return true;
	}
}