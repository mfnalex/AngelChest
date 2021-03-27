package de.jeff_media.AngelChest;

import de.jeff_media.AngelChest.utils.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EmergencyMode {

    public static final String[] FREE_VERSION_INSTALLED = {
            "§c ",
            "§c§l! ! !W A R N I N G ! ! !",
            "§cYou have installed AngelChest "+Main.getInstance().getDescription().getVersion()+" but did not remove the old version.",
            "§cThe plugin will not work correctly until you remove the old .jar file.",
            "§cMake sure to properly RESTART (NOT RELOAD) your server afterwards!",
            "§cYou do NOT have to remove the old AngelChest config folder, it gets updated automatically."
    };

    public static final String[] UNSUPPORTED_MC_VERSION_1_12 = {
            "§c ",
            "§c§l! ! !W A R N I N G ! ! !",
            "§cAngelChest " + Main.getInstance().getDescription().getVersion()+" is only compatible with Minecraft versions 1.13 and newer.",
            "§cPlease use AngelChest version 2.22.2 for Minecraft 1.12:",
            "§chttps://www.spigotmc.org/resources/1-12-1-16-angelchest.60383/history"
    };

    public static final String[] BROKEN_CONFIG_FILE = {
            "§c ",
            "§c§l! ! !W A R N I N G ! ! !",
            "§cYour AngelChest config file \"{filename}\" is broken!",
            "§cThis can happen if you messed up the YAML syntax while editing the file,",
            "§cor when you used an editor that does not support UTF8.",
            "§cPlease do NOT use online / web editors to edit the file.",
            "§cPlease delete the file and use a fresh copy to start editing it again.",
            "§cYou can validate YAML files here: http://www.yamllint.com/",
            "§cContact me on Discord if you need help:",
            "§c-> https://discord.jeff-media.de <-",
            "§c",
            "§c(This message is only shown to server operators)"
    };

    public static void warnBrokenConfig() {

        if(Main.getInstance().invalidConfigFiles==null) return;

        String[] text = BROKEN_CONFIG_FILE.clone();

        for(String file : Main.getInstance().invalidConfigFiles) {
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.isOp()) {
                    for(String line : text) {
                        Main.getInstance().getLogger().warning(line.replaceAll("\\{filename}",file));
                    }
                }
            }
            for (String line : text) {
                Main.getInstance().getLogger().warning(line.replaceAll("\\{filename}",file));
            }
        }
    }


    public static void severe(String[] text) {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(text);
            }
            for(String line : text) {
                Main.getInstance().getLogger().severe(line);
            }
        },0,Ticks.fromSeconds(30));

        Bukkit.getPluginManager().registerEvent(
                PlayerJoinEvent.class,
                new Listener() { },
                EventPriority.MONITOR,
                ((listener, event) -> {
                    PlayerJoinEvent playerJoinEvent = (PlayerJoinEvent) event;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> playerJoinEvent.getPlayer().sendMessage(text), Ticks.fromSeconds(0.5));
                }),
                Main.getInstance());


    }

}
