package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.CommandAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CommandArgument {

    private final OfflinePlayer chestOwner;
    private final String chest;
    private final CommandSender sender;
    private final Player toTeleport;

    private CommandArgument(final CommandSender sender, final String chest, final OfflinePlayer chestOwner, Player toTeleport) {
        this.sender =sender;
        this.chest = chest;
        this.chestOwner = chestOwner;
        if(toTeleport == null && sender instanceof Player) {
            toTeleport = (Player) sender;
        }
        this.toTeleport = toTeleport;
    }

    public static @Nullable CommandArgument parse(final CommandAction action, final CommandSender requester, final String[] args) {
        OfflinePlayer chestOwner = null;
        String chest = null;
        Player toTeleport = null;

        String syntax = "";
        if(action == CommandAction.UNLOCK_CHEST) {
            syntax = action.getCommand() + " <chestOwner> <chestId>";
        }
        if(action == CommandAction.FETCH_CHEST || action == CommandAction.TELEPORT_TO_CHEST) {
            syntax = action.getCommand() + " <chestOwner> <chestId> [targetPlayer]";
        }
        if(action == CommandAction.LIST_CHESTS) {
            syntax = action.getCommand() + " <chestOwner>";
        }

        if(!syntax.isEmpty()) {
            syntax = ChatColor.RESET.toString() + ChatColor.GRAY + " (" + syntax + ")";
        }

        switch (action) {
            case UNLOCK_CHEST:
            case TELEPORT_TO_CHEST:
            case FETCH_CHEST:
                if (args.length >= 2) {
                    if (args[0].equalsIgnoreCase(requester.getName()) || requester.hasPermission(Permissions.OTHERS)) {
                        chest = args[1];
                        //noinspection deprecation
                        chestOwner = Bukkit.getOfflinePlayer(args[0]);

                        if(requester instanceof Player) {
                            toTeleport = (Player) requester;
                        }

                        if(args.length >= 3 && requester.hasPermission(Permissions.OTHERS)) {
                            toTeleport = Bukkit.getPlayer(args[2]);

                            if (toTeleport == null || !toTeleport.isOnline()) {
                                Messages.send(requester, String.format(AngelChestMain.getInstance().messages.MSG_UNKNOWN_PLAYER, args[2]));
                                return null;
                            }
                        } else {
                            if(toTeleport == null) {
                                if (chestOwner.isOnline()) {
                                    toTeleport = (Player) chestOwner;
                                } else {
                                    Messages.send(requester, AngelChestMain.getInstance().messages.MSG_MUST_SPECIFY_PLAYER + syntax);
                                    return null;
                                }
                            }
                        }

                        if (chestOwner == null) {
                            Messages.send(requester, String.format(AngelChestMain.getInstance().messages.MSG_UNKNOWN_PLAYER, args[0]));
                            return null;
                        }

                    } else {
                        Messages.send(requester, AngelChestMain.getInstance().messages.MSG_NO_PERMISSION);
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
                            Messages.send(requester, String.format(AngelChestMain.getInstance().messages.MSG_UNKNOWN_PLAYER, args[0]));
                            return null;
                        }
                        chest = null;
                    } else {
                        Messages.send(requester, AngelChestMain.getInstance().messages.MSG_NO_PERMISSION);
                        return null;
                    }
                }

        }
        if (chestOwner == null) {
            if (requester instanceof Player) {
                chestOwner = (Player) requester;
            } else {
                Messages.send(requester, AngelChestMain.getInstance().messages.MSG_MUST_SPECIFY_PLAYER);
                return null;
            }
        }

        final AngelChestMain main = AngelChestMain.getInstance();
        if (main.debug) main.debug("===== CommandArgument Parser =====");
        if (main.debug) main.debug("Requester  = " + requester.getName());
        if (main.debug) main.debug("ChestOwner = " + chestOwner.getName());
        if (main.debug) main.debug("Chest      = " + chest);

        return new CommandArgument(requester, chest, chestOwner, toTeleport);
    }

    public OfflinePlayer getChestOwner() {
        return chestOwner;
    }

    public @Nullable String getChest() {
        return chest;
    }

    public CommandSender getRequester() {
        return sender;
    }

    public @Nullable Player getToTeleport() {
        return toTeleport;
    }

}
