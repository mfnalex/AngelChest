package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.AngelChestMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class EnderCrystalListener implements Listener {

    public static UUID lastEnderCrystalKiller = null;
    private final AngelChestMain main = AngelChestMain.getInstance();

    @EventHandler
    public void onKillEndCrystal(final EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Projectile) {
            final Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                damager = (Entity) projectile.getShooter();
            }
        }
        if (damager.getType() != EntityType.PLAYER) return;
        if (event.getEntityType() != EntityType.ENDER_CRYSTAL) return;

        final Player player = (Player) damager;
        if (main.debug) main.debug("" + player.getName() + " is a possible End Crystal killer");
        lastEnderCrystalKiller = player.getUniqueId();
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            lastEnderCrystalKiller = null;
            if (main.debug) main.debug("" + player.getName() + " no longer is a possible End Crystal killer");
        }, 1);

    }


}
