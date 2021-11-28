package de.jeff_media.angelchest.nms;

import com.mojang.authlib.GameProfile;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.utils.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSLegacy extends AbstractNMSHandler {

    /*private final Main main = Main.getInstance();

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

    @Override
    public void createHeadInWorld(Block block, GameProfile profile) {
        try {
            final Object nmsWorld = block.getWorld().getClass().getMethod("getHandle").invoke(block.getWorld());
            final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            final Class<?> blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
            final Class<?> tileEntityClass = Class.forName("net.minecraft.server." + version + ".TileEntitySkull");
            final Constructor<?> cons = blockPositionClass.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
            final Object blockPosition = cons.newInstance(block.getX(), block.getY(), block.getZ());
            final Method getTileEntity = nmsWorld.getClass().getMethod("getTileEntity", blockPositionClass);
            final Object tileEntity = tileEntityClass.cast(getTileEntity.invoke(nmsWorld, blockPosition));
            tileEntityClass.getMethod("setGameProfile", GameProfile.class).invoke(tileEntity, profile);
        } catch (final IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException | InstantiationException e) {
            main.getLogger().warning("Could not set custom base64 player head.");
        }
    }*/
}
