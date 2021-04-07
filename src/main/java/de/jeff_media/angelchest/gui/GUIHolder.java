package de.jeff_media.angelchest.gui;

import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.CommandAction;
import de.jeff_media.angelchest.utils.AngelChestUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class GUIHolder implements InventoryHolder {

    private final ArrayList<AngelChest> chests;
    private final GUIContext context;
    private final int numberOfAngelChests;

    private final Player player;
    private CommandAction action;
    private int chestIdStartingAt1 = 0;
    private Inventory inventory;
    private boolean isReadOnlyPreview = false;

    // for use when a player previes another player's chest
    private AngelChest specialAngelChest;

    public GUIHolder(final Player player, final GUIContext context) {
        this.context = context;
        this.chests = AngelChestUtils.getAllAngelChestsFromPlayer(player);
        this.numberOfAngelChests = chests.size();
        this.player = player;
    }

    public GUIHolder(final Player player, final GUIContext context, final int chestIdStartingAt1) {
        this(player, context);
        this.chestIdStartingAt1 = chestIdStartingAt1;
    }

    public CommandAction getAction() {
        return action;
    }

    public void setAction(final CommandAction action) {
        this.action = action;
    }

    public @Nullable AngelChest getAngelChest() {
        if (specialAngelChest != null) return specialAngelChest;
        if (chestIdStartingAt1 == 0) return null;
        return getAngelChests().get(chestIdStartingAt1 - 1);
    }

    public void setAngelChest(final AngelChest angelChest) {
        this.specialAngelChest = angelChest;
    }

    public ArrayList<AngelChest> getAngelChests() {
        return chests;
    }

    public int getChestIdStartingAt1() {
        return chestIdStartingAt1;
    }

    public void setChestIdStartingAt1(final int chestIdStartingAt1) {
        this.chestIdStartingAt1 = chestIdStartingAt1;
    }

    public @Nullable GUIContext getContext() {
        return context;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(final Inventory inventory) {
        this.inventory = inventory;
    }

    public int getNumberOfAngelChests() {
        return numberOfAngelChests;
    }

    @SuppressWarnings("unused")
    public Player getPlayer() {
        return player;
    }

    public @Nullable AngelChest getSpecialAngelChest() {
        return specialAngelChest;
    }

    public boolean isReadOnlyPreview() {
        return isReadOnlyPreview;
    }

    public void setReadOnlyPreview(final boolean readOnlyPreview) {
        isReadOnlyPreview = readOnlyPreview;
    }

}
