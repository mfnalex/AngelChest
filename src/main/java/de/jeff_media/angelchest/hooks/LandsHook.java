
package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.AngelChestMain;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import me.angeschossen.lands.api.player.*;
import me.angeschossen.lands.api.war.*;

import java.util.stream.Collectors;

public class LandsHook {

    private static final AngelChestMain main = AngelChestMain.getInstance();
    private static final me.angeschossen.lands.api.LandsIntegration lands = me.angeschossen.lands.api.LandsIntegration.of(main);

    public static boolean isInWar(Player player1, Player player2) {

        LandPlayer landPlayer1 = lands.getLandPlayer(player1.getUniqueId());
        LandPlayer landPlayer2 = lands.getLandPlayer(player2.getUniqueId());
        if(landPlayer1==null || landPlayer2==null) return false;

        for(War war : landPlayer1.getWars()
                .stream()
                .filter(landPlayer2::isInWar)
                .collect(Collectors.toList())) {
            if(!war.getTeam(landPlayer1).equals(war.getTeam(landPlayer2))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWarDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if(victim.getKiller() == null) return false;
        return isInWar(victim, victim.getKiller());
    }
}

