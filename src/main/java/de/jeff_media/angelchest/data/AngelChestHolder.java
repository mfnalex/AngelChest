package de.jeff_media.angelchest.data;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an AngelChest's Inventory
 */
public final class AngelChestHolder implements InventoryHolder {

    Inventory inv;

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    public void setInventory(final Inventory inv) {
        this.inv = inv;
    }
}
