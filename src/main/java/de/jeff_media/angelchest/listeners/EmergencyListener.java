package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.EmergencyMode;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.jefflib.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class EmergencyListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent playerJoinEvent) {
        Main mainInstance = Main.getInstance();
        if (mainInstance.invalidConfigFiles == null) return;
        if (playerJoinEvent.getPlayer().isOp()) {
            int i = 0;
            for (final String file : mainInstance.invalidConfigFiles) {
                final String[] text = EmergencyMode.BROKEN_CONFIG_FILE.clone();
                i++;
                Bukkit.getScheduler().scheduleSyncDelayedTask(mainInstance, ()->{
                    for (int j = 0; j < text.length; j++) {
                        text[j] = text[j].replaceAll("\\{filename}", file);
                    }
                    Messages.send(playerJoinEvent.getPlayer(),text);
                }, Ticks.fromSeconds(0.5) + i);
            }
        }
    }

}
