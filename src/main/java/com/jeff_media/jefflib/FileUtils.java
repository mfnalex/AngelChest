package com.jeff_media.jefflib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.plugin.Plugin;

public final class FileUtils {

    private FileUtils() {
    }

    public static boolean saveResourceIfNotExists(final String filename) {
        final Plugin plugin = Objects.requireNonNull(de.jeff_media.angelchest.AngelChestMain.getInstance(), "AngelChest is not initialized");
        if (new java.io.File(plugin.getDataFolder(), filename).exists()) return false;
        plugin.saveResource(filename, false);
        return true;
    }

    public static List<String> readFileFromResources(final Plugin plugin, final String filename) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(plugin.getResource(filename)), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read resource " + filename, exception);
        }
    }
}
