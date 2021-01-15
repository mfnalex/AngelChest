package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.jeff_media.AngelChestPlus.Utils.findSafeBlock;
import static de.jeff_media.AngelChestPlus.Utils.getCardinalDirection;

public class CommandFetch implements CommandExecutor {

    final Main main;

    public CommandFetch(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {

        Player affectedPlayer = null;

        if (!command.getName().equalsIgnoreCase("acfetch")) return false;

        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(main.messages.MSG_PLAYERSONLY);
            return true;
        }

        if (args.length > 1 && sender.hasPermission("angelchest.others")) {

            Player p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + "Could not find player " + args[1]);
                return true;
            }

            affectedPlayer = Bukkit.getPlayer(args[1]);
        }

        Player sendTo = (Player) sender;
        if (affectedPlayer == null) affectedPlayer = sendTo;

        if (!sender.hasPermission("angelchest.fetch")) {
            sender.sendMessage(main.getCommand("acfetch").getPermissionMessage());
            return true;
        }

        AngelChest ac = AngelChestCommandUtils.argIdx2AngelChest(main, sendTo, affectedPlayer, args);
        if (ac == null) {
            return true;
        }

        double price = main.getConfig().getDouble(Config.PRICE_FETCH);
        if (price > 0) {
            PendingConfirm confirm = main.pendingConfirms.get(((Player) sender).getUniqueId());
            if (confirm == null || !confirm.chest.equals(ac) || confirm.action != PendingConfirm.Action.Fetch) {
                if (confirm == null) {
                    System.out.println("confirm == null");
                } else if (!confirm.chest.equals(ac)) {
                    System.out.println("!= ac");
                } else if (confirm.action != PendingConfirm.Action.Fetch) {
                    System.out.println("!= action");
                }
                main.pendingConfirms.put(((Player) sender).getUniqueId(), new PendingConfirm(ac, PendingConfirm.Action.Fetch));
                String confirmCommand = "/acfetch " + String.join(" ", args);
                AngelChestCommandUtils.sendConfirmMessage(sender, confirmCommand, price, main.messages.MSG_CONFIRM, main);
                return true;
            }
            main.pendingConfirms.remove(((Player) sender).getUniqueId());
        }

        if (price > 0 && !AngelChestCommandUtils.hasEnoughMoney(sendTo, price, main, main.messages.MSG_NOT_ENOUGH_MONEY,"AngelChest Fetch")) {
            return true;
        }

        Location newLoc = sendTo.getLocation();
        String dir = getCardinalDirection(sendTo);
        BlockFace facing;

        // Set the relative direction of the block and offset the new chest location
        switch (dir) {
            case "N":
                newLoc.add(0, 0, -2);
                facing = BlockFace.SOUTH;
                break;
            case "NE":
                newLoc.add(2, 0, -2);
                facing = BlockFace.SOUTH;
                break;
            case "E":
                newLoc.add(2, 0, 0);
                facing = BlockFace.WEST;
                break;
            case "SE":
                newLoc.add(2, 0, 2);
                facing = BlockFace.WEST;
                break;
            case "S":
                newLoc.add(0, 0, 2);
                facing = BlockFace.NORTH;
                break;
            case "SW":
                newLoc.add(-2, 0, 2);
                facing = BlockFace.NORTH;
                break;
            case "W":
                newLoc.add(-2, 0, 0);
                facing = BlockFace.EAST;
                break;
            case "NW":
                newLoc.add(-2, 0, -2);
                facing = BlockFace.EAST;
                break;
            default:
                main.getLogger().info("Unable to get block facing direction");
                facing = BlockFace.NORTH;
        }

        Block newBlock = findSafeBlock(newLoc.getBlock(), main);
        Block oldBlock = ac.block;

        // Move the block in game
        ac.destroyChest(oldBlock);
        ac.createChest(newBlock, sendTo.getUniqueId());

        // Make the chest face the player
        try {
            AngelChestBlockDataUtils.setBlockDirection(newBlock, facing);
        } catch (Throwable throwable) {
            // NoClassDefFoundError in <1.13
        }

        // Swap the block in code
        main.angelChests.put(newBlock, main.angelChests.remove(oldBlock));
        main.angelChests.get(newBlock).block = newBlock;

        sendTo.sendMessage(main.messages.MSG_RETRIEVED);

        return true;
    }

}
