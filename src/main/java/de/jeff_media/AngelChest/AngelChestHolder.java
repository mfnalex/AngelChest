package de.jeff_media.AngelChest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class AngelChestHolder implements InventoryHolder {

    Inventory inv;

    void setInventory(Inventory inv) {
        this.inv=inv;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
