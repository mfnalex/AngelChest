package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.jefflib.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class BlockDataManager {

    private static final Main main = Main.getInstance();
    private static final YamlConfiguration YAML = new YamlConfiguration();
    private static final File FILE = new File(main.getDataFolder(), "custom-block-data.yml");

    public static void init() {
        if(FILE.exists()) {
            try {
                YAML.load(FILE);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        List<String> headerLines = FileUtils.readFileFromResources(main,"custom-block-data.header");
        YAML.options().header(String.join(System.lineSeparator(),headerLines));
        YAML.options().copyHeader();
    }

    public static void save(BlockData data) {
        YAML.set(data.getMaterial().name(),data.getAsString());
        try {
            YAML.save(FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setBlockData(Block block) {
        Material mat = block.getType();
        Graveyard yard = GraveyardManager.fromBlock(block);
        if(yard != null) {
            if(yard.hasCustomMaterial()) {
                block.setBlockData(yard.getCustomMaterial());
                return;
            }
        }
        if(YAML.contains(mat.name())) {
            block.setBlockData(Bukkit.getServer().createBlockData(Objects.requireNonNull(YAML.getString(mat.name()))));
        }
    }
}
