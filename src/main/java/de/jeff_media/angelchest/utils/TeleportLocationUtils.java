package de.jeff_media.angelchest.utils;

import com.jeff_media.jefflib.BlockUtils;
import com.jeff_media.jefflib.VectorUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.function.Predicate;

public class TeleportLocationUtils {

    private static final int[][] offsets = {
            {-2,-2}, {-2,-1}, {-2,0}, {-2,1}, {-2,2},
            {-1,-2}, /*{-1,-1}, {-1,0}, {-1,1},*/ {-1,2},
            {0,-2},  /*{0,-1},  {0,0},  {0,1}, */ {0,2},
            {1,-2},  /*{1,-1},  {1,0},  {1,1}, */ {1,2},
            {2,-2},  {2,-1},  {2,0},  {2,1},  {2,2}
    };

    public static Location getFinalTPLocation(Block chest) {
        Collection<Block> targetLocations = BlockUtils.getBlocksInRadius(chest.getLocation(), 3, BlockUtils.RadiusType.CUBOID, new Predicate<Block>() {
            @Override
            public boolean test(Block block) {
                return false;
            }
        });
        VectorUtils.lookAt(null,null);
        return null; // TODO
    }
}
