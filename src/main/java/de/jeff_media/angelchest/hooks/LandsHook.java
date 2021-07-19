package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.Main;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.entity.Player;

public class LandsHook {

    private static final Main main = Main.getInstance();
    private static final LandsIntegration lands = new LandsIntegration(main);

    public static boolean isInWar(Player player1, Player player2) {

        return false;
    }
}
