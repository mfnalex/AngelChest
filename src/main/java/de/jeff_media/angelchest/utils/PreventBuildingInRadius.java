package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Permissions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class PreventBuildingInRadius {

    private final AngelChestMain main;

    public PreventBuildingInRadius(AngelChestMain main) {
        this.main = main;
    }

    public enum BuildType {
        PLACING, BREAKING;

        public double getRadius() {
            switch(this) {
                case PLACING:
                    return AngelChestMain.getInstance().getConfig().getDouble(Config.PREVENT_BUILDING_IN_RADIUS);
                case BREAKING:
                    return AngelChestMain.getInstance().getConfig().getDouble(Config.PREVENT_BREAKING_IN_RADIUS);
                default:
                    return 0;
            }
        }
    }

    public boolean canBuild(Player player, Block block, BuildType type) {
        if(player.hasPermission(Permissions.IGNORE_PREVENT_BUILDING)) {
            return true;
        }

        double radius = type.getRadius();
        if(radius <= 0) {
            return true;
        }

        Vector vec = block.getLocation().toVector();

        for(AngelChest ac : main.angelChests) {
            if(ac.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            if(!ac.isProtected()) {
                if(main.getConfig().getBoolean(Config.ONLY_PREVENT_BUILDING_IN_RADIUS_FOR_PROTECTED_CHESTS)) continue;
            }

            Block chestBlock = ac.getBlock();

            if(!block.getWorld().equals(chestBlock.getWorld())) continue;
            BoundingBox box = BoundingBox.of(chestBlock).expand(radius);
            if(box.contains(vec)) {
                return false;
            }
        }

        return true;
    }
}
