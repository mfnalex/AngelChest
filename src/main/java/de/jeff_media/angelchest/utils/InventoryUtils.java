package de.jeff_media.angelchest.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class InventoryUtils {

    public static int getAmountOfItemStacks(final PlayerInventory playerInventory) {
        int stacks = 0;

        for (final ItemStack itemStack : playerInventory.getContents()) {
            if (!Utils.isEmpty(itemStack)) {
                stacks++;
            }
        }

        return stacks;
    }

    public static ArrayList<Integer> getNonEmptySlots(final PlayerInventory inventory) {
        final ArrayList<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!Utils.isEmpty(inventory.getItem(i))) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static Set<ItemStack> removeRandomItemsFromInventory(final PlayerInventory inventory, final int amount) {
        final ArrayList<Integer> slots = getNonEmptySlots(inventory);
        final Set<ItemStack> removedItems = new HashSet<>();

        int remaining = amount;
        final Random rand = new Random();

        while (remaining > 0 && !slots.isEmpty()) {
            final int slot = rand.nextInt(slots.size());
            removedItems.add(inventory.getItem(slots.get(slot)));
            inventory.clear(slots.get(slot));
            slots.remove(slot);
            remaining--;
        }

        return removedItems;
    }

}
