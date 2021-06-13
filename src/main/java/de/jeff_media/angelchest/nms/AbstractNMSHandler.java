package de.jeff_media.angelchest.nms;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.utils.NMSUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public abstract class AbstractNMSHandler {

    protected static final byte TOTEM_MAGIC_VALUE = 35;
    protected static Class<?> CLASS_CRAFTPLAYER;
    protected static Method METHOD_CRAFTPLAYER_GETHANDLE;
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

    public abstract boolean playTotemAnimation(Player p);
}
