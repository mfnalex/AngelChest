package de.jeff_media.angelchest.utils;

import com.jeff_media.jefflib.*;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.PendingConfirm;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.enums.EconomyStatus;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.listeners.InvulnerabilityListener;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.nbt.NBTValues;
import de.jeff_media.daddy.Daddy_Stepsister;
import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class CommandUtils {

    static final int CHUNK_SIZE = 16;

    private static boolean areChunksLoadedNearby(final Location loc, final AngelChestMain main) {
        boolean allChunksLoaded = true;
        //ArrayList<Location> locs = new ArrayList<>();
        for (int x = -CHUNK_SIZE; x <= CHUNK_SIZE; x += CHUNK_SIZE) {
            for (int z = -CHUNK_SIZE; z <= CHUNK_SIZE; z += CHUNK_SIZE) {
                if (!isChunkLoaded(loc.add(x, 0, z))) {
                    if (main.debug) main.debug("Chunk at " + loc.add(x, 0, z) + " is not loaded yet, waiting...");
                    allChunksLoaded = false;
                }
            }
        }
        return allChunksLoaded;
    }

    /*
    Integer = chest ID (starting at 1)
    AngelChest = affected chest
    Player = chest owner
     */
    public static @Nullable Triplet<Integer, AngelChest, OfflinePlayer> argIdx2AngelChest(final AngelChestMain main, final CommandSender sendTo, final OfflinePlayer affectedPlayer, final String chest) {

        int chestIdStartingAt1;

        // Get all AngelChests by this player
        final ArrayList<AngelChest> angelChestsFromThisPlayer = AngelChestUtils.getAllAngelChestsFromPlayer(affectedPlayer);

        if (angelChestsFromThisPlayer.isEmpty()) {
            Messages.send(sendTo, main.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
            return null;
        }

        if (angelChestsFromThisPlayer.size() > 1 && chest == null) {
            Messages.send(sendTo, main.messages.MSG_PLEASE_SELECT_CHEST);
            sendListOfAngelChests(main, sendTo, affectedPlayer);
            return null;
        } else {
            chestIdStartingAt1 = 1;
        }

        if (chest != null) {
            try {
                chestIdStartingAt1 = Integer.parseInt(chest);
            } catch (final NumberFormatException exception) {
                Messages.send(sendTo, main.messages.ERR_INVALIDCHEST);
                return null;
            }
        }

        if (chestIdStartingAt1 > angelChestsFromThisPlayer.size() || chestIdStartingAt1 < 1) {
            Messages.send(sendTo, main.messages.ERR_INVALIDCHEST);
            return null;
        }

        return new Triplet<>(chestIdStartingAt1, angelChestsFromThisPlayer.get(chestIdStartingAt1 - 1), affectedPlayer);
    }

    private static void doActualTeleport(final AngelChestMain main, final Player player, final AngelChest ac) {
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> SoundUtils.playTpFetchSound(player, ac.getBlock().getLocation(), CommandAction.TELEPORT_TO_CHEST), 1L);
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
        //noinspection MagicNumber
        tploc.add(0.5, 0, 0.5);

        player.teleport(tploc, TeleportCause.PLUGIN);

        // Add invulnerability
        final int seconds = main.groupManager.getInvulnerabilityTimePerPlayer(player);
        if (seconds > 0 && Daddy_Stepsister.allows(PremiumFeatures.INVULNERABILITY_ON_TP)) {

            if (main.debug)
                main.debug("Making player " + player.getName() + " invulnerable for " + main.getConfig().getDouble(Config.INVULNERABILITY_AFTER_TP) + " seconds");
            if (NBTAPI.hasNBT(player, NBTTags.IS_INVULNERABLE)) {
                InvulnerabilityListener.removeGod(player);
                if (main.invulnerableTasks.containsKey(player.getUniqueId())) {
                    Bukkit.getScheduler().cancelTask(main.invulnerableTasks.get(player.getUniqueId()));
                }
            }
            NBTAPI.addNBT(player, NBTTags.IS_INVULNERABLE, NBTValues.TRUE);

            final AtomicInteger secondsLeft = new AtomicInteger(seconds);
            final AtomicInteger finalTask = new AtomicInteger(-1);
            finalTask.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
                if (player == null || !player.isOnline()) {
                    if (finalTask.get() != -1) {
                        Bukkit.getScheduler().cancelTask(finalTask.get());
                    }
                }

                if (secondsLeft.getAndDecrement() > 0) {
                    Messages.sendActionBar(player, main.messages.MSG_ACTIONBAR_INVULNERABLE.replace("{time}", getFormattedTime(secondsLeft.get(), false)));
                } else {
                    if (finalTask.get() != -1) {
                        Bukkit.getScheduler().cancelTask(finalTask.get());
                        Messages.sendActionBar(player, main.messages.MSG_ACTIONBAR_VULNERABLE);
                        InvulnerabilityListener.removeGod(player);
                    }
                }
            }, 0, Ticks.fromSeconds(1)));
            main.invulnerableTasks.put(player.getUniqueId(), finalTask.get());
        } else if (seconds <= 0) {
            if (main.debug) main.debug("Invulnerability time is set to 0.");
        } else {
            if (!Daddy_Stepsister.allows(PremiumFeatures.INVULNERABILITY_ON_TP)) {
                Messages.sendPremiumOnlyConsoleMessage(Config.INVULNERABILITY_AFTER_TP);
            }
        }
    }

    private static void fetchChestToPlayer(final AngelChestMain main, final Player player, final AngelChest ac) {

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
        //main.angelChests.add(newBlock, main.angelChests.remove(oldBlock));
        ac.block = newBlock;

        Messages.send(player, main.messages.MSG_RETRIEVED);
    }

    /**
     * If args is null, skip the confirmation stuff
     */
    public static void fetchOrTeleport(final AngelChestMain main, final Player sender, final AngelChest ac, final int chestIdStartingAt1, final CommandAction action, final boolean askForConfirmation) {

        if (!sender.hasPermission(action.getPermission())) {
            Messages.send(sender, main.messages.MSG_NO_PERMISSION);
            return;
        }

        final UUID uuid = sender.getUniqueId();

        if (!ac.owner.equals(uuid) && !sender.hasPermission(Permissions.OTHERS)) {
            Messages.send(sender, main.messages.ERR_NOTOWNER);
            return;
        }

        final double price = action.getPrice(sender);
        final ItemStack priceItem = action.getPriceItem(sender);

        // Allow TP / Fetch across worlds
        final World playerWorld = sender.getWorld();
        final World chestWorld = ac.getWorld();
        if (action == CommandAction.TELEPORT_TO_CHEST && !main.groupManager.getAllowTpAcrossWorlds(sender)) {
            if (!playerWorld.equals(chestWorld)) {
                Messages.send(sender, main.messages.MSG_TP_ACROSS_WORLDS_NOT_ALLOWED);
                if (main.debug) main.debug("Forbidden TP across worlds detected.");
                if (main.debug) main.debug("Player World: " + playerWorld);
                if (main.debug) main.debug("Chest  World: " + chestWorld.toString());
                return;
            }
        }
        if (action == CommandAction.FETCH_CHEST && !main.groupManager.getAllowFetchAcrossWorlds(sender)) {
            if (!playerWorld.equals(chestWorld)) {
                Messages.send(sender, main.messages.MSG_FETCH_ACROSS_WORLDS_NOT_ALLOWED);
                if (main.debug) main.debug("Forbidden Fetch across worlds detected.");
                if (main.debug) main.debug("Player World: " + playerWorld);
                if (main.debug) main.debug("Chest  World: " + chestWorld.toString());
                return;
            }
        }
        // Max / Min TP / Fetch distance
        if (playerWorld.equals(chestWorld) && playerWorld.getUID().equals(ac.worldid)) {
            final double distance = sender.getLocation().distance(ac.block.getLocation());
            if (main.debug) main.debug("Fetch / TP in same world. Distance: " + distance);

            // Max distance
            final int maxTpDistance = main.groupManager.getMaxTpDistance(sender);
            final int maxFetchDistance = main.groupManager.getMaxFetchDistance(sender);
            if (action == CommandAction.TELEPORT_TO_CHEST && maxTpDistance > 0 && distance > maxTpDistance) {
                Messages.send(sender, main.messages.MSG_MAX_TP_DISTANCE.replace("{distance}", String.valueOf(maxTpDistance)));
                return;
            }
            if (action == CommandAction.FETCH_CHEST && maxFetchDistance > 0 && distance > maxFetchDistance) {
                Messages.send(sender, main.messages.MSG_MAX_FETCH_DISTANCE.replace("{distance}", String.valueOf(maxFetchDistance)));
                return;
            }

            // Min distance
            final int minDistance = main.getConfig().getInt(Config.MIN_DISTANCE);
            if (minDistance > 0 && distance < minDistance) {
                Messages.send(sender, main.messages.MSG_MIN_DISTANCE);
                return;
            }
        }

        if (askForConfirmation && main.economyStatus != EconomyStatus.INACTIVE) {
            if (!hasConfirmed(main, sender, chestIdStartingAt1, price, action)) return;
        }

        // TP Wait time
        if(Daddy_Stepsister.allows(PremiumFeatures.TP_WAIT_TIME)) {
            if (main.groupManager.getTpWaitTime(sender) > 0 && action == CommandAction.TELEPORT_TO_CHEST) {
                final int delay = (int) Ticks.fromSeconds(main.groupManager.getTpWaitTime(sender));
                AtomicInteger ticks = new AtomicInteger(delay);
                final BossBar bar = Bukkit.createBossBar(main.messages.MSG_TELEPORT_BOSSBAR, BarColor.GREEN, BarStyle.SOLID);
                bar.setProgress(1);
                bar.addPlayer(sender);
                final Vector position = sender.getLocation().toVector();
                new BukkitRunnable() {
                    private void remove() {
                        bar.removePlayer(sender);
                        cancel();
                    }

                    @Override
                    public void run() {
                        final int remaining = ticks.decrementAndGet();
                        if (sender.getLocation().toVector().distanceSquared(position) > 0.25) {
                            remove();
                            return;
                        }
                        if (remaining == 0) {
                            remove();
                            if ((price > 0 || priceItem != null ) && !hasEnoughMoney(sender, price, priceItem, main.messages.MSG_NOT_ENOUGH_MONEY, main.messages.MSG_HAS_NO_ITEM,action.getEconomyReason())) {
                                return;
                            }
                            teleportPlayerToChest(main, sender, ac);
                            Messages.send(sender, main.messages.MSG_ANGELCHEST_TELEPORTED);
                            return;
                        }
                        bar.setProgress((double) ticks.get() / (double) delay);
                    }
                }.runTaskTimer(main, 0, 1);
                return;
            }
        }

        if(action == CommandAction.FETCH_CHEST) {
            if(!ProtectionUtils.playerMayBuildHere(sender, sender.getLocation()) && main.getConfig().getBoolean(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD)) {
                sender.sendMessage(main.messages.MSG_CANT_FETCH_HERE);
                return;
            }
        }

        if ((price > 0 || priceItem != null) && !hasEnoughMoney(sender, price, priceItem, main.messages.MSG_NOT_ENOUGH_MONEY, main.messages.MSG_HAS_NO_ITEM, action.getEconomyReason())) {
            return;
        }
        switch (action) {
            case TELEPORT_TO_CHEST:
                teleportPlayerToChest(main, sender, ac);
                Messages.send(sender, main.messages.MSG_ANGELCHEST_TELEPORTED);
                break;
            case FETCH_CHEST:
                fetchChestToPlayer(main, sender, ac);
                Messages.send(sender, main.messages.MSG_ANGELCHEST_FETCHED);
                SoundUtils.playTpFetchSound(sender, sender.getLocation(), CommandAction.FETCH_CHEST);
                break;
        }
    }

    public static String getCurrency(final double money) {

        /*Plugin v = main.getServer().getPluginManager().getPlugin("Vault");
        if (v == null) return "";

        RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return "";

        if (rsp.getProvider() == null) return "";

        Economy econ = rsp.getProvider();

        if (econ == null) return "";*/

        final AngelChestMain main = AngelChestMain.getInstance();
        if (main.economyStatus == EconomyStatus.ACTIVE) {
            return money == 1 ? main.econ.currencyNameSingular() : main.econ.currencyNamePlural();
        }

        return "";

    }

    public static String getFormattedTime(final int seconds, final boolean infinite) {
        final int sec = seconds % 60;
        final int min = (seconds / 60) % 60;
        final int hour = (seconds / 60) / 60;

        final String time;
        if (infinite) {
            //text = String.format("[%d] §aX:§f %d §aY:§f %d §aZ:§f %d | %s ",
            //		chestIndex, b.getX(), b.getY(), b.getZ(), b.getWorld().getName()
            time = "∞";
            //);
        } else if (hour > 0) {
            time = String.format("%02d:%02d:%02d", hour, min, sec);

        } else {
            time = String.format("%02d:%02d", min, sec);
        }

        return time;
    }

    public static String getTimeLeft(final AngelChest angelChest) {
        return getFormattedTime(angelChest.getSecondsLeft(), angelChest.isInfinite());
    }

    public static String getUnlockTimeLeft(final AngelChest angelChest) {
        return getFormattedTime(angelChest.getUnlockIn(), false);
    }

    private static boolean hasConfirmed(final AngelChestMain main, final CommandSender p, final int chestIdStartingAt1, final double price, final CommandAction action) {
        if (main.debug) main.debug("Creating confirm message for Chest ID " + chestIdStartingAt1);
        if (main.debug) main.debug("Action: " + action.toString());
        String confirmCommand = String.format("/%s ", action.getCommand());
        confirmCommand += chestIdStartingAt1;
        final UUID uuid = p instanceof Player ? ((Player) p).getUniqueId() : AngelChestMain.consoleSenderUUID;
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

    public static double getBalance(Player player) {
        final AngelChestMain main = AngelChestMain.getInstance();

        if (main.economyStatus != EconomyStatus.ACTIVE) {
            return 0;
        }

        return main.econ.getBalance(player);
    }

    public static boolean hasEnoughMoney(final CommandSender sender, final double money, @Nullable ItemStack item, final String messageWhenNotEnoughMoney, final String messageWhenNotEnoughItems, final String reason) {

        if(item != null && !Daddy_Stepsister.allows(PremiumFeatures.CUSTOM_ITEMS)) {
            AngelChestMain.getInstance().getLogger().warning("Using custom items for payments is only available in AngelChest Plus.");
            return true;
        }

        final AngelChestMain main = AngelChestMain.getInstance();

        if (!(sender instanceof Player)) {
            if (main.debug) main.debug(sender.getName() + " is no player, so they should have enough money lol");
            return true;
        }

        final Player player = (Player) sender;

        if(item != null && item.getItemMeta() != null) {
            main.debug("Checking if " + sender + " has AngelChest item: "+ item);

            if(ItemUtils.checkForAndRemoveOneItem(PDCUtils.get(item,NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING), player.getInventory(), item)) {
                main.debug("Yes, player has this item!");
                return true;
            } else {
                main.debug("No, the player doesn't have this item!");
                String itemName = MaterialUtils.getNiceMaterialName(item.getType());
                if(item.getItemMeta().hasDisplayName()
                        && item.getItemMeta().getDisplayName() != null
                        && !item.getItemMeta().getDisplayName().isEmpty()) itemName = item.getItemMeta().getDisplayName();
                Messages.send(player, messageWhenNotEnoughItems.replace("{item}",itemName));
                return false;
            }

        }

        if (main.debug) main.debug("Checking if " + sender.getName() + " has at least " + money + " money...");



        if (main.economyStatus != EconomyStatus.ACTIVE) {
            if (main.debug)
                main.debug("We already know that economy support is not active, so all players have enough money!");
            return true;
        }

        if (money <= 0) {
            if (main.debug) main.debug("yes: money <= 0");
            return true;
        }

        if (main.econ.getBalance(player) >= money) {
            main.econ.withdrawPlayer(player, reason, money);
            if (main.debug) main.debug("yes, enough money and paid");
            return true;
        } else {
            if (main.debug) main.debug("no, not enough money - nothing paid");
            Messages.send(player, messageWhenNotEnoughMoney);
            return false;
        }

    }

    private static boolean isChunkLoaded(final Location loc) {
        PaperLib.getChunkAtAsync(loc);
        return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public static void payMoney(final OfflinePlayer p, final double money, final String reason) {

        final AngelChestMain main = AngelChestMain.getInstance();

        if (money <= 0) {
            return;
        }

        if (main.economyStatus == EconomyStatus.ACTIVE) {
            main.econ.depositPlayer(p, reason, money);
        }
    }

    public static void sendConfirmMessage(final CommandSender sender, final String command, final double price, final String message) {
        final TextComponent text = new TextComponent(message.replace("{price}", AngelChestMain.getInstance().getCurrencyFormatter().format(price)).replace("{currency}", getCurrency(price)));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        sender.spigot().sendMessage(text);
    }

    public static void sendListOfAngelChests(final AngelChestMain main, final CommandSender sendTo, final OfflinePlayer affectedPlayer) {
        // Get all AngelChests by this player
        final ArrayList<AngelChest> angelChestsFromThisPlayer = AngelChestUtils.getAllAngelChestsFromPlayer(affectedPlayer);

        if (angelChestsFromThisPlayer.isEmpty()) {
            Messages.send(sendTo, main.messages.MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS);
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
            if (angelChest.isProtected && sendTo.hasPermission(Permissions.UNLOCK)) {
                unlockCommand = "/acunlock " + affectedPlayerParameter + chestIndex;
            }

            String text;

            text = main.messages.ANGELCHEST_LIST;
            text = text.replace("{id}", String.valueOf(chestIndex));
            text = text.replace("{x}", String.valueOf(b.getX()));
            text = text.replace("{y}", String.valueOf(b.getY()));
            text = text.replace("{z}", String.valueOf(b.getZ()));
            text = text.replace("{time}", getTimeLeft(angelChest));
            text = text.replace("{world}", b.getWorld().getName());
            text = ChatColor.translateAlternateColorCodes('&',TextUtils.format(text,affectedPlayer));
            // TODO: This doesn't work with gradients :/
            sendTo.spigot().sendMessage(LinkUtils.getLinks(sendTo, affectedPlayer, text, tpCommand, unlockCommand, fetchCommand));
            chestIndex++;
        }
    }

    private static void teleportPlayerToChest(final AngelChestMain main, final Player p, final AngelChest ac) {
        if (main.getConfig().getBoolean(Config.ASYNC_CHUNK_LOADING)) {
            final AtomicInteger chunkLoadingTask = new AtomicInteger();
            chunkLoadingTask.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
                if (areChunksLoadedNearby(ac.block.getLocation(), main)) {
                    if (main.debug) main.debug("[Async chunk loading] All chunks loaded! Teleporting now!");
                    doActualTeleport(main, p, ac);
                    Bukkit.getScheduler().cancelTask(chunkLoadingTask.get());
                } else {
                    if (main.debug) main.debug("[Async chunk loading] Not all chunks are loaded yet, waiting...");
                }
            }, 1L, 1L));
        } else {
            if (main.debug)
                main.debug("[Async chunk loading] You disabled async-chunk-loading. Chunk loading COULD cause tps losses! See config.yml");
            doActualTeleport(main, p, ac);
        }
    }

    public static void unlockSingleChest(final AngelChestMain main, final CommandSender requester, final AngelChest ac) {
//		if(!p.hasPermission("angelchest.tp")) {
//			Messages.send(p,plugin.getCommand("aclist").getPermissionMessage());
//			return;
//		}
        /*
        if (!ac.owner.equals(affectedPlayer.getUniqueId())) {
            affectedPlayer.sendMessage(main.messages.ERR_NOTOWNER);
            return;
        }
        */

        if (!ac.isProtected) {
            Messages.send(requester, main.messages.ERR_ALREADYUNLOCKED);
            return;
        }
        //main.getChestMaterial(ac).remove(ac.block);
        //ac.destroyChest(ac.block);
        ac.unlock();
        ac.scheduleBlockChange();
        Messages.send(requester, main.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
    }

    /*
    public static void unlockAllChests(AngelChestMain main, Player p) {
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
            Messages.send(p,main.messages.MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED);
        } else if (chestsUnlocked == 1) {
            Messages.send(p,main.messages.MSG_UNLOCKED_ONE_ANGELCHEST);
        } else {
            Messages.send(p,String.format(main.messages.MSG_UNLOCKED_MORE_ANGELCHESTS, chestsUnlocked));
        }
    }*/
}
