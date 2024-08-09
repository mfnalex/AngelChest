package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.Group;
import de.jeff_media.angelchest.enums.EconomyStatus;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.utils.InventoryUtils;
import de.jeff_media.daddy.Daddy_Stepsister;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public final class GroupManager {

    private static final String DOT = ".";
    private final AngelChestMain main;
    private LinkedHashMap<String, Group> groups;
    private YamlConfiguration yaml;

    public GroupManager(final File yamlFile) {
        this.main = AngelChestMain.getInstance();
        if (!yamlFile.exists()) {
            main.getLogger().info("groups.yml does not exist, skipping custom group settings.");
            return;
        }
        this.yaml = YamlConfiguration.loadConfiguration(yamlFile);
        groups = new LinkedHashMap<>();


        for (final String groupName : yaml.getKeys(false)) {
            final int angelchestDuration = yaml.getInt(groupName + DOT + Config.ANGELCHEST_DURATION, -1);
            final int angelChestDurationPvp = yaml.getInt(groupName + DOT + Config.ANGELCHEST_DURATION_IN_PVP, -2);
            Boolean suspendWhenOffline = null;
            if (yaml.isSet(groupName + DOT + Config.SUSPEND_COUNTDOWN_OFFLINE_PLAYERS)) {
                suspendWhenOffline = yaml.getBoolean(groupName + DOT + Config.SUSPEND_COUNTDOWN_OFFLINE_PLAYERS, false);
            }
            final int chestsPerPlayer = yaml.getInt(groupName + DOT + Config.MAX_ALLOWED_ANGELCHESTS, -1);
            final String priceSpawn;
            if (yaml.isSet(groupName + DOT + "price-spawn")) {
                priceSpawn = yaml.getString(groupName + DOT + "price-spawn");
            } else {
                priceSpawn = yaml.getString(groupName + DOT + Config.PRICE, "-1");
            }
            final String priceOpen = yaml.getString(groupName + DOT + Config.PRICE_OPEN, "-1");
            final String priceFetch = yaml.getString(groupName + DOT + Config.PRICE_FETCH, "-1");
            final String priceTeleport = yaml.getString(groupName + DOT + Config.PRICE_TELEPORT, "-1");
            final double xpPercentage = yaml.getDouble(groupName + DOT + Config.XP_PERCENTAGE, -2);
            final int unlockDuration = yaml.getInt(groupName + DOT + Config.UNLOCK_DURATION, -1);
            final double spawnChance = yaml.getDouble(groupName + DOT + Config.SPAWN_CHANCE, 1.0);
            final String itemLoss = yaml.getString(groupName + DOT + Config.ITEM_LOSS, "-1");
            final int invulnerabilityAfterTP = yaml.getInt(groupName + DOT + Config.INVULNERABILITY_AFTER_TP, -1);
            final Boolean allowTpAcrossWorlds = yaml.isSet(groupName + DOT + Config.ALLOW_TP_ACROSS_WORLDS) ? yaml.getBoolean(groupName + DOT + Config.ALLOW_TP_ACROSS_WORLDS) : null;
            final Boolean allowFetchAcrossWorlds = yaml.isSet(groupName + DOT + Config.ALLOW_FETCH_ACROSS_WORLDS) ? yaml.getBoolean(groupName + DOT + Config.ALLOW_FETCH_ACROSS_WORLDS) : null;
            final Integer maxTpDistance = yaml.isSet(groupName + DOT + Config.MAX_TP_DISTANCE) ? yaml.getInt(groupName + DOT + Config.MAX_TP_DISTANCE) : null;
            final Integer maxFetchDistance = yaml.isSet(groupName + DOT + Config.MAX_FETCH_DISTANCE) ? yaml.getInt(groupName + DOT + Config.MAX_FETCH_DISTANCE) : null;
            final Double tpWaitTime = yaml.isSet(groupName + DOT + Config.TP_WAIT_TIME) ? yaml.getDouble(groupName + DOT + Config.TP_WAIT_TIME) : null;
            final Group group = new Group(angelchestDuration, angelChestDurationPvp, suspendWhenOffline, chestsPerPlayer, priceSpawn, priceOpen, priceTeleport, priceFetch, xpPercentage, unlockDuration, spawnChance, itemLoss, invulnerabilityAfterTP, allowTpAcrossWorlds, allowFetchAcrossWorlds, maxTpDistance, maxFetchDistance, tpWaitTime);
            if (main.debug) main.debug("Created group \"" + groupName + "\": " + group);
            groups.put(groupName, group);

        }
    }

    private static int getPercentageItemLoss(final Player p, final String value) {
        final AngelChestMain main = AngelChestMain.getInstance();
        if (value.endsWith("p")) {
            if (!Daddy_Stepsister.allows(PremiumFeatures.RANDOM_ITEM_LOSS)) {
                main.getLogger().warning("You are using percentage random-item-loss in your config file. This is only available in AngelChestPlus. See here: " + AngelChestMain.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
                return 0;
            }
            final double percentage = Double.parseDouble(value.substring(0, value.length() - 1));
            if (percentage <= 0) {
                return 0;
            }

            if(main.getConfig().getBoolean(Config.RANDOM_ITEM_LOSS_SPLIT_STACKS)) {
                final int totalItems = InventoryUtils.countTotalItems(p.getInventory());
            }

            final int result =  main.getConfig().getBoolean(Config.RANDOM_ITEM_LOSS_SPLIT_STACKS)
                    ? (int) (InventoryUtils.getAmountOfItems(p.getInventory()) * percentage)
                    : (int) (InventoryUtils.getAmountOfItemStacks(p.getInventory()) * percentage);
            if (main.debug)
                main.debug("GroupManager -> Item Loss -> " + value + " contains a p, getting percentage for player " + p.getName() + ": " + result);
            return result;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static double getPercentagePrice(final CommandSender commandSender, final String value) {
        final AngelChestMain main = AngelChestMain.getInstance();
        if (value.endsWith("p")) {
            if (!Daddy_Stepsister.allows(PremiumFeatures.SET_PRICES_AS_PERCENTAGE)) {
                main.getLogger().warning("You are using percentage prices in your config file. This is only available in AngelChestPlus. See here: " + AngelChestMain.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
                return 0;
            }
            final double percentage = Double.parseDouble(value.substring(0, value.length() - 1));
            if (main.economyStatus != EconomyStatus.ACTIVE) {
                return 0;
            }
            if (percentage <= 0) {
                return 0;
            }
            final double result = commandSender instanceof Player ? main.econ.getBalance((OfflinePlayer) commandSender) * percentage : 0;
            if (main.debug)
                main.debug(value + " contains a p, getting percentage for player " + commandSender.getName() + ": " + result);
            return result;
        } else if(value.contains("%")) {
            if (!Daddy_Stepsister.allows(PremiumFeatures.SET_PRICES_AS_PERCENTAGE)) {
                main.getLogger().warning("You are using percentage prices in your config file. This is only available in AngelChestPlus. See here: " + AngelChestMain.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
                return 0;
            }

            String[] split = value.split(",");
            Double min = parseMinPrice(split);
            Double max = parseMaxPrice(split);
            Double percent = parsePercent(split);
            MinMaxPrice minMaxPrice = new MinMaxPrice(min, max, percent);
            return minMaxPrice.getEffectivePrice(main, commandSender);
        } else {
            return Double.parseDouble(value);
        }
    }

    static class MinMaxPrice {
        public final Double min;
        public final Double max;
        public final Double percent;

        MinMaxPrice(Double min, Double max, Double percent) {
            this.min = min;
            this.max = max;
            this.percent = percent;
        }

        double getEffectivePrice(AngelChestMain main, CommandSender sender) {
            if(!(sender instanceof Player)) return 0;
            if(main.economyStatus != EconomyStatus.ACTIVE) return 0;
            OfflinePlayer player = (OfflinePlayer) sender;
            double balance = main.econ.getBalance(player);
            double percentageOfBalance = balance * percent / 100;
            main.debug("Calculating effective price for player " + player.getName() + " with min=" + min + ", max=" + max + ", percent=" + percent + ", balance=" + balance + ", percentageOfBalance=" + percentageOfBalance);
            if(min != null && percentageOfBalance < min) {
                main.debug("Returning min: " + min);
                return min;
            }
            if(max != null && percentageOfBalance > max) {
                main.debug("Returning max: " + max);
                return max;
            }
            main.debug("Returning percentageOfBalance: " + percentageOfBalance);
            return percentageOfBalance;
        }
    }

    private static Double parseMinPrice(String[] split) {
        for(String s : split) {
            if(s.startsWith("min=")) {
                return Double.parseDouble(s.substring(4));
            }
        }
        return null;
    }

    private static Double parseMaxPrice(String[] split) {
        for(String s : split) {
            if(s.startsWith("max=")) {
                return Double.parseDouble(s.substring(4));
            }
        }
        return null;
    }

    private static Double parsePercent(String[] split) {
        for(String s : split) {
            if(s.endsWith("%")) {
                return Double.parseDouble(s.substring(0, s.length()-1));
            }
        }
        return null;
    }

    public boolean getAllowFetchAcrossWorlds(final CommandSender commandSender) {
        if (yaml == null) return main.getConfig().getBoolean(Config.ALLOW_FETCH_ACROSS_WORLDS);
        final Iterator<String> it = groups.keySet().iterator();
        Boolean bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            if (groups.get(group).allowFetchAcrossWorlds == null) continue;
            if (groups.get(group).allowFetchAcrossWorlds) return true;
            bestValueFound = false;
        }
        return bestValueFound == null && main.getConfig().getBoolean(Config.ALLOW_FETCH_ACROSS_WORLDS);
    }

    public boolean getAllowTpAcrossWorlds(final CommandSender commandSender) {
        if (yaml == null) return main.getConfig().getBoolean(Config.ALLOW_TP_ACROSS_WORLDS);
        final Iterator<String> it = groups.keySet().iterator();
        Boolean bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            if (groups.get(group).allowTpAcrossWorlds == null) continue;
            if (groups.get(group).allowTpAcrossWorlds) return true;
            bestValueFound = false;
        }
        return bestValueFound == null && main.getConfig().getBoolean(Config.ALLOW_TP_ACROSS_WORLDS);
    }

    public boolean getSuspendWhenOffline(final CommandSender commandSender) {
        if (yaml == null) return main.getConfig().getBoolean(Config.SUSPEND_COUNTDOWN_OFFLINE_PLAYERS);
        final Iterator<String> it = groups.keySet().iterator();
        Boolean bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (groups.get(group).suspendWhenOffline != null) {
                if (groups.get(group).suspendWhenOffline) {
                    return true;
                } else {
                    bestValueFound = false;
                }
            }
        }
        if (bestValueFound == null) {
            return main.getConfig().getBoolean(Config.SUSPEND_COUNTDOWN_OFFLINE_PLAYERS);
        } else {
            return bestValueFound;
        }
    }

    public int getMaxFetchDistance(final CommandSender commandSender) {
        final int result = getMaxFetchDistancePremium(commandSender);
        if (!Daddy_Stepsister.allows(PremiumFeatures.MAX_TP_FETCH_DISTANCE)) {
            Messages.sendPremiumOnlyConsoleMessage(Config.MAX_FETCH_DISTANCE);
            return 0;
        }
        return result;
    }

    public double getTpWaitTime(final CommandSender commandSender) {
        final double result = getTpWaitTimePremium(commandSender);
        if (!Daddy_Stepsister.allows(PremiumFeatures.TP_WAIT_TIME)) {
            Messages.sendPremiumOnlyConsoleMessage(Config.TP_WAIT_TIME);
            return 0;
        }
        return result;
    }

    private double getTpWaitTimePremium(final CommandSender commandSender) {

        if (yaml == null) return main.getConfig().getDouble(Config.TP_WAIT_TIME);
        final Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            if (groups.get(group).tpWaitTime == null) continue;
            if (bestValueFound == null) {
                bestValueFound = groups.get(group).tpWaitTime;
                continue;
            }
            bestValueFound = Math.min(bestValueFound, groups.get(group).tpWaitTime);
        }
        return bestValueFound == null ? main.getConfig().getInt(Config.TP_WAIT_TIME) : bestValueFound;
    }

    private int getMaxFetchDistancePremium(final CommandSender commandSender) {

        if (yaml == null) return main.getConfig().getInt(Config.MAX_FETCH_DISTANCE);
        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            if (groups.get(group).maxFetchDistance == null) continue;
            if (bestValueFound == null) {
                bestValueFound = groups.get(group).maxFetchDistance;
                continue;
            }
            bestValueFound = Math.max(bestValueFound, groups.get(group).maxFetchDistance);
        }
        return bestValueFound == null ? main.getConfig().getInt(Config.MAX_FETCH_DISTANCE) : bestValueFound;

    }

    public int getMaxTpDistance(final CommandSender commandSender) {
        final int result = getMaxTpDistancePremium(commandSender);
        if (!Daddy_Stepsister.allows(PremiumFeatures.MAX_TP_FETCH_DISTANCE)) {
            Messages.sendPremiumOnlyConsoleMessage(Config.MAX_TP_DISTANCE);
            return 0;
        }
        return result;
    }

    public int getMaxTpDistancePremium(final CommandSender commandSender) {
        if (yaml == null) return main.getConfig().getInt(Config.MAX_TP_DISTANCE);
        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            if (groups.get(group).maxTpDistance == null) continue;
            if (bestValueFound == null) {
                bestValueFound = groups.get(group).maxTpDistance;
                continue;
            }
            bestValueFound = Math.max(bestValueFound, groups.get(group).maxTpDistance);
        }
        return bestValueFound == null ? main.getConfig().getInt(Config.MAX_TP_DISTANCE) : bestValueFound;
    }

    public int getChestsPerPlayer(final Player p) {
        if (yaml == null) return main.getConfig().getInt(Config.MAX_ALLOWED_ANGELCHESTS);
        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final int valuePerPlayer = groups.get(group).maxChests;
            if (valuePerPlayer == -1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return main.getConfig().getInt(Config.MAX_ALLOWED_ANGELCHESTS);
        }
    }

    public int getDurationPerPlayer(final Player p) {
        if (yaml == null) return main.getConfig().getInt(Config.ANGELCHEST_DURATION);
        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final int valuePerPlayer = groups.get(group).duration;
            if (valuePerPlayer == -1) {
                continue;
            }
            if (valuePerPlayer == 0) {
                return 0;
            }
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return main.getConfig().getInt(Config.ANGELCHEST_DURATION);
        }
    }

    public int getPvpDurationPerPlayer(final Player p) {

        int globalPvpDefault = main.getConfig().getInt(Config.ANGELCHEST_DURATION_IN_PVP);
        int globalNonPvpDefault = main.getConfig().getInt(Config.ANGELCHEST_DURATION);

        if (yaml == null) {
            if (globalPvpDefault == -1) {
                return globalNonPvpDefault;
            } else {
                return globalPvpDefault;
            }
        }

        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final int declared = groups.get(group).pvpDuration;
            int valuePerPlayer = declared != -1 ? declared : groups.get(group).duration;
            if (valuePerPlayer == -2) {
                continue;
            }
            if (valuePerPlayer == -1) {
                valuePerPlayer = groups.get(group).duration;
            }

            if (valuePerPlayer == 0) {
                return 0;
            }
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return main.getConfig().getInt(Config.ANGELCHEST_DURATION_IN_PVP);
        }
    }

    public double getFetchPricePerPlayer(final CommandSender commandSender) {
        try {
            if (yaml == null || !Daddy_Stepsister.allows(PremiumFeatures.FETCH_PRICE_PER_PLAYER))
                return getPercentagePrice(commandSender, main.getConfig().getString(Config.PRICE_FETCH));
            final Iterator<String> it = groups.keySet().iterator();
            Double bestValueFound = null;
            while (it.hasNext()) {
                final String group = it.next();
                if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
                final String pricePerPlayer = groups.get(group).priceFetch;
                if (pricePerPlayer.equals("-1")) {
                    continue;
                }
                bestValueFound = bestValueFound == null ? getPercentagePrice(commandSender, pricePerPlayer) : Math.min(getPercentagePrice(commandSender, pricePerPlayer), bestValueFound);
            }
            if (bestValueFound != null) {
                return bestValueFound;
            } else {
                return getPercentagePrice(commandSender, main.getConfig().getString(Config.PRICE_FETCH));
            }
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public int getInvulnerabilityTimePerPlayer(final CommandSender commandSender) {
        if (yaml == null) return main.getConfig().getInt(Config.INVULNERABILITY_AFTER_TP);
        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final int timePerPlayer = groups.get(group).invulnerabilityAfterTP;
            if (timePerPlayer == -1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? timePerPlayer : Math.min(timePerPlayer, bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return main.getConfig().getInt(Config.INVULNERABILITY_AFTER_TP);
        }
    }

    public int getItemLossPerPlayer(final Player p) {
        if (yaml == null || !Daddy_Stepsister.allows(PremiumFeatures.RANDOM_ITEM_LOSS))
            return getPercentageItemLoss(p, main.getConfig().getString(Config.ITEM_LOSS));
        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final String itemLossPerPlayer = groups.get(group).itemLoss;
            if (itemLossPerPlayer.equals("-1")) {
                continue;
            }
            bestValueFound = bestValueFound == null ? getPercentageItemLoss(p, itemLossPerPlayer) : Math.min(getPercentageItemLoss(p, itemLossPerPlayer), bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return getPercentageItemLoss(p, main.getConfig().getString(Config.ITEM_LOSS));
        }
    }

    public List<String> getGroups(final Player p) {
        final List<String> matchingGroups = new ArrayList<>();
        for (final String group : groups.keySet()) {
            if (p.hasPermission(Permissions.PREFIX_GROUP + group)) {
                matchingGroups.add(group);
            }
        }
        return matchingGroups;
    }

    public double getOpenPricePerPlayer(final Player p) {
        try {
            if (!Daddy_Stepsister.allows(PremiumFeatures.PAY_TO_OPEN_ANGELCHEST)) {
                return 0;
            }
            if (yaml == null) return getPercentagePrice(p, main.getConfig().getString(Config.PRICE_OPEN));
            final Iterator<String> it = groups.keySet().iterator();
            Double bestValueFound = null;
            while (it.hasNext()) {
                final String group = it.next();
                if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
                final String pricePerPlayer = groups.get(group).priceOpen;
                if (pricePerPlayer.equals("-1")) {
                    continue;
                }
                bestValueFound = bestValueFound == null ? getPercentagePrice(p, pricePerPlayer) : Math.min(getPercentagePrice(p, pricePerPlayer), bestValueFound);
            }
            if (bestValueFound != null) {
                return bestValueFound;
            } else {
                return getPercentagePrice(p, main.getConfig().getString(Config.PRICE_OPEN));
            }
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public double getSpawnChancePerPlayer(final Player p) {
        if (!Daddy_Stepsister.allows(PremiumFeatures.SPAWN_CHANCE)) return 1.0;
        if (yaml == null) return main.getConfig().getDouble(Config.SPAWN_CHANCE);
        final Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final double spawnChancePlayer = groups.get(group).spawnChance;
            if (spawnChancePlayer >= 1) return 1;
            if (spawnChancePlayer == -1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? spawnChancePlayer : Math.max(spawnChancePlayer, bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.SPAWN_CHANCE);
        }
    }

    public double getSpawnPricePerPlayer(final Player p) {
        try {
            if (!Daddy_Stepsister.allows(PremiumFeatures.SPAWN_PRICE_PER_PLAYER)) {
                return 0;
            }
            if (yaml == null) return getPercentagePrice(p, main.getConfig().getString(Config.PRICE));
            final Iterator<String> it = groups.keySet().iterator();
            Double bestValueFound = null;
            while (it.hasNext()) {
                final String group = it.next();
                if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
                final String pricePerPlayer = groups.get(group).priceSpawn;
                if (pricePerPlayer.equals("-1")) {
                    continue;
                }
                bestValueFound = bestValueFound == null ? getPercentagePrice(p, pricePerPlayer) : Math.min(getPercentagePrice(p, pricePerPlayer), bestValueFound);
            }
            if (bestValueFound != null) {
                return bestValueFound;
            } else {
                return getPercentagePrice(p, main.getConfig().getString(Config.PRICE));
            }
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public double getTeleportPricePerPlayer(final CommandSender p) {
        try {
            if (yaml == null || !Daddy_Stepsister.allows(PremiumFeatures.TELEPORT_PRICE_PER_PLAYER))
                return getPercentagePrice(p, main.getConfig().getString(Config.PRICE_TELEPORT));
            final Iterator<String> it = groups.keySet().iterator();
            Double bestValueFound = null;
            while (it.hasNext()) {
                final String group = it.next();
                if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
                final String pricePerPlayer = groups.get(group).priceTeleport;
                if (pricePerPlayer.equals("-1")) {
                    continue;
                }
                bestValueFound = bestValueFound == null ? getPercentagePrice(p, pricePerPlayer) : Math.min(getPercentagePrice(p, pricePerPlayer), bestValueFound);
            }
            if (bestValueFound != null) {
                return bestValueFound;
            } else {
                return getPercentagePrice(p, main.getConfig().getString(Config.PRICE_TELEPORT));
            }
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public int getUnlockDurationPerPlayer(final Player p) {
        if (!Daddy_Stepsister.allows(PremiumFeatures.UNLOCK_DURATION_PER_PLAYER)) {
            return -1;
        }
        if (yaml == null) {
            if (main.getConfig().getInt(Config.UNLOCK_DURATION) == 0) return -1;
            return main.getConfig().getInt(Config.UNLOCK_DURATION);
        }
        final Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final int valuePerPlayer = groups.get(group).unlockDuration;
            if (valuePerPlayer == -2) {
                continue;
            }
            if (valuePerPlayer == 0) return -1; // Important! This is different from the other methods!
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return main.getConfig().getInt(Config.UNLOCK_DURATION) == 0 ? -1 : main.getConfig().getInt(Config.UNLOCK_DURATION);
        }
    }

    public double getXPPercentagePerPlayer(final Player p) {
        if (yaml == null) return main.getConfig().getDouble(Config.XP_PERCENTAGE);
        final Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while (it.hasNext()) {
            final String group = it.next();
            if (!p.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
            final double valuePerPlayer = groups.get(group).xpPercentage;
            if (valuePerPlayer == -2) {
                continue;
            }
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if (bestValueFound != null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.XP_PERCENTAGE);
        }
    }

    /*
    TODO: Do this generically some day
     */
//    public int getInt(final CommandSender commandSender, final String node, final GroupCalculationMethod method, final int defaultValue) {
//        if (yaml == null) return defaultValue;
//        final Iterator<String> it = groups.keySet().iterator();
//        Integer bestValueFound = null;
//        while (it.hasNext()) {
//            final String group = it.next();
//            if (!commandSender.hasPermission(Permissions.PREFIX_GROUP + group)) continue;
//            final String pricePerPlayer = groups.get(group).priceFetch;
//            if (pricePerPlayer.equals("-1")) {
//                continue;
//            }
//            bestValueFound = bestValueFound == null ? getPercentagePrice(commandSender, pricePerPlayer) : Math.min(getPercentagePrice(commandSender, pricePerPlayer), bestValueFound);
//        }
//        if (bestValueFound != null) {
//            return bestValueFound;
//        } else {
//            return getPercentagePrice(commandSender, main.getConfig().getString(Config.PRICE_FETCH));
//        }
//    }
//
//    public enum GroupCalculationMethod {
//        LOWEST, HIGHEST
//    }

}
