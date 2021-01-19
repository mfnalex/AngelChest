package de.jeff_media.AngelChestPlus.gui;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.TeleportAction;
import de.jeff_media.AngelChestPlus.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class GUIHolder implements InventoryHolder {

    private final GUIContext context;
    private final ArrayList<AngelChest> chests;
    private final int numberOfAngelChests;
    private final Player player;
    private int chestIdStartingAt1 = 0;
    private TeleportAction action;
    private Inventory inventory;
    private boolean isReadOnlyPreview = false;

    public GUIHolder(Player player, GUIContext context, Main main) {
        this.context = context;
        this.chests = Utils.getAllAngelChestsFromPlayer(player,main);
        this.numberOfAngelChests = chests.size();
        this.player = player;
    }

    public GUIHolder(Player player, GUIContext context, Main main, int chestIdStartingAt1) {
        this(player,context,main);
        this.chestIdStartingAt1 = chestIdStartingAt1;
    }

    public void setAction(TeleportAction action) {
        this.action=action;
    }

    public TeleportAction getAction() {
        return action;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public ArrayList<AngelChest> getAngelChests() {
        return chests;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getNumberOfAngelChests() {
        return numberOfAngelChests;
    }

    public @Nullable GUIContext getContext() {
        return context;
    }

    public int getChestIdStartingAt1() {
        return chestIdStartingAt1;
    }

    public void setChestIdStartingAt1(int chestIdStartingAt1) {
        this.chestIdStartingAt1 = chestIdStartingAt1;
    }

    public @Nullable AngelChest getAngelChest() {
        if(chestIdStartingAt1 == 0) return null;
        return getAngelChests().get(chestIdStartingAt1 -1);
    }

    public boolean isReadOnlyPreview() {
        return isReadOnlyPreview;
    }

    public void setReadOnlyPreview(boolean readOnlyPreview) {
        isReadOnlyPreview = readOnlyPreview;
    }



}
