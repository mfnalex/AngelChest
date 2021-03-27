package de.jeff_media.AngelChest.utils;

public class Ticks {

    public static long fromSeconds(double seconds) {
        return (long) (seconds*20);
    }

    public static long fromMinutes(double minutes) {
        return fromSeconds(minutes*60);
    }

    public static long fromHours(double hours) {
        return fromMinutes(hours*60);
    }

}
