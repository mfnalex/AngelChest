package com.jeff_media.jefflib.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import org.bukkit.entity.Entity;

public class Cooldown {

    private final Map<Object, Long> cooldowns = new ConcurrentHashMap<>();
    private final TimeUnit precision;
    private final LongSupplier timeSupplier;

    public Cooldown() {
        this(TimeUnit.MILLISECONDS, System::currentTimeMillis);
    }

    public Cooldown(final TimeUnit precision) {
        this(precision, supplierFor(precision));
    }

    public Cooldown(final TimeUnit precision, final LongSupplier timeSupplier) {
        this.precision = precision;
        this.timeSupplier = timeSupplier;
    }

    private static LongSupplier supplierFor(final TimeUnit unit) {
        return switch (unit) {
            case NANOSECONDS -> System::nanoTime;
            case MICROSECONDS -> () -> System.nanoTime() / 1_000L;
            case MILLISECONDS -> System::currentTimeMillis;
            case SECONDS -> () -> System.currentTimeMillis() / 1_000L;
            case MINUTES -> () -> System.currentTimeMillis() / 60_000L;
            case HOURS -> () -> System.currentTimeMillis() / 3_600_000L;
            case DAYS -> () -> System.currentTimeMillis() / 86_400_000L;
        };
    }

    private static Object id(final Object object) {
        return object instanceof Entity entity ? entity.getUniqueId() : object;
    }

    private long now() {
        return timeSupplier.getAsLong();
    }

    public void setCooldown(final Object object, final long duration, final TimeUnit unit) {
        cooldowns.put(id(object), now() + precision.convert(duration, unit));
    }

    public boolean hasCooldown(final Object object) {
        return getCooldownEnd(object) > now();
    }

    public long getCooldownEnd(final Object object) {
        final long end = cooldowns.getOrDefault(id(object), 0L);
        return end > now() ? end : 0;
    }

    public void removeCooldown(final Object object) {
        cooldowns.remove(id(object));
    }

    public void clearOldEntries() {
        final long current = now();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= current);
    }
}
