package de.jeff_media.angelchest.hooks;

import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class OraxenHook {

    public static @Nullable ItemStack getItemStack(String name) {
        if(!OraxenItems.exists(name)) return null;
        ItemBuilder builder = OraxenItems.getItemById(name);
        return builder.build();
    }

    private static boolean placeFurniture(Block block, String name) {
        try {
            FurnitureFactory factory = (FurnitureFactory) MechanicsManager.getMechanicFactory("furniture");
            if (factory.isNotImplementedIn(name)) {
                return false;
            }
            ((FurnitureMechanic) factory.getMechanic(name)).place(Rotation.NONE, 0, BlockFace.SELF, block.getLocation(), name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean placeNoteBlock(Block block, String name) {
        try {
            NoteBlock data = NoteBlockMechanicFactory.getInstance().createNoteBlockData(name);
            block.setType(Material.NOTE_BLOCK);
            block.setBlockData(data);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean place(Block block, String name) {
        if(placeNoteBlock(block,name)) return true;
        if(placeFurniture(block,name)) return true;
        return false;
    }

}
