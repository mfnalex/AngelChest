package com.jeff_media.jefflib;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/** Timing and duration helpers required by AngelChest. */
public final class TimeUtils {

    private static final int MILLISECONDS_PER_TICK = 50;
    private static final Map<String, Long> measurements = new HashMap<>();

    private TimeUtils() {
    }

    public static long nanoSecondsToMilliSeconds(final long nanoSeconds) {
        return nanoSeconds / 1_000_000;
    }

    public static double milliSecondsToTickPercentage(final long milliSeconds) {
        return (double) (milliSeconds / MILLISECONDS_PER_TICK * 100);
    }

    public static double nanoSecondsToMilliSecondsDouble(final long nanoSeconds) {
        return (double) nanoSeconds / 1_000_000D;
    }

    public static String formatNanoseconds(final long nanoSeconds) {
        return String.format(Locale.ROOT, "%.4f ms", nanoSecondsToMilliSecondsDouble(nanoSeconds));
    }

    public static void startTimings(final String identifier) {
        measurements.put(identifier, System.nanoTime());
    }

    public static long endTimings(final String identifier) {
        return endTimings(identifier, null, true);
    }

    public static long endTimings(final String identifier, final boolean sendMessage) {
        return endTimings(identifier, null, sendMessage);
    }

    public static long endTimings(final String identifier, final Plugin plugin, final boolean sendMessage) {
        final Long start = measurements.remove(identifier);
        if (start == null) {
            throw new IllegalArgumentException("No timings with identifier \"" + identifier + "\" running");
        }
        final long elapsed = System.nanoTime() - start;
        if (sendMessage) {
            final Logger logger = plugin == null ? Bukkit.getLogger() : plugin.getLogger();
            logger.info(String.format(Locale.ROOT, "Task \"%s\" finished in %.4f ms", identifier, nanoSecondsToMilliSecondsDouble(elapsed)));
        }
        return elapsed;
    }
}
