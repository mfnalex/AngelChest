package com.jeff_media.jefflib;

import java.util.Collection;
import java.util.Map;

public final class DebugUtils {

    private DebugUtils() {
    }

    public static void print(final Map<?, ?> map) {
        map.forEach((key, value) -> System.out.println(key + " -> " + value));
    }

    public static void print(final Collection<?> collection) {
        collection.forEach(System.out::println);
    }
}
