package com.jeff_media.jefflib;

import de.jeff_media.angelchest.AngelChestMain;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/** Minimal in-project scheduler helpers used by AngelChest. */
public final class Tasks {

    private Tasks() {
    }

    private static de.jeff_media.angelchest.AngelChestMain plugin() {
        return Objects.requireNonNull(AngelChestMain.getInstance(), "AngelChest is not initialized");
    }

    public static BukkitTask nextTick(final Runnable task) {
        return Bukkit.getScheduler().runTask(plugin(), task);
    }

    public static BukkitTask later(final Runnable task, final long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin(), task, delay);
    }

    public static void later(final Consumer<BukkitTask> task, final long delay) {
        Bukkit.getScheduler().runTaskLater(plugin(), task, delay);
    }

    public static BukkitTask repeatAsync(final Runnable task, final long initialDelay, final long delay) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin(), task, initialDelay, delay);
    }
}
