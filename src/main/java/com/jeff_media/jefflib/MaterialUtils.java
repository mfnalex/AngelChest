package com.jeff_media.jefflib;

import org.bukkit.Material;

public final class MaterialUtils {

    private MaterialUtils() {
    }

    public static String getNiceMaterialName(final Material material) {
        final String[] words = material.name().toLowerCase(java.util.Locale.ROOT).split("_");
        final StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
