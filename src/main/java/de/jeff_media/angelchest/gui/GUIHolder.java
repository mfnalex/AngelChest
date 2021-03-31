package de.jeff_media.angelchest.gui;

import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.TeleportAction;
import de.jeff_media.angelchest.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class GUIHolder implements InventoryHolder {

    private final GUIContext context;
    private final ArrayList<AngelChest> chests;
    private final int numberOfAngelChests;

    private final Player player;
    private int chestIdStartingAt1 = 0;
    private TeleportAction action;
    private Inventory inventory;
    private boolean isReadOnlyPreview = false;

    // for use when a player previes another player's chest
    private AngelChest specialAngelChest;

    public GUIHolder(Player player, GUIContext context) {
        this.context = context;
        this.chests = Utils.getAllAngelChestsFromPlayer(player);
        this.numberOfAngelChests = chests.size();
        this.player = player;
    }

    public GUIHolder(Player player, GUIContext context, int chestIdStartingAt1) {
        this(player,context);
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
        if(specialAngelChest != null) return specialAngelChest;
        if(chestIdStartingAt1 == 0) return null;
        return getAngelChests().get(chestIdStartingAt1 -1);
    }

    public @Nullable AngelChest getSpecialAngelChest() {
        return specialAngelChest;
    }

    public boolean isReadOnlyPreview() {
        return isReadOnlyPreview;
    }

    public void setReadOnlyPreview(boolean readOnlyPreview) {
        isReadOnlyPreview = readOnlyPreview;
    }

    public void setAngelChest(AngelChest angelChest) {
        this.specialAngelChest = angelChest;
    }

    @SuppressWarnings("unused")
    public Player getPlayer() {
        return player;
    }

}
