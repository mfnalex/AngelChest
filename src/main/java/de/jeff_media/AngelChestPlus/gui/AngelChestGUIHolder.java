package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AngelChestGUIHolder implements InventoryHolder {

    private final GUIContext context;
    private final ArrayList<AngelChest> chests;
    private final int numberOfAngelChests;
    private final Player player;

    public AngelChestGUIHolder(Player player, GUIContext context, Main main) {
        this.context = context;
        this.chests = Utils.getAllAngelChestsFromPlayer(player,main);
        this.numberOfAngelChests = chests.size();
        this.player = player;
    }

    public ArrayList<AngelChest> getAngelChests() {
        return chests;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public int getNumberOfAngelChests() {
        return numberOfAngelChests;
    }

    public @Nullable GUIContext getContext() {
        return context;
    }


}
