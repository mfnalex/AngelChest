package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.AngelChestCommandUtils;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandList implements CommandExecutor {
	
	final Main main;
	
	public CommandList(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {

		Player affectedPlayer = null;


		if(!command.getName().equalsIgnoreCase("aclist")) return false;
		
		if(!sender.hasPermission("angelchest.use")) {
			sender.sendMessage(main.getCommand("aclist").getPermissionMessage());
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
		
		Player p = (Player) sender;
		if(affectedPlayer==null) affectedPlayer=p;
		
		// Only send this message if the player has chests
		if(!Utils.getAllAngelChestsFromPlayer(p, main).isEmpty()) {
			p.sendMessage(main.messages.MSG_ANGELCHEST_LOCATION);
		}

		AngelChestCommandUtils.sendListOfAngelChests(main, p, affectedPlayer);

		return true;
	}
}