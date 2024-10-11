package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.utils.LoreUtils;
import com.jeff_media.jefflib.EnumUtils;
import com.jeff_media.jefflib.PDCUtils;
import com.jeff_media.jefflib.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeathMapManager {

    private static final AngelChestMain main = AngelChestMain.getInstance();

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
        PDCUtils.set(meta, NBTTags.DEATH_MAP, PersistentDataType.BYTE, (byte) 1);
        if (chest instanceof de.jeff_media.angelchest.data.AngelChest) {
            de.jeff_media.angelchest.data.AngelChest angelChestImpl = (de.jeff_media.angelchest.data.AngelChest) chest;
            UUID uuid = angelChestImpl.uniqueId;
            if (uuid != null) {
                PDCUtils.set(meta, NBTTags.DEATH_MAP_UID, PersistentDataType.STRING, uuid.toString());
            }
        }
        meta.setDisplayName(getName());
        meta.setLore(getLore(chest));
        map.setItemMeta(meta);
        return map;
    }

    private static MapCursor.Type getCursorType() {
        String configuredMarker = main.getConfig().getString(Config.DEATH_MAP_MARKER).toUpperCase(Locale.ROOT);
        MapCursor.Type type = EnumUtils.getIfPresent(MapCursor.Type.class, configuredMarker).orElse(null);

        if (type == null) {
            type = MapCursor.Type.RED_X;
            main.getLogger().warning("You are using an invalid value for " + Config.DEATH_MAP_MARKER + ". Please see config.yml for valid values. Falling back to RED_X now.");
        }
        return type;
    }

    private static String getName() {
        return TextUtils.format(main.getConfig().getString(Config.DEATH_MAP_NAME));
    }

    private static List<String> getLore(AngelChest chest) {
        return LoreUtils.applyNewlines(main.getConfig().getString(Config.DEATH_MAP_LORE)).stream().map(
                line -> TextUtils.format(
                        line.replace("{x}", String.valueOf(chest.getBlock().getX()))
                                .replace("{y}", String.valueOf(chest.getBlock().getY()))
                                .replace("{z}", String.valueOf(chest.getBlock().getZ()))
                                .replace("{world}", chest.getWorld().getName())
                )).collect(Collectors.toList());
    }

    public static void removeDeathMap(de.jeff_media.angelchest.data.AngelChest angelChest) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeDeathMap(player, angelChest);
        }
    }

    public static void removeDeathMapsFromChestContents(de.jeff_media.angelchest.data.AngelChest angelChest) {
        removeDeathMap(angelChest.armorInv);
        removeDeathMap(angelChest.extraInv);
        removeDeathMap(angelChest.storageInv);
    }

    private static void removeDeathMap(ItemStack[] items) {
        for(int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) {
                continue;
            }
            if (item.getType() != Material.FILLED_MAP) {
                continue;
            }
            if(isDeathMap(item)) {
                items[i] = null;
            }
        }
    }

    private static void removeDeathMap(Player player, de.jeff_media.angelchest.data.AngelChest angelChest) {
        UUID chestId = angelChest.uniqueId;
        if (chestId == null) return;
        player.getInventory().setStorageContents(removeDeathMap(player.getInventory().getStorageContents(), chestId));
        player.getInventory().setArmorContents(removeDeathMap(player.getInventory().getArmorContents(), chestId));
        player.getInventory().setExtraContents(removeDeathMap(player.getInventory().getExtraContents(), chestId));
    }

    private static ItemStack[] removeDeathMap(ItemStack[] items, UUID chestId) {
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) continue;
            if (!isDeathMap(item)) continue;
            String mapIdString = PDCUtils.getOrDefault(item, NBTTags.DEATH_MAP_UID, PersistentDataType.STRING, "");
            if (mapIdString.isEmpty()) continue;
            UUID mapId = UUID.fromString(mapIdString);
            if (chestId.equals(mapId)) {
                items[i] = null;
            }
        }
        return items;
    }

    public static boolean isDeathMap(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (!itemStack.hasItemMeta()) return false;
        return PDCUtils.has(itemStack, NBTTags.DEATH_MAP, PersistentDataType.BYTE);
    }

    private static class DeathMapRenderer extends MapRenderer {

        private final MapCursor.Type type;
        private boolean isDone = false;

        private DeathMapRenderer(MapCursor.Type type) {
            this.type = type;
        }

        @Override
        public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
            if (isDone) return;
            isDone = true;
            canvas.getCursors().addCursor(new MapCursor((byte) 0, (byte) 0, (byte) 0, type, true));
        }
    }
}
