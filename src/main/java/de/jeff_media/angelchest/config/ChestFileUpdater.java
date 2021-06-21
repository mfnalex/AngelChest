package de.jeff_media.angelchest.config;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public final class ChestFileUpdater {

    public static void updateChestFilesToNewDeathCause() {
        final Main main = Main.getInstance();

        if (!main.getDataFolder().exists()) return;
        if (!new File(main.getDataFolder(), "angelchests").exists()) return;
        for (final File file : new File(main.getDataFolder(), "angelchests").listFiles()) {
            if (file.getName().equals("shadow")) continue;
            try {
                if (FileUtils.replaceStringsInFile(file, "de.jeff_media.AngelChestPlus.data.DeathCause", "de.jeff_media.AngelChest.data.DeathCause")) {
                    main.getLogger().info("Updated old AngelChest file " + file.getName());
                }
            } catch (final IOException ioException) {
                main.getLogger().severe("There was a problem updating AngelChest file " + file.getName() + ":");
                ioException.printStackTrace();
            }
        }
    }

}
