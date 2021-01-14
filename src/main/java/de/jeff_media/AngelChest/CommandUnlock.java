package de.jeff_media.AngelChest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandUnlock implements CommandExecutor {
	
	final Main main;
	
	public CommandUnlock(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
		Player affectedPlayer = null;
		if(!command.getName().equalsIgnoreCase("unlock")) return false;
		
		if(!sender.hasPermission("angelchest.protect")) {
			sender.sendMessage(main.getCommand("unlock").getPermissionMessage());
			return true;
		}

		if(!(sender instanceof Player) && args.length==0) {
			sender.sendMessage(main.messages.MSG_PLAYERSONLY);
			return true;
		}

		if(args.length>1 && sender.hasPermission("angelchest.others")) {

			Player p = Bukkit.getPlayer(args[1]);
			if(p==null) {
				sender.sendMessage(ChatColor.RED+"Could not find player "+args[1]);
				return true;
			}

			affectedPlayer = Bukkit.getPlayer(args[1]);
		}
		
		Player p = (Player) sender;
		if(affectedPlayer==null) affectedPlayer=p;
		
		if(args.length > 0) {
			if(args[0].equals("all")) {
				AngelChestCommandUtils.unlockAllChests(main, p);
				return true;
			}
		}
		
		AngelChest ac = AngelChestCommandUtils.argIdx2AngelChest(main, p, affectedPlayer, args);
		if(ac == null) {
			return true;
		}

		AngelChestCommandUtils.unlockSingleChest(main, p, affectedPlayer, ac);
		return true;
	}
}