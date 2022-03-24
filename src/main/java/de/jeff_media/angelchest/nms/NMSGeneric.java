package de.jeff_media.angelchest.nms;

/**
 * Does NMS stuff for 1.17+
 */
public class NMSGeneric extends AbstractNMSHandler {

    /*public boolean playTotemAnimation(Player p) {
        try {
            EntityPlayer entityPlayer = getEntityPlayer(p);
            Packet packet = new PacketPlayOutEntityStatus(entityPlayer, TOTEM_MAGIC_VALUE);

            // TODO: PlayerConnectio field "b" might have another name in the future - still need reflections? -.-
            PlayerConnection playerConnection = entityPlayer.b;
            playerConnection.sendPacket(packet);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/

    /*@Override
    public void createHeadInWorld(Block block, GameProfile profile) {
        try {
            final World world = (World) block.getWorld().getClass().getMethod("getHandle").invoke(block.getWorld());
            final BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
            final TileEntitySkull skullEntity = (TileEntitySkull) world.getTileEntity(blockPosition);
            skullEntity.setGameProfile(profile);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }*/

    /*private EntityPlayer getEntityPlayer(Player p) {
        try {
            return (EntityPlayer) METHOD_CRAFTPLAYER_GETHANDLE.invoke(p);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }*/
}
