package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.Group;
import de.jeff_media.angelchest.enums.EconomyStatus;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

public final class GroupUtils {

    final Main main;
    LinkedHashMap<String, Group> groups;
    YamlConfiguration yaml;

    public GroupUtils(final File yamlFile) {
        this.main = Main.getInstance();
        if (!yamlFile.exists()) {
            main.getLogger().info("groups.yml does not exist, skipping custom group settings.");
            return;
        }
        this.yaml = YamlConfiguration.loadConfiguration(yamlFile);
        groups = new LinkedHashMap<>();

        final String DOT = ".";
        for (final String groupName : yaml.getKeys(false)) {
            final int angelchestDuration = yaml.getInt(groupName + DOT + Config.ANGELCHEST_DURATION, -1);
            final int chestsPerPlayer = yaml.getInt(groupName + DOT + Config.MAX_ALLOWED_ANGELCHESTS, -1);
            final String priceSpawn = yaml.getString(groupName + DOT + Config.PRICE, "-1");
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

            if(main.debug) main.debug("Registering group " + groupName);
            final Group group = new Group(angelchestDuration, chestsPerPlayer, priceSpawn, priceOpen, priceTeleport, priceFetch, xpPercentage, unlockDuration, spawnChance, itemLoss, invulnerabilityAfterTP, allowTpAcrossWorlds, allowFetchAcrossWorlds, maxTpDistance, maxFetchDistance);

            groups.put(groupName, group);

        }
    }

    private static int getPercentageItemLoss(final Player p, final String value) {
        final Main main = Main.getInstance();
        if (value.endsWith("p")) {
            if (!Daddy.allows(PremiumFeatures.RANDOM_ITEM_LOSS)) {
                main.getLogger().warning("You are using percentage random-item-loss in your config file. This is only available in AngelChestPlus. See here: " + Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
                return 0;
            }
            final double percentage = Double.parseDouble(value.substring(0, value.length() - 1));
            if (percentage <= 0) {
                return 0;
            }
            final int result = (int) (InventoryUtils.getAmountOfItemStacks(p.getInventory()) * percentage);
            if(main.debug) main.debug("GroupUtils -> Item Loss -> " + value + " contains a p, getting percentage for player " + p.getName() + ": " + result);
            return result;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static double getPercentagePrice(final CommandSender commandSender, final String value) {
        final Main main = Main.getInstance();
        if (value.endsWith("p")) {
            if (!Daddy.allows(PremiumFeatures.SET_PRICES_AS_PERCENTAGE)) {
                main.getLogger().warning("You are using percentage prices in your config file. This is only available in AngelChestPlus. See here: " + Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
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
            if(main.debug) main.debug(value + " contains a p, getting percentage for player " + commandSender.getName() + ": " + result);
            return result;
        } else {
            return Double.parseDouble(value);
        }
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
        if(yaml == null) return main.getConfig().getBoolean(Config.ALLOW_TP_ACROSS_WORLDS);
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

    public int getMaxFetchDistance(final CommandSender commandSender) {
        final int result = getMaxFetchDistancePremium(commandSender);
        if (!Daddy.allows(PremiumFeatures.MAX_TP_FETCH_DISTANCE)) {
            Messages.sendPremiumOnlyConsoleMessage(Config.MAX_FETCH_DISTANCE);
            return 0;
        }
        return result;
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
        if (!Daddy.allows(PremiumFeatures.MAX_TP_FETCH_DISTANCE)) {
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

    public double getFetchPricePerPlayer(final CommandSender commandSender) {
        if (yaml == null || !Daddy.allows(PremiumFeatures.FETCH_PRICE_PER_PLAYER))
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
        if (yaml == null || !Daddy.allows(PremiumFeatures.RANDOM_ITEM_LOSS))
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

    public double getOpenPricePerPlayer(final Player p) {
        if (!Daddy.allows(PremiumFeatures.PAY_TO_OPEN_ANGELCHEST)) {
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
    }

    public double getSpawnChancePerPlayer(final Player p) {
        if (!Daddy.allows(PremiumFeatures.SPAWN_CHANCE)) return 1.0;
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
        if (!Daddy.allows(PremiumFeatures.SPAWN_PRICE_PER_PLAYER)) {
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
    }

    public double getTeleportPricePerPlayer(final CommandSender p) {
        if (yaml == null || !Daddy.allows(PremiumFeatures.TELEPORT_PRICE_PER_PLAYER))
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
    }

    public int getUnlockDurationPerPlayer(final Player p) {
        if (!Daddy.allows(PremiumFeatures.UNLOCK_DURATION_PER_PLAYER)) {
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
            return main.getConfig().getInt(Config.UNLOCK_DURATION);
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
