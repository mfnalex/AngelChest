package de.jeff_media.angelchest.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public final class HeadCreator {

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
    private static String defaultBase64 = "SSBkb24ndCBsaWtlIHBlb3BsZSB3aG8gdHJ5IHRvIHN0ZWFsIG15IHBsdWdpbnMuIEl0J3Mgb3BlbiBzb3VyY2UgYnJvLCBzbyB3aHkgZG8geW91IGV2ZW4gYm90aGVyIGFib3V0IHNlbmRpbmcgdGhlIC5qYXIgZmlsZSBhd2F5PyBBdCBsZWFzdCBoYXZlIG90aGVyIHBlb3BsZSBjb21waWxlIGl0IHRoZW1zZWx2ZXMuLi4=eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90Z%%__USER__%%Xh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThiYzhmYTcxNmN%%__NONCE__%%hZGQwMDRiODI4Y2IyN2NjMGY2ZjZhZGUzYmU0MTUxMTY4OGNhOWVjZWZmZDE2NDdmYjkifX19SSBkb24ndCBsaWtlIHBlb3BsZSB3aG8gdHJ5IHRvIHN0ZWFsIG15IHBsdWdpbnMuIEl0J3Mgb3BlbiBzb3VyY2UgYnJvLCBzbyB3aHkgZG8geW91IGV2ZW4gYm90aGVyIGFib3V0IHNlbmRpbmcgdGhlIC5qYXIgZmlsZSBhd2F5PyBBdCBsZWFzdCBoYXZlIG90aGVyIHBlb3BsZSBjb21waWxlIGl0IHRoZW1zZWx2ZXMuLi4=";

    @SuppressWarnings("unused")
    public static ItemStack getHead() {
        return getHead(defaultBase64);
    }

    public static ItemStack getHead(String base64) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64));
        Field profileField;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return new ItemStack(Material.PLAYER_HEAD);
        }

        head.setItemMeta(meta);
        return head;
    }

    public static ItemStack getPlayerHead(UUID uuid) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) (head.hasItemMeta() ? head.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD));
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        head.setItemMeta(skullMeta);
        return head;
    }

    public static void createHeadInWorld(Block block, UUID uuid) {
        Main main = Main.getInstance();
        Skull state = (Skull) block.getState();

        // Use the player skin's texture
        if(main.getConfig().getBoolean(Config.HEAD_USES_PLAYER_NAME)) {
            main.debug("Player head = username");
            OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
            state.setOwningPlayer(player);
            state.update();
        }
        // Use a predefined texture
        else {
            main.debug("Player head = base64");
            String base64 = main.getConfig().getString(Config.CUSTOM_HEAD_BASE64);
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", base64));

            try {
                // Some reflection because Spigot cannot place ItemStacks in the world, which ne need to keep the SkullMeta
                // Caching those methods is not important because it only happens once on death

                Object nmsWorld = block.getWorld().getClass().getMethod("getHandle").invoke(block.getWorld());

                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                Class<?> blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
                Class<?> tileEntityClass = Class.forName("net.minecraft.server." + version + ".TileEntitySkull");


                Constructor<?> cons = blockPositionClass.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
                Object blockPosition = cons.newInstance(block.getX(), block.getY(), block.getZ());

                Method getTileEntity = nmsWorld.getClass().getMethod("getTileEntity", blockPositionClass);
                Object tileEntity = tileEntityClass.cast(getTileEntity.invoke(nmsWorld, blockPosition));

                tileEntityClass.getMethod("setGameProfile", GameProfile.class).invoke(tileEntity, profile);

            } catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException | InstantiationException e) {
                main.getLogger().warning("Could not set custom base64 player head.");
            }

        }
    }
}
