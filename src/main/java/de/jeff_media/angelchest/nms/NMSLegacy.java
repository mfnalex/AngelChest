package de.jeff_media.angelchest.nms;

import de.jeff_media.angelchest.utils.NMSUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NMSLegacy extends NMSHandler {
    @Override
    public boolean playTotemAnimation(Player p) {
        try {
            final Class<?> statusPacketClass = NMSUtils.getNMSClass("PacketPlayOutEntityStatus");
            final Class<?> entityPlayerClass = NMSUtils.getNMSClass("EntityPlayer");
            final Class<?> entityClass = NMSUtils.getNMSClass("Entity");
            final Class<?> craftPlayerClass = NMSUtils.getBukkitNMSClass("entity.CraftPlayer");
            final Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
            final Method sendPacketMethod = NMSUtils.getNMSClass("PlayerConnection").getMethod("sendPacket", NMSUtils.getNMSClass("Packet"));
            final Constructor<?> packetConstructor = statusPacketClass.getConstructor(entityClass, byte.class);
            final Object craftPlayer = craftPlayerClass.cast(p);
            final Object entityPlayer = getHandleMethod.invoke(craftPlayer, null);
            final Object packet = packetConstructor.newInstance(entityPlayerClass.cast(entityPlayer), TOTEM_MAGIC_VALUE);
            sendPacketMethod.invoke(NMSUtils.getConnection(p), packet);
            return true;

        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
