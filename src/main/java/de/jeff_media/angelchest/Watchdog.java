package de.jeff_media.angelchest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Watchdog keeps track of holograms and their UIDs. When the server starts, and a
 * rogue hologram has been found, it will be automatically removed. This however does
 * not restore the AngelChest's content.
 * This shouldn't really matter, because holograms can only break when the server
 * crashes, and in that case, the world can easily get corrupted, so a lost AngelChest
 * will be the last problem a server admin will face after a crash.
 */
public final class Watchdog {

    final YamlConfiguration yaml;
    private final Main main;

    public Watchdog(final Main main) {
        this.main = main;
        // If the Watchdog file exists, we have to delete all leftover holograms
        if (getFile().exists()) {
            /*main.getLogger().warning("Found watchdog file at " + getFile().getAbsolutePath());
            main.getLogger().warning("Did the server not shutdown correctly?");
            main.getLogger().warning("Fixing leftover AngelChests ...");*/
            yaml = YamlConfiguration.loadConfiguration(getFile());
            restore();
            //main.getLogger().warning("Done!");
            removeFile();
        } else {
            yaml = new YamlConfiguration();
        }
    }

    /**
     * Returns the amount of unsaved armor stands
     *
     * @return amount of unsaved armor stands
     */
    public int getCurrentUnsavedArmorStands() {
        return main.getAllArmorStandUUIDs().size();
    }

    /**
     * Returns the Watchdog File
     *
     * @return Watchdog File
     */
    private File getFile() {
        return new File(main.getDataFolder() + File.separator + "watchdog");
    }

    /**
     * Removes the Watchdog file
     */
    public void removeFile() {
        if (getFile().exists()) {
            if (!getFile().delete()) {
                main.getLogger().severe("Could not delete file " + getFile().getAbsolutePath());
            }
        }
    }

    /**
     * Removes leftover armor stands.
     * TODO: Also restore the AngelChests
     */
    private void restore() {
        final List<String> leftoverArmorStandUUIDs = yaml.getStringList("armorstands");
        if (main.debug)
            main.debug(String.format("Removing %d leftover armor stands...", leftoverArmorStandUUIDs.size()));
        for (final String entry : leftoverArmorStandUUIDs) {
            final UUID uuid = UUID.fromString(entry);
            final Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof ArmorStand) {
                if (main.debug) main.debug("Removed leftover armor stand " + entry + ": " + entity.getCustomName());
                entity.remove();
            }
        }
    }

    /**
     * Saves the Watchdog file, unless all armor stands have been properly removed already
     * (i.e. graceful shutdown or /acreload)
     */
    public void save() {
        /*if(main.gracefulShutdown) {
            return;
        }*/
        //Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
        final int unsavedArmorStands = getCurrentUnsavedArmorStands();
        if (unsavedArmorStands == 0) {
            if (main.debug) main.debug("Removing watchdog file: 0 unsaved armor stands");
            removeFile();
            return;
        }
        if (main.debug) main.debug("Saving watchdog file: " + unsavedArmorStands + " unsaved armor stands");
        final List<String> list = new ArrayList<>();
        for (final UUID uuid : main.getAllArmorStandUUIDs()) {
            list.add(uuid.toString());
        }
        yaml.set("armorstands", list);
        try {
            yaml.save(getFile());
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }
        //});
    }


}
