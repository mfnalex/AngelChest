package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.CommandAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CommandArgument {

    private CommandSender sender;
    private String chest;
    private OfflinePlayer affectedPlayer;

    private CommandArgument(CommandSender sender, String chest, OfflinePlayer affectedPlayer) {
        this.sender = sender;
        this.chest = chest;
        this.affectedPlayer = affectedPlayer;
    }

    public static @Nullable CommandArgument parse(CommandAction action, CommandSender requester, String[] args) {
        OfflinePlayer chestOwner = null;
        String chest = null;

        switch (action) {
            case UNLOCK_CHEST:
            case TELEPORT_TO_CHEST:
            case FETCH_CHEST:
                if (args.length >= 2) {
                    if (args[0].equalsIgnoreCase(requester.getName()) || requester.hasPermission(Permissions.OTHERS)) {
                        chest = args[1];
                        chestOwner = Bukkit.getOfflinePlayer(args[0]);
                        if (chestOwner == null) {
                            requester.sendMessage(String.format(Main.getInstance().messages.MSG_UNKNOWN_PLAYER, args[0]));
                            return null;
                        }
                    } else {
                        requester.sendMessage(Main.getInstance().messages.MSG_NO_PERMISSION);
                        return null;
                    }
                } else if (args.length == 1) {
                    chest = args[0];
                } else {
                    chest = null;
                }
                break;
            case LIST_CHESTS:
            default:
                if (args.length == 1) {
                    if(requester.hasPermission(Permissions.OTHERS)) {
                        chestOwner = Bukkit.getOfflinePlayer(args[0]);
                        if (chestOwner == null) {
                            requester.sendMessage(String.format(Main.getInstance().messages.MSG_UNKNOWN_PLAYER, args[0]));
                            return null;
                        }
                        chest = null;
                    } else {
                        requester.sendMessage(Main.getInstance().messages.MSG_NO_PERMISSION);
                        return null;
                    }
                }

        }
        if (chestOwner == null) {
            if (requester instanceof Player) {
                chestOwner = (Player) requester;
            } else {
                requester.sendMessage(Main.getInstance().messages.MSG_MUST_SPECIFY_PLAYER);
                return null;
            }
        }

        System.out.println("Chest     = " + chest);
        System.out.println("Requester = " + requester);
        System.out.println("Player    = " + chestOwner);

        return new CommandArgument(requester, chest, chestOwner);
    }

    public CommandSender getRequester() {
        return sender;
    }

    public @Nullable String getChest() {
        return chest;
    }

    public OfflinePlayer getAffectedPlayer() {
        return affectedPlayer;
    }

}
