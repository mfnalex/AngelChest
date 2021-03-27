package de.jeff_media.AngelChest.listeners;

import de.jeff_media.AngelChest.EmergencyMode;
import de.jeff_media.AngelChest.Main;
import de.jeff_media.AngelChest.utils.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EmergencyListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {

        if(Main.getInstance().invalidConfigFiles==null) return;

        if (playerJoinEvent.getPlayer().isOp()) {
            int i = 0;
            for (String file : Main.getInstance().invalidConfigFiles) {
                String[] text = EmergencyMode.BROKEN_CONFIG_FILE.clone();
                i++;
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                    for (int j = 0; j < text.length; j++) {
                        text[j] = text[j].replaceAll("\\{filename}", file);
                    }
                    playerJoinEvent.getPlayer().sendMessage(text);
                }, Ticks.fromSeconds(0.5) + i);
            }
        }
    }

}
