package de.jeff_media.angelchest.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtils {

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        return  Class.forName(name);
    }

    public static Class<?> getBukkitNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + nmsClassString;
        return  Class.forName(name);
    }

    public static @Nullable Object getConnection(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {

            Method getHandle = player.getClass().getMethod("getHandle");
            Object nmsPlayer = getHandle.invoke(player);
            Field conField = nmsPlayer.getClass().getField("playerConnection");
            Object con = conField.get(nmsPlayer);
            return con;

    }

}
