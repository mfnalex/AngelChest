package de.jeff_media.AngelChestPlus.utils;

import de.jeff_media.AngelChestPlus.config.Config;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.data.Group;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class GroupUtils {

    final Main main;
    YamlConfiguration yaml;
    LinkedHashMap<String,Group> groups;
    public GroupUtils(File yamlFile) {
        this.main=Main.getInstance();
        if(!yamlFile.exists()) {
            main.getLogger().info("groups.yml does not exist, skipping custom group settings.");
            return;
        }
        this.yaml=YamlConfiguration.loadConfiguration(yamlFile);
        groups = new LinkedHashMap<>();

        for(String groupName : yaml.getKeys(false)) {
            int angelchestDuration = yaml.getInt(groupName+".angelchest-duration",-1);
            int chestsPerPlayer = yaml.getInt(groupName+".max-allowed-angelchests",-1);
            double priceSpawn = yaml.getDouble(groupName+".price-spawn",-1);
            double priceOpen = yaml.getDouble(groupName+".price-open",-1);
            double priceFetch = yaml.getDouble(groupName+".price-fetch",-1);
            double priceTeleport = yaml.getDouble(groupName+".price-teleport",-1);
            double xpPercentage = yaml.getDouble(groupName+".xp-percentage",-2);
            double angelChestSpawnChance = yaml.getInt(groupName+".angelchest-spawn-chance",-1);
            main.debug("Registering group "+groupName);
            Group group = new Group(angelchestDuration,chestsPerPlayer,priceSpawn,priceOpen,priceTeleport,priceFetch, xpPercentage, angelChestSpawnChance);
            groups.put(groupName, group);

        }
    }

    public double getXPPercentagePerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getInt(Config.XP_PERCENTAGE);
        Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            double valuePerPlayer = groups.get(group).xpPercentage;
            if(valuePerPlayer==-2) {
                continue;
            }
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.XP_PERCENTAGE);
        }
    }

    public int getDurationPerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getInt(Config.ANGELCHEST_DURATION);
        Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            int valuePerPlayer = groups.get(group).duration;
            if(valuePerPlayer==-1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getInt(Config.ANGELCHEST_DURATION);
        }
    }

    public int getChestsPerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getInt(Config.MAX_ALLOWED_ANGELCHESTS);
        Iterator<String> it = groups.keySet().iterator();
        Integer bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            int valuePerPlayer = groups.get(group).maxChests;
            if(valuePerPlayer==-1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? valuePerPlayer : Math.max(valuePerPlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getInt(Config.MAX_ALLOWED_ANGELCHESTS);
        }
    }

    public double getSpawnPricePerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getDouble(Config.PRICE);
        Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            double pricePerPlayer = groups.get(group).priceSpawn;
            if(pricePerPlayer==-1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? pricePerPlayer : Math.min(pricePerPlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.PRICE);
        }
    }

    public double getOpenPricePerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getDouble(Config.PRICE_OPEN);
        Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            double pricePerPlayer = groups.get(group).priceOpen;
            if(pricePerPlayer==-1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? pricePerPlayer : Math.min(pricePerPlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.PRICE_OPEN);
        }
    }

    public double getFetchPricePerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getDouble(Config.PRICE_FETCH);
        Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            double pricePerPlayer = groups.get(group).priceFetch;
            if(pricePerPlayer==-1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? pricePerPlayer : Math.min(pricePerPlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.PRICE_FETCH);
        }
    }

    public double getTeleportPricePerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getDouble(Config.PRICE_TELEPORT);
        Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            double pricePerPlayer = groups.get(group).priceTeleport;
            if(pricePerPlayer==-1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? pricePerPlayer : Math.min(pricePerPlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.PRICE_TELEPORT);
        }
    }

    public double getSpawnChancePerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getDouble(Config.ANGELCHEST_SPAWN_CHANCE);
        Iterator<String> it = groups.keySet().iterator();
        Double bestValueFound = null;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            double spawnChancePlayer = groups.get(group).spawnChance;
            if(spawnChancePlayer==-1) {
                continue;
            }
            bestValueFound = bestValueFound == null ? spawnChancePlayer : Math.max(spawnChancePlayer, bestValueFound);
        }
        if(bestValueFound!=null) {
            return bestValueFound;
        } else {
            return main.getConfig().getDouble(Config.ANGELCHEST_SPAWN_CHANCE);
        }
    }

}
