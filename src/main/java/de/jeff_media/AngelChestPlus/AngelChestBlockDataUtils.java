package de.jeff_media.AngelChestPlus;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;

public class AngelChestBlockDataUtils {

    protected static BlockFace getBlockDirection(Block b) {
        BlockFace dir;
        try {
            // check for player skull
            dir = ((Rotatable) b.getBlockData()).getRotation();
            dir = dir.getOppositeFace();
        } catch(Exception e) {
            try {
                // check for chest
                dir = ((Directional) b.getBlockData()).getFacing();
            } catch(Exception e2) {
                // Can't get block rotation, probably because it doesn't support it
                return BlockFace.NORTH;
            }
        }
        return dir;
    }

    public static void setBlockDirection(Block b, BlockFace dir) {
        try {
            // check for player skull
            Rotatable blockData = ((Rotatable) b.getBlockData());
            blockData.setRotation(dir.getOppositeFace());
            b.setBlockData(blockData);
        } catch(Exception e) {
            try {
                // check for chest
                Directional blockData = ((Directional) b.getBlockData());
                blockData.setFacing(dir);
                b.setBlockData(blockData);
            } catch(Exception e2) {
                // Can't set block rotation, probably because it doesn't support it
            }
        }
    }

}
