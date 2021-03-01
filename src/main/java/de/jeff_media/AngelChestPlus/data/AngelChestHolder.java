package de.jeff_media.AngelChestPlus.data;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an AngelChest's Inventory
 */
public class AngelChestHolder implements InventoryHolder {

    Inventory inv;

    public void setInventory(Inventory inv) {
        this.inv=inv;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
