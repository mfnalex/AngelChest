package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.utils.AngelChestUtils;
import de.jeff_media.angelchest.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandList implements CommandExecutor {
	
	final Main main;
	
	public CommandList() {
		this.main = Main.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender requester, Command command, @NotNull String alias, String[] args) {

		Player affectedPlayer = null;


		if(!command.getName().equalsIgnoreCase("aclist")) return false;
		
		if(!requester.hasPermission(Permissions.ALLOW_USE)) {
			requester.sendMessage(main.messages.MSG_NO_PERMISSION);
			return true;
		}

		if(!(requester instanceof Player) && args.length==0) {
			requester.sendMessage(main.messages.MSG_MUST_SPECIFY_PLAYER);
			return true;
		}

		if(args.length>0 ) {
			if (requester.hasPermission(Permissions.OTHERS)) {
				Player p = Bukkit.getPlayer(args[0]);
				if (p == null) {
					requester.sendMessage(String.format(main.messages.MSG_UNKNOWN_PLAYER, args[0]));
					return true;
				}
				requester.sendMessage(String.format(main.messages.MSG_SU, p.getName()));
				affectedPlayer = Bukkit.getPlayer(args[0]);
			} else {
				if(!args[0].equalsIgnoreCase(requester.getName())) {
					requester.sendMessage(main.messages.MSG_NO_PERMISSION);
					return true;
				}
			}
		}

		if(affectedPlayer==null) affectedPlayer=(Player) requester;
		
		// Only send this message if the player has chests
		if(!AngelChestUtils.getAllAngelChestsFromPlayer(affectedPlayer).isEmpty()) {
			requester.sendMessage(main.messages.MSG_ANGELCHEST_LOCATION);
		}

		CommandUtils.sendListOfAngelChests(main, requester, affectedPlayer);

		return true;
	}
}