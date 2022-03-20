package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.utils.LoreUtils;
import de.jeff_media.jefflib.EnumUtils;
import de.jeff_media.jefflib.PDCUtils;
import de.jeff_media.jefflib.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DeathMapManager {

    private static final Main main = Main.getInstance();

    private static MapCursor.Type getCursorType() {
        MapCursor.Type type = EnumUtils.getIfPresent(MapCursor.Type.class,main.getConfig().getString(Config.DEATH_MAP_MARKER.toUpperCase(Locale.ROOT)))
                .orElse(null);
        if(type == null) {
            type = MapCursor.Type.RED_X;
            main.getLogger().warning("You are using an invalid value for " + Config.DEATH_MAP_MARKER + ". Please see config.yml for valid values. Falling back to RED_X now.");
        }
        return type;
    }

    private static List<String> getLore(AngelChest chest) {
        return LoreUtils.applyNewlines(main.getConfig().getString(Config.DEATH_MAP_LORE)).stream().map(line ->
                TextUtils.format(line.replace("{x}",String.valueOf(chest.getBlock().getX()))
                .replace("{y}",String.valueOf(chest.getBlock().getY()))
                .replace("{z}",String.valueOf(chest.getBlock().getZ())))).collect(Collectors.toList());
    }

    private static String getName() {
        return TextUtils.format(main.getConfig().getString(Config.DEATH_MAP_NAME));
    }

    public static ItemStack getDeathMap(AngelChest chest) {
        ItemStack map = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) map.getItemMeta();
        MapView view = Bukkit.createMap(chest.getWorld());
        view.setScale(MapView.Scale.CLOSE);
        view.setCenterX(chest.getBlock().getX());
        view.setCenterZ(chest.getBlock().getZ());
        view.setUnlimitedTracking(true);
        view.setTrackingPosition(true);
        view.addRenderer(new DeathMapRenderer(getCursorType()));
        meta.setMapView(view);
        PDCUtils.set(meta, NBTTags.DEATH_MAP, PersistentDataType.BYTE,(byte) 1);
        meta.setDisplayName(getName());
        meta.setLore(getLore(chest));
        map.setItemMeta(meta);
        return map;
    }

    public static boolean isDeathMap(ItemStack itemStack) {
        if(itemStack == null) return false;
        if(!itemStack.hasItemMeta()) return false;
        return PDCUtils.has(itemStack,NBTTags.DEATH_MAP,PersistentDataType.BYTE);
    }

    private static class DeathMapRenderer extends MapRenderer {

        private boolean isDone = false;
        private final MapCursor.Type type;

        private DeathMapRenderer(MapCursor.Type type) {
            this.type = type;
        }

        @Override
        public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
            if(isDone) return;
            isDone = true;
            canvas.getCursors().addCursor(new MapCursor((byte)0, (byte)0, (byte)0, type, true));
        }
    }
}
