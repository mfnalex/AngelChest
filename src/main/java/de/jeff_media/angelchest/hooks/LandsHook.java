
package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.Main;
import me.angeschossen.lands.api.MemberHolder;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.LandPlayer;
import me.angeschossen.lands.api.war.War;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.stream.Collectors;

public class LandsHook {

    private static final Main main = Main.getInstance();
    private static final LandsIntegration lands = new LandsIntegration(main);

    public static boolean isInWar(Player player1, Player player2) {

        LandPlayer landPlayer1 = lands.getLandPlayer(player1.getUniqueId());
        LandPlayer landPlayer2 = lands.getLandPlayer(player2.getUniqueId());
        if(landPlayer1==null || landPlayer2==null) return false;

        for(War war : landPlayer1.getWars()
                .stream()
                .filter(landPlayer2::isInWar)
                .collect(Collectors.toList())) {
            if(!war.getTeam(landPlayer1.getPlayer()).equals(war.getTeam(landPlayer2.getPlayer()))) {
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

