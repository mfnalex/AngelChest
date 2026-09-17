package com.jeff_media.jefflib;

import java.util.function.Supplier;
import org.bukkit.Bukkit;

public final class PluginUtils {

    private PluginUtils() {
    }

    public static <T> T whenInstalled(final String pluginName, final Supplier<T> action, final T fallback) {
        return Bukkit.getPluginManager().getPlugin(pluginName) == null ? fallback : action.get();
    }
}
