package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.CommandAction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CommandArgument {

    private final OfflinePlayer affectedPlayer;
    private final String chest;
    private final CommandSender sender;
    @Getter private final Player destination;

    private CommandArgument(final CommandSender sender, final String chest, final OfflinePlayer affectedPlayer, final Player destination) {
        this.sender = sender;
        this.chest = chest;
        this.affectedPlayer = affectedPlayer;
        this.destination = destination;
    }

    public static @Nullable CommandArgument parse(final CommandAction action, final CommandSender requester, final String[] args) {
        OfflinePlayer chestOwner = null;
        String chest = null;
        Player destination = null;

        switch (action) {
            case UNLOCK_CHEST:
            case TELEPORT_TO_CHEST:
            case FETCH_CHEST:
                if (args.length >= 2) {
                    if (args[0].equalsIgnoreCase(requester.getName()) || requester.hasPermission(Permissions.OTHERS)) {
                        chest = args[1];
                        //noinspection deprecation
                        chestOwner = Bukkit.getOfflinePlayer(args[0]);
                        if (chestOwner == null) {
                            Messages.send(requester, String.format(Main.getInstance().messages.MSG_UNKNOWN_PLAYER, args[0]));
                            return null;
                        }
                        if(args.length>=3) {
                            destination = Bukkit.getPlayer(args[2]);
                            if (destination == null) {
                                Messages.send(requester, String.format(Main.getInstance().messages.MSG_UNKNOWN_PLAYER, args[2]));
                                return null;
                            }
                        }
                    } else {
                        Messages.send(requester, Main.getInstance().messages.MSG_NO_PERMISSION);
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
                    if (requester.hasPermission(Permissions.OTHERS)) {
                        //noinspection deprecation
                        chestOwner = Bukkit.getOfflinePlayer(args[0]);
                        if (chestOwner == null) {
                            Messages.send(requester, String.format(Main.getInstance().messages.MSG_UNKNOWN_PLAYER, args[0]));
                            return null;
                        }
                        chest = null;
                    } else {
                        Messages.send(requester, Main.getInstance().messages.MSG_NO_PERMISSION);
                        return null;
                    }
                }

        }
        if (chestOwner == null) {
            if (requester instanceof Player) {
                chestOwner = (Player) requester;
            } else {
                Messages.send(requester, Main.getInstance().messages.MSG_MUST_SPECIFY_PLAYER);
                return null;
            }
        }

        if (destination == null) {
            if (requester instanceof Player) {
                destination = (Player) requester;
            } else {
                Messages.send(requester, Main.getInstance().messages.MSG_MUST_SPECIFY_PLAYER);
                return null;
            }
        }

        final Main main = Main.getInstance();

        if (main.debug) main.debug("===== CommandArgument Parser =====");
        if (main.debug) main.debug("Requester  = " + requester.getName());
        if (main.debug) main.debug("ChestOwner = " + chestOwner.getName());
        if (main.debug) main.debug("Chest      = " + chest);
        if (main.debug) main.debug("Destination= " + destination.getName());

        return new CommandArgument(requester, chest, chestOwner, destination);
    }

    public OfflinePlayer getAffectedPlayer() {
        return affectedPlayer;
    }

    public @Nullable String getChest() {
        return chest;
    }

    public CommandSender getRequester() {
        return sender;
    }

}
