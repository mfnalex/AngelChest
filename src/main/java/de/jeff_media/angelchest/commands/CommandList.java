package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.utils.CommandUtils;
import de.jeff_media.angelchest.utils.Utils;
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
	public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {

		Player affectedPlayer = null;


		if(!command.getName().equalsIgnoreCase("aclist")) return false;
		
		if(!sender.hasPermission(Permissions.ALLOW_USE)) {
			sender.sendMessage(main.messages.MSG_NO_PERMISSION);
			return true;
		}

		if(!(sender instanceof Player) && args.length==0) {
			sender.sendMessage(main.messages.MSG_PLAYERSONLY);
			return true;
		}

		if(args.length>0 && sender.hasPermission("angelchest.others")) {

			Player p = Bukkit.getPlayer(args[0]);
			if(p==null) {
				sender.sendMessage(ChatColor.RED+"Could not find player "+args[0]);
				return true;
			}

			affectedPlayer = Bukkit.getPlayer(args[0]);
		}

		assert sender instanceof Player;
		Player p = (Player) sender;
		if(affectedPlayer==null) affectedPlayer=p;
		
		// Only send this message if the player has chests
		if(!Utils.getAllAngelChestsFromPlayer(p).isEmpty()) {
			p.sendMessage(main.messages.MSG_ANGELCHEST_LOCATION);
		}

		CommandUtils.sendListOfAngelChests(main, p, affectedPlayer);

		return true;
	}
}