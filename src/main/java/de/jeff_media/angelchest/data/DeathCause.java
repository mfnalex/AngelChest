package de.jeff_media.angelchest.data;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class DeathCause implements ConfigurationSerializable {

    private final EntityDamageEvent.DamageCause damageCause;
    private String killerName;

    public DeathCause(final EntityDamageEvent entityDamageEvent) {
        final Main main = Main.getInstance();

        // Some plugins do strange stuff and kill players without EntityDamageEvent
        if (entityDamageEvent == null) {
            damageCause = EntityDamageEvent.DamageCause.CUSTOM;
            this.killerName = null;
            return;
        }

        this.damageCause = entityDamageEvent.getCause();
        this.killerName = null;
        final Entity victim = entityDamageEvent.getEntity();
        Entity killer = main.killers.get(victim.getUniqueId());

        if (killer != null) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (killer.getType()) {
                case PRIMED_TNT:
                    killerName = "TNT";
                    return;
            }
        }

        if (killer instanceof Projectile) {
            final Projectile projectile = (Projectile) killer;
            if (projectile.getShooter() instanceof Entity) {
                killer = (Entity) projectile.getShooter();
                if (killer.getUniqueId().equals(victim.getUniqueId())) {
                    this.killerName = victim.getName();
                    return;
                }
            } else if (projectile.getShooter() instanceof BlockProjectileSource) {
                final BlockProjectileSource blockProjectileSource = (BlockProjectileSource) projectile.getShooter();
                this.killerName = blockProjectileSource.getBlock().getType().toString();
                return;
            }
        }

        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            this.killerName = killer.getType().name();
            if (killer.getCustomName() != null && !killer.getCustomName().equals("")) {
                this.killerName = killer.getCustomName();
            }
            if (killer.getType() == EntityType.PLAYER) {
                this.killerName = killer.getName();
            }
        }
    }

    public DeathCause(final EntityDamageEvent.DamageCause damageCause, @Nullable final String killerName) {
        this.damageCause = damageCause;
        this.killerName = killerName;
    }

    @SuppressWarnings("unused")
    public static DeathCause deserialize(final Map<String, Object> map) {
        final EntityDamageEvent.DamageCause damageCause = Enums.getIfPresent(EntityDamageEvent.DamageCause.class, (String) map.get("damageCause")).or(EntityDamageEvent.DamageCause.VOID);
        final String killer = (String) map.get("killer");
        return new DeathCause(damageCause, killer);
    }

    public String getText() {
        if (killerName != null) {
            return killerName;
        }
        return damageCause.name();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("damageCause", damageCause.toString());
        map.put("killer", killerName);
        return map;
    }
}
