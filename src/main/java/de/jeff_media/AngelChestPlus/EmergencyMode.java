package de.jeff_media.AngelChestPlus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EmergencyMode {

    private static final String[] FREE_VERSION_INSTALLED = {
            "§c ",
            "§c§l! ! !W A R N I N G ! ! !",
            "§cYou have installed AngelChestPlus but did not remove the old version.",
            "§cThe plugin will not work correctly until you remove the old AngelChest plugin.",
            "§cMake sure to properly RESTART (NOT RELOAD) your server afterwards!",
            "§cYou do NOT have to remove the old AngelChest config folder, it gets updated automatically."
    };

    public enum EmergencyReason {
        FREE_VERSION_INSTALLED
    }

    public static void run(Main main,EmergencyReason reason) {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            for(String line : FREE_VERSION_INSTALLED) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(line);
                }
                main.getLogger().severe(line);
            }
        },0,30*20L);


    }

}
