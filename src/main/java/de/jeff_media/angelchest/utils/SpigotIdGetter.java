package de.jeff_media.angelchest.utils;

public class SpigotIdGetter {

    public static String getSpigotId() {
        String intern = "%%__USER__%%";
        return intern.startsWith("%") ? "()" : "(" + "%%__USER__%%" + ")";
    }

}
