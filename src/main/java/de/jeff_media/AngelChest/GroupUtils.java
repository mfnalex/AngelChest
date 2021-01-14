package de.jeff_media.AngelChest;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class GroupUtils {

    final Main main;
    YamlConfiguration yaml;
    LinkedHashMap<String,Group> groups;
    GroupUtils(Main main, File yamlFile) {
        this.main=main;
        if(!yamlFile.exists()) {
            main.getLogger().info("groups.yml does not exist, skipping custom group settings.");
            return;
        }
        this.yaml=YamlConfiguration.loadConfiguration(yamlFile);
        groups = new LinkedHashMap<>();

        for(String groupName : yaml.getKeys(false)) {
            int angelchestDuration = yaml.getInt(groupName+".angelchest-duration",-1);
            int chestsPerPlayer = yaml.getInt(groupName+".max-allowed-angelchests",-1);
            //System.out.println("Registering group "+groupName);
            groups.put(groupName, new Group(angelchestDuration,chestsPerPlayer));
        }
    }

    int getDurationPerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getInt(Config.ANGELCHEST_DURATION);
        if(main.getConfig().getInt(Config.ANGELCHEST_DURATION)==0) return 0;
        Iterator<String> it = groups.keySet().iterator();
        int bestValueFound = -1;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            //System.out.println(" Player is in group "+group);
            int angelchestDuration = groups.get(group).angelchestDuration;
            if(angelchestDuration==0) return 0;
            bestValueFound = Math.max(angelchestDuration, bestValueFound);
            //System.out.println("best value found: "+bestValueFound);
        }
        return bestValueFound == -1 ? main.getConfig().getInt(Config.ANGELCHEST_DURATION) : bestValueFound;
    }

    int getChestsPerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getInt(Config.MAX_ALLOWED_ANGELCHESTS);
        Iterator<String> it = groups.keySet().iterator();
        int bestValueFound = -1;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            //System.out.println(" Player is in group "+group);
            int chestsPerPlayer = groups.get(group).chestsPerPlayer;
            bestValueFound = Math.max(chestsPerPlayer, bestValueFound);
            //System.out.println("best value found: "+bestValueFound);
        }
        return bestValueFound == -1 ? main.getConfig().getInt(Config.MAX_ALLOWED_ANGELCHESTS) : bestValueFound;
    }

    static class Group {
        final int angelchestDuration;
        final int chestsPerPlayer;

        Group(int angelchestDuration, int chestsPerPlayer) {

            this.angelchestDuration = angelchestDuration;
            this.chestsPerPlayer = chestsPerPlayer;
        }
    }

}
