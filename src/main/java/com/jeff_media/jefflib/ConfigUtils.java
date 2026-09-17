package com.jeff_media.jefflib;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigUtils {

    private ConfigUtils() {
    }

    public static Map<String, Object> asMap(final ConfigurationSection section) {
        final Map<String, Object> result = new HashMap<>();
        for (String key : section.getKeys(false)) result.put(key, section.get(key));
        return result;
    }
}
