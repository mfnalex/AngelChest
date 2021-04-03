package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.PendingConfirm;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.enums.EconomyStatus;
import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class CommandUtils {

    public static void payMoney(final OfflinePlayer p, final double money, final String reason) {

        final Main main = Main.getInstance();

        if (money <= 0) {
            return;
        }

        if (main.economyStatus == EconomyStatus.ACTIVE) {
            main.econ.depositPlayer(p, reason, money);
        }
    }

    public static boolean hasEnoughMoney(final CommandSender sender, final double money, final String messageWhenNotEnoughMoney, final String reason) {

        final Main main = Main.getInstance();

        main.debug("Checking if " + sender.getName() + " has at least " + money + " money...");

        if (!(sender instanceof Player)) {
            main.debug(sender.getName() + " is no player, so they should have enough money lol");
            return true;
        }

        if (main.economyStatus != EconomyStatus.ACTIVE) {
            main.debug("We already know that economy support is not active, so all players have enough money!");
            return true;
        }

        if (money <= 0) {
            main.debug("yes: money <= 0");
            return true;
        }

        final Player player = (Player) sender;

        if (main.econ.getBalance(player) >= money) {
            main.econ.withdrawPlayer(player, reason, money);
            main.debug("yes, enough money and paid");
            return true;
        } else {
            main.debug("no, not enough money - nothing paid");
            player.sendMessage(messageWhenNotEnoughMoney);
            return false;
        }

    }

    /*
    Integer = chest ID (starting at 1)
    AngelChest = affected chest
    Player = chest owner
     */
    public static @Nullable Triplet<Integer, AngelChest, OfflinePlayer> argIdx2AngelChest(final Main main, final CommandSender sendTo, final OfflinePlayer affectedPlayer, final String chest) {

        int chestIdStartingAt1;

        // Get all AngelChests by this player
        final ArrayList<AngelChest> angelChestsFromThisPlayer = AngelChestUtils.getAllAngelChestsFromPlayer(affectedPlayer);

        if (angelChestsFromThisPlayer.isEmpty()) {
            sendTo.sendMessage(main.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
            return null;
        }

        if (angelChestsFromThisPlayer.size() > 1 && chest == null) {
            sendTo.sendMessage(main.messages.MSG_PLEASE_SELECT_CHEST);
            sendListOfAngelChests(main, sendTo, affectedPlayer);
            return null;
        } else {
            chestIdStartingAt1 = 1;
        }

        if (chest != null) {
            try {
                chestIdStartingAt1 = Integer.parseInt(chest);
            } catch (final NumberFormatException exception) {
                sendTo.sendMessage(main.messages.ERR_INVALIDCHEST);
                return null;
            }
        }

        if (chestIdStartingAt1 > angelChestsFromThisPlayer.size() || chestIdStartingAt1 < 1) {
            sendTo.sendMessage(main.messages.ERR_INVALIDCHEST);
            return null;
        }

        return new Triplet<>(chestIdStartingAt1, angelChestsFromThisPlayer.get(chestIdStartingAt1 - 1), affectedPlayer);
    }

    public static void sendConfirmMessage(final CommandSender sender, final String command, final double price, final String message) {
        final TextComponent text = new TextComponent(message.replaceAll("\\{price}", String.valueOf(price)).replaceAll("\\{currency}", getCurrency(price)));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        sender.spigot().sendMessage(text);
    }

    /**
     * If args is null, skip the confirmation stuff
     */
    public static void fetchOrTeleport(final Main main, final Player sender, final AngelChest ac, final int chestIdStartingAt1, final CommandAction action, final boolean askForConfirmation) {

        if (!sender.hasPermission(action.getPermission())) {
            sender.sendMessage(main.messages.MSG_NO_PERMISSION);
            return;
        }

        UUID uuid = ac.owner;
        if (sender instanceof Player) {
            uuid = sender.getUniqueId();
        }

        if (!ac.owner.equals(uuid) && !sender.hasPermission(Permissions.OTHERS)) {
            sender.sendMessage(main.messages.ERR_NOTOWNER);
            return;
        }

        final double price = action.getPrice(sender);

        if (askForConfirmation && main.economyStatus != EconomyStatus.INACTIVE) {
            if (!hasConfirmed(main, sender, chestIdStartingAt1, price, action)) return;
        }

        if (price > 0 && !hasEnoughMoney(sender, price, main.messages.MSG_NOT_ENOUGH_MONEY, action.getEconomyReason())) {
            return;
        }
        switch (action) {
            case TELEPORT_TO_CHEST:
                teleportPlayerToChest(main, sender, ac);
                break;
            case FETCH_CHEST:
                fetchChestToPlayer(main, sender, ac);
                break;
        }
    }


    private static void fetchChestToPlayer(final Main main, final Player player, final AngelChest ac) {

        final String dir = AngelChestUtils.getCardinalDirection(player);
        final Location newLoc = BlockDataUtils.getLocationInDirection(player.getLocation(), dir);
        final BlockFace facing = BlockDataUtils.getChestFacingDirection(dir);

        final Block newBlock = AngelChestUtils.getChestLocation(newLoc.getBlock());
        final Block oldBlock = ac.block;

        // Move the block in game
        ac.destroyChest(oldBlock);
        ac.createChest(newBlock, ac.owner);

        // Make the chest face the player
        BlockDataUtils.setBlockDirection(newBlock, facing);

        // Swap the block in code
        main.angelChests.put(newBlock, main.angelChests.remove(oldBlock));
        main.angelChests.get(newBlock).block = newBlock;

        player.sendMessage(main.messages.MSG_RETRIEVED);
    }


    private static void teleportPlayerToChest(final Main main, final Player p, final AngelChest ac) {
        if (main.getConfig().getBoolean(Config.ASYNC_CHUNK_LOADING)) {
            final AtomicInteger chunkLoadingTask = new AtomicInteger();
            chunkLoadingTask.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
                if (areChunksLoadedNearby(ac.block.getLocation(), main)) {
                    main.debug("[Async chunk loading] All chunks loaded! Teleporting now!");
                    doActualTeleport(main, p, ac);
                    Bukkit.getScheduler().cancelTask(chunkLoadingTask.get());
                } else {
                    main.debug("[Async chunk loading] Not all chunks are loaded yet, waiting...");
                }
            }, 1L, 1L));
        } else {
            main.debug("[Async chunk loading] You disabled async-chunk-loading. Chunk loading COULD cause tps losses! See config.yml");
            doActualTeleport(main, p, ac);
        }
    }

    private static boolean hasConfirmed(final Main main, final CommandSender p, final int chestIdStartingAt1, final double price, final CommandAction action) {
        main.debug("Creating confirm message for Chest ID " + chestIdStartingAt1);
        main.debug("Action: " + action.toString());
        String confirmCommand = String.format("/%s ", action.getCommand());
        confirmCommand += chestIdStartingAt1;
        final UUID uuid = p instanceof Player ? ((Player) p).getUniqueId() : Main.consoleSenderUUID;
        if (price > 0) {
            final PendingConfirm newConfirm = new PendingConfirm(chestIdStartingAt1, action);
            final PendingConfirm oldConfirm = main.pendingConfirms.get(uuid);
            if (newConfirm.equals(oldConfirm)) {
                main.pendingConfirms.remove(uuid);
                return true;
            } else {
                main.pendingConfirms.put(uuid, newConfirm);
                sendConfirmMessage(p, confirmCommand, price, main.messages.MSG_CONFIRM);
                return false;
            }
        }
        return true;
    }

    private static boolean areChunksLoadedNearby(final Location loc, final Main main) {
        boolean allChunksLoaded = true;
        //ArrayList<Location> locs = new ArrayList<>();
        for (int x = -16; x <= 16; x += 16) {
            for (int z = -16; z <= 16; z += 16) {
                if (!isChunkLoaded(loc.add(x, 0, z))) {
                    main.debug("Chunk at " + loc.add(x, 0, z) + " is not loaded yet, waiting...");
                    allChunksLoaded = false;
                }
            }
        }
        return allChunksLoaded;
    }

    private static boolean isChunkLoaded(final Location loc) {
        PaperLib.getChunkAtAsync(loc);
        return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    private static void doActualTeleport(final Main main, final Player p, final AngelChest ac) {
        final Location acloc = ac.block.getLocation();
        Location tploc = acloc.clone();
        final double tpDistance = main.getConfig().getDouble("tp-distance");
        // TODO: Find safe spot instead of just any block
        try {
            // offset the target location
            switch (BlockDataUtils.getBlockDirection(ac.block)) {
                case SOUTH:
                    tploc.add(0, 0, tpDistance);
                    break;
                case WEST:
                    tploc.add(-tpDistance, 0, 0);
                    break;
                case NORTH:
                    tploc.add(0, 0, -tpDistance);
                    break;
                case EAST:
                    tploc.add(tpDistance, 0, 0);
                    break;
                default:
                    break;
            }
        } catch (final Throwable ignored) {

        }

        // Search for a safe spawn point
        final List<Block> possibleSpawnPoints = AngelChestUtils.getPossibleTPLocations(tploc, main.getConfig().getInt(Config.MAX_RADIUS));
        AngelChestUtils.sortBlocksByDistance(tploc.getBlock(), possibleSpawnPoints);

        if (!possibleSpawnPoints.isEmpty()) {
            tploc = possibleSpawnPoints.get(0).getLocation();
        }
        if (possibleSpawnPoints.isEmpty()) {
            tploc = acloc.getBlock().getRelative(0, 1, 0).getLocation();
        }

        // Set yaw and pitch of camera
        final Location headloc = tploc.clone();
        headloc.add(0, 1, 0);
        tploc.setDirection(acloc.toVector().subtract(headloc.toVector()));
        tploc.add(0.5, 0, 0.5);

        p.teleport(tploc, TeleportCause.PLUGIN);
    }

    public static String getCurrency(final double money) {

        /*Plugin v = main.getServer().getPluginManager().getPlugin("Vault");
        if (v == null) return "";

        RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return "";

        if (rsp.getProvider() == null) return "";

        Economy econ = rsp.getProvider();

        if (econ == null) return "";*/

        final Main main = Main.getInstance();
        if (main.economyStatus == EconomyStatus.ACTIVE) {
            return money == 1 ? main.econ.currencyNameSingular() : main.econ.currencyNamePlural();
        }

        return "";

    }

    public static void unlockSingleChest(final Main main, final CommandSender requester, final AngelChest ac) {
//		if(!p.hasPermission("angelchest.tp")) {
//			p.sendMessage(plugin.getCommand("aclist").getPermissionMessage());
//			return;
//		}
        /*
        if (!ac.owner.equals(affectedPlayer.getUniqueId())) {
            affectedPlayer.sendMessage(main.messages.ERR_NOTOWNER);
            return;
        }
        */

        if (!ac.isProtected) {
            requester.sendMessage(main.messages.ERR_ALREADYUNLOCKED);
            return;
        }

        ac.unlock();
        ac.scheduleBlockChange();
        requester.sendMessage(main.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
    }

    public static void sendListOfAngelChests(final Main main, final CommandSender sendTo, final OfflinePlayer affectedPlayer) {
        // Get all AngelChests by this player
        final ArrayList<AngelChest> angelChestsFromThisPlayer = AngelChestUtils.getAllAngelChestsFromPlayer(affectedPlayer);

        if (angelChestsFromThisPlayer.isEmpty()) {
            sendTo.sendMessage(main.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
            return;
        }

        int chestIndex = 1;
        Block b;

        for (final AngelChest angelChest : angelChestsFromThisPlayer) {


            String affectedPlayerParameter = "";
            if (!affectedPlayer.equals(sendTo)) affectedPlayerParameter = affectedPlayer.getName() + " ";

            b = angelChest.block;
            String tpCommand = null;
            String fetchCommand = null;
            String unlockCommand = null;
            if (sendTo.hasPermission(Permissions.TP)) {
                tpCommand = "/actp " + affectedPlayerParameter + chestIndex;
            }
            if (sendTo.hasPermission(Permissions.FETCH)) {
                fetchCommand = "/acfetch " + affectedPlayerParameter + chestIndex;
            }
            if (angelChest.isProtected) {
                unlockCommand = "/acunlock " + affectedPlayerParameter + chestIndex;
            }

            String text;

            text = main.messages.ANGELCHEST_LIST;
            text = text.replaceAll("\\{id}", String.valueOf(chestIndex));
            text = text.replaceAll("\\{x}", String.valueOf(b.getX()));
            text = text.replaceAll("\\{y}", String.valueOf(b.getY()));
            text = text.replaceAll("\\{z}", String.valueOf(b.getZ()));
            text = text.replaceAll("\\{time}", getTimeLeft(angelChest));
            text = text.replaceAll("\\{world}", b.getWorld().getName());
            sendTo.spigot().sendMessage(LinkUtils.getLinks(sendTo, affectedPlayer, text, tpCommand, unlockCommand, fetchCommand));
            chestIndex++;
        }
    }

    // TODO: Make this generic to getTimeLeft(AngelChest)
    public static String getUnlockTimeLeft(final AngelChest angelChest) {
        final int remaining = angelChest.unlockIn;
        final int sec = remaining % 60;
        final int min = (remaining / 60) % 60;
        final int hour = (remaining / 60) / 60;

        final String time;
        if (hour > 0) {
            time = String.format("%02d:%02d:%02d",
                    hour, min, sec
            );

        } else {
            time = String.format("%02d:%02d",
                    min, sec
            );
        }

        return time;
    }

    public static String getTimeLeft(final AngelChest angelChest) {
        final int remaining = angelChest.secondsLeft;
        final int sec = remaining % 60;
        final int min = (remaining / 60) % 60;
        final int hour = (remaining / 60) / 60;

        final String time;
        if (angelChest.infinite) {
            //text = String.format("[%d] §aX:§f %d §aY:§f %d §aZ:§f %d | %s ",
            //		chestIndex, b.getX(), b.getY(), b.getZ(), b.getWorld().getName()
            time = "∞";
            //);
        } else if (hour > 0) {
            time = String.format("%02d:%02d:%02d",
                    hour, min, sec
            );

        } else {
            time = String.format("%02d:%02d",
                    min, sec
            );
        }

        return time;
    }

    /*
    public static void unlockAllChests(Main main, Player p) {
        ArrayList<AngelChest> angelChestsFromThisPlayer = Utils.getAllAngelChestsFromPlayer(p);

        int chestsUnlocked = 0;

        for (AngelChest angelChest : angelChestsFromThisPlayer) {
            if (angelChest.isProtected) {
                angelChest.unlock();
                angelChest.scheduleBlockChange();
                chestsUnlocked++;
            }
        }

        if (chestsUnlocked == 0) {
            p.sendMessage(main.messages.MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED);
        } else if (chestsUnlocked == 1) {
            p.sendMessage(main.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
        } else {
            p.sendMessage(String.format(main.messages.MSG_UNLOCKED_MORE_ANGELCHESTS, chestsUnlocked));
        }
    }*/
}
