package de.jeff_media.angelchest.nms;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class NMSGeneric extends NMSHandler {

    public boolean playTotemAnimation(Player p) {
        try {
            EntityPlayer entityPlayer = getEntityPlayer(p);
            Packet packet = new PacketPlayOutEntityStatus(entityPlayer,TOTEM_MAGIC_VALUE);

            // TODO: PlayerConnectio field "b" might have another name in the future - still need reflections? -.-
            PlayerConnection playerConnection = entityPlayer.b;
            playerConnection.sendPacket(packet);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private EntityPlayer getEntityPlayer(Player p) {
        try {
            return (EntityPlayer) METHOD_CRAFTPLAYER_GETHANDLE.invoke(p);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
