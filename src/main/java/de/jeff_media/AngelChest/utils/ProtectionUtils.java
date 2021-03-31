package de.jeff_media.AngelChest.utils;

import de.jeff_media.AngelChest.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ProtectionUtils {

    public static boolean playerMayBuildHere(Player p, Location loc) {
        Main main = Main.getInstance();
        BlockPlaceEvent event = new BlockPlaceEvent(loc.getBlock(),loc.getBlock().getState(),loc.getBlock().getRelative(BlockFace.DOWN),new ItemStack(Material.DIRT),p,true, EquipmentSlot.HAND);
        main.getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) {
            main.debug("AngelChest spawn prevented because player "+p.getName()+" is not allowed to place blocks at "+loc.toString());
            return false;
        }
        return true;

    }

}
