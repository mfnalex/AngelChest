package com.jeff_media.jefflib;

/** Converts durations into Minecraft ticks. */
public final class Ticks {

    private Ticks() {
    }

    public static long fromHours(final double hours) {
        return fromMinutes(hours * 60);
    }

    public static long fromMinutes(final double minutes) {
        return fromSeconds(minutes * 60);
    }

    public static long fromSeconds(final double seconds) {
        return (long) (seconds * 20);
    }
}
