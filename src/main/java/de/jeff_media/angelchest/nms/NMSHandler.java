package de.jeff_media.angelchest.nms;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.utils.NMSUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class NMSHandler {

    protected static final byte TOTEM_MAGIC_VALUE = 35;
    protected static Class<?> CLASS_CRAFTPLAYER;
    protected static Method METHOD_CRAFTPLAYER_GETHANDLE;

    static NMSHandler instance;
    private static final Main main = Main.getInstance();

    static {
        try {
            CLASS_CRAFTPLAYER = NMSUtils.getBukkitNMSClass("entity.CraftPlayer");
            METHOD_CRAFTPLAYER_GETHANDLE = CLASS_CRAFTPLAYER.getMethod("getHandle");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            main.getLogger().severe("Error while getting CraftBukkit NMS class or method:");
            e.printStackTrace();
        }
    }

    public static NMSHandler getInstance() {
        if(instance != null) return instance;

        try {
            instance = new NMSGeneric();
            main.getLogger().info("Loaded generic NMS handler.");
            return instance;
        } catch (Throwable ignored) {

        }

        try {
            instance = new NMSLegacy();
            main.getLogger().info("Loaded legacy NMS handler.");
            return instance;
        } catch (Throwable ignored) {

        }

        instance = new NMSHandler();
        main.getLogger().severe("Could not load any NMS handler. Some features might be disabled.");
        return instance;
    }

    public boolean playTotemAnimation(Player p) {
        return false;
    }
}
