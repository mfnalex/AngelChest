package de.jeff_media.AngelChestPlus;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Watchdog {

    private final Main main;
    final YamlConfiguration yaml;

    public Watchdog(Main main) {
        this.main=main;
        if(getFile().exists()) {
            main.getLogger().warning("Found watchdog file at "+getFile().getAbsolutePath());
            main.getLogger().warning("Did the server not shutdown correctly?");
            main.getLogger().warning("Fixing leftover AngelChests ...");
            yaml = YamlConfiguration.loadConfiguration(getFile());
            restore();
            main.getLogger().warning("Done!");
            removeFile();
        } else {
            yaml = new YamlConfiguration();
        }
    }

    private void restore() {
        List<String> leftoverArmorStandUUIDs = yaml.getStringList("armorstands");
        main.debug(String.format("Removing %d leftover armor stands...",leftoverArmorStandUUIDs.size()));
        for(String entry : leftoverArmorStandUUIDs) {
            UUID uuid = UUID.fromString(entry);
            Entity entity = Bukkit.getEntity(uuid);
            if(entity instanceof ArmorStand) {
                main.debug("Removed leftover armor stand "+entry +": "+entity.getCustomName());
                entity.remove();
            }
        }
    }



    public int getCurrentUnsavedArmorStands() {
        return main.getAllArmorStandUUIDs().size();
    }


    public void removeFile() {
        if(getFile().exists()) {
            getFile().delete();
        }
    }

    public void save() {
        if(main.gracefulShutdown) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            int unsavedArmorStands = getCurrentUnsavedArmorStands();
            if (unsavedArmorStands == 0) {
                main.debug("Removing watchdog file: 0 unsaved armor stands");
                removeFile();
                return;
            }
            main.debug("Saving watchdog file: " + unsavedArmorStands + " unsaved armor stands");
            List<String> list = new ArrayList<>();
            for (UUID uuid : main.getAllArmorStandUUIDs()) {
                list.add(uuid.toString());
            }
            yaml.set("armorstands", list);
            try {
                yaml.save(getFile());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    private File getFile() {
        return new File(main.getDataFolder()+File.separator + "wachdog");
    }


}
