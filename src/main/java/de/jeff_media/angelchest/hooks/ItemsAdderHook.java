package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.AngelChestMain;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemsAdderHook {

    private static final AngelChestMain main = AngelChestMain.getInstance();

    public static void runOnceItemsAdderLoaded(Runnable runnable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (main.isItemsAdderLoaded()) {
                    cancel();
                    runnable.run();
                }
            }
        }.runTaskTimer(main, 1, 1);
    }

}
