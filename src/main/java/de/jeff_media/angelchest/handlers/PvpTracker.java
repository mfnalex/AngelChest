package de.jeff_media.angelchest.handlers;

import com.jeff_media.jefflib.NumberUtils;
import de.jeff_media.angelchest.AngelChestMain;
import com.jeff_media.jefflib.data.Cooldown;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PvpTracker implements Listener {

    private final AngelChestMain main;
    private final Supplier<Double> cooldownSupplier;

    @Getter private final Cooldown tracker = new Cooldown(TimeUnit.MILLISECONDS);

    public PvpTracker(AngelChestMain main, Supplier<Double> cooldownSupplier) {
        this.main = main;
        this.cooldownSupplier = cooldownSupplier;

        Bukkit.getPluginManager().registerEvents(this, main);
    }

    private void registerHit(UUID victim) {
        tracker.setCooldown(victim, (int) (cooldownSupplier.get() * 1000), TimeUnit.MILLISECONDS);
    }

    public boolean isPvp(Player victim) {
        if(victim.isDead() || victim.getHealth() <= 0.000001) {
            EntityDamageEvent event = victim.getLastDamageCause();
            if(event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
                if(isPvp(event2)) {
                    return true;
                }
            }
        }
        return tracker.hasCooldown(victim.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if(isPvp(event)) {
            registerHit(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        tracker.setCooldown(event.getEntity().getUniqueId(), 0, TimeUnit.MILLISECONDS);
    }

    private boolean isPvp(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player)) return false;
        Player victim = (Player) event.getEntity();

        // Was killed by player right now
        if(victim.isDead() || NumberUtils.isZero(victim.getHealth())) {
            if(victim.getKiller() != null) return true;
        }

        // Was damaged by player right now
        if(event.getDamager() instanceof Player) {
            return true;
        }

        // Was damaged by projectile right now
        if(event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            // Player shot the projectile
            if(projectile.getShooter() instanceof Player) {
                return true;
            }
        }

        return false;
    }
    

}
