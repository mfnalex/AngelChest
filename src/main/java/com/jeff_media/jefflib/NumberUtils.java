package com.jeff_media.jefflib;

public final class NumberUtils {

    private static final double EPSILON = 1e-6;

    private NumberUtils() {
    }

    public static boolean isZero(final double number) {
        return Math.abs(number) < EPSILON;
    }
}
