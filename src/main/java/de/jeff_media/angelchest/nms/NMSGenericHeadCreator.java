package de.jeff_media.angelchest.nms;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;

/**
 * Used to create heads in the world in 1.17+
 */
public class NMSGenericHeadCreator {

    public static void createHeadInWorld(Block block, GameProfile profile) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final World world = (World) block.getWorld().getClass().getMethod("getHandle").invoke(block.getWorld());
        final BlockPosition blockPosition = new BlockPosition(block.getX(),block.getY(),block.getZ());
        final TileEntitySkull skullEntity = (TileEntitySkull) world.getTileEntity(blockPosition);
        skullEntity.setGameProfile(profile);
    }

}
