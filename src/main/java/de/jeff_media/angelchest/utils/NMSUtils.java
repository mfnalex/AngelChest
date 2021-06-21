package de.jeff_media.angelchest.utils;

import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtils {

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        return Class.forName(name);
    }

    public static Class<?> getBukkitNMSClass(final String nmsClassString) throws ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        final String name = "org.bukkit.craftbukkit." + version + nmsClassString;
        return Class.forName(name);
    }

    public static @Nullable Object getConnection(final Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {

        final Method getHandle = player.getClass().getMethod("getHandle");
        final Object nmsPlayer = getHandle.invoke(player);
        final Field conField = nmsPlayer.getClass().getField("playerConnection");
        final Object con = conField.get(nmsPlayer);
        return con;
    }

    public static GameProfile getGameProfile(OfflinePlayer player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player, null);
            return (GameProfile) entityPlayer.getClass().getMethod("getProfile").invoke(entityPlayer, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
