package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.data.Graveyard;
import com.jeff_media.jefflib.FileUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraveyardYamlManager {

    private static final Main main = Main.getInstance();
    private static final String HEADER = String.join(System.lineSeparator(), FileUtils.readFileFromResources(main, "graveyards.header"));
    private static final File EXAMPLE_FILE = new File(main.getDataFolder(), "graveyards.example.yml");
    private static final File FILE = new File(main.getDataFolder(), "graveyards.yml");

    public static void init() {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(String.join(System.lineSeparator(), FileUtils.readFileFromResources(main, "graveyards.example.yml")));
            yaml.options().header(HEADER);
            yaml.save(EXAMPLE_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static YamlConfiguration getYaml() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(FILE);
        return yaml;
    }

    private static void save(FileConfiguration newConfig) {
        newConfig.options().header(HEADER);
        try {
            newConfig.save(FILE);
            if(main.debug) {
                main.debug("Saved graveyards.yml file");
                for(String node : newConfig.getKeys(true)) {
                    main.debug(node+" -> " + newConfig.get(node));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        GraveyardManager.init();
    }

    /**
     * Creates a new graveyard.
     *
     * @param name Name
     * @param min  corner1
     * @param max  corner2
     * @return true if saved successfully, false if graveyard with this {@param name} already exists
     */
    public static boolean createGraveyard(String name, Block min, Block max) {
        YamlConfiguration yaml = getYaml();
        if (yaml.isConfigurationSection(name)) {
            return false;
        }
        ConfigurationSection section = yaml.createSection(name);
        section.set("location.world",min.getWorld().getName());
        section.set("location.min.x", min.getX());
        section.set("location.min.y", min.getY());
        section.set("location.min.z", min.getZ());
        section.set("location.max.x", max.getX());
        section.set("location.max.y", max.getY());
        section.set("location.max.z", max.getZ());
        save(yaml);
        return true;
    }


    public static void setMaterial(Graveyard yard, BlockData data) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName() + ".material",data.getAsString());
        save(yaml);
    }

    public static void deleteGraveyard(Graveyard yard) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName(),null);
        save(yaml);
    }

    public static void updateSpawnOn(Graveyard yard) {
        List<String> stringList = new ArrayList<>();
        for(Material mat : yard.getSpawnOn()) {
            stringList.add(mat.name());
        }
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName()+".grave-locations",stringList);
        save(yaml);
    }

    public static void setSpawn(Player player, Graveyard yard) {
        Location location = player.getLocation();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double yaw = location.getYaw();
        double pitch = location.getPitch();
        String world = location.getWorld().getName();
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName()+".spawn.x",x);
        yaml.set(yard.getName()+".spawn.y",y);
        yaml.set(yard.getName()+".spawn.z",z);
        yaml.set(yard.getName()+".spawn.yaw",yaw);
        yaml.set(yard.getName()+".spawn.pitch",pitch);
        yaml.set(yard.getName()+".spawn.world",world);
        save(yaml);

    }

    public static void setHologramText(Graveyard yard, String arg) {
        if(arg!=null) {
            arg = arg.replace("\\n", "\n");
        }
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName()+".hologram-text",arg);
        save(yaml);
    }

    public static void addPotionEffect(Graveyard yard, PotionEffect effect) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName() + ".potion-effects." + effect.getType().getName(),effect.getAmplifier());
        save(yaml);
    }

    public static void removePotionEffect(Graveyard yard, PotionEffectType type) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName()+".potion-effects."+type.getName(),null);
        save(yaml);
    }

    public static void setTime(Graveyard yard, Long time) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName() + ".local-time", time);
        save(yaml);
    }

    public static void setWeather(Graveyard yard, WeatherType weatherType) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName() + ".local-weather", weatherType == WeatherType.CLEAR ? "sun" : "rain");
        save(yaml);
    }

    public static void setInstantRespawn(Graveyard yard, Boolean bool) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName() + ".instant-respawn", bool);
        save(yaml);
    }

    public static void setGlobal(Graveyard yard, Boolean bool) {
        YamlConfiguration yaml = getYaml();
        yaml.set(yard.getName() + ".global", bool);
        save(yaml);
    }
}
