package de.jeff_media.angelchest.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;


public class TeleportLocationUtils {

    private static final int[][] offsets = {
            {-2,-2}, {-2,-1}, {-2,0}, {-2,1}, {-2,2},
            {-1,-2}, /*{-1,-1}, {-1,0}, {-1,1},*/ {-1,2},
            {0,-2},  /*{0,-1},  {0,0},  {0,1}, */ {0,2},
            {1,-2},  /*{1,-1},  {1,0},  {1,1}, */ {1,2},
            {2,-2},  {2,-1},  {2,0},  {2,1},  {2,2}
    };

    public static Location getFinalTPLocation(Block chest) {
        return null; // TODO
    }
}
