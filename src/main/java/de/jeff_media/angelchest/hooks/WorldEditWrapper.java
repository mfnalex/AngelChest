package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.data.WorldBoundingBox;
import org.bukkit.entity.Player;

public class WorldEditWrapper {

    public static WorldBoundingBox getSelection(Player player) {

        try {
            return WorldEditHandler.getSelection(player);
        } catch (Throwable throwable) {
            return null;
        }
    }

}
