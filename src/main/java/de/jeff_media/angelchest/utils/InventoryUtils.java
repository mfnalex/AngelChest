package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class InventoryUtils {

    private static final AngelChestMain main = AngelChestMain.getInstance();

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

    private static final Random rand = new Random();

    public static Collection<ItemStack> removeRandomItemsFromInventory(final PlayerInventory inventory, final int amount, final Location dropLocation, boolean splitStacks) {

        final boolean drop = main.getConfig().getBoolean(Config.RANDOM_ITEM_LOSS_DROP, false);
        final ArrayList<Integer> slots = getNonEmptySlots(inventory);
        final Set<ItemStack> removedItems = new HashSet<>();

        if (splitStacks) {
            return removeSplitStacks(slots, inventory, amount, dropLocation, drop);
        }

        int remaining = amount;


        while (remaining > 0 && !slots.isEmpty()) {
            final int slot = rand.nextInt(slots.size());

            ItemStack tmp = inventory.getItem(slots.get(slot));
            if(main.getConfig().getBoolean(Config.RANDOM_ITEM_LOSS_IGNORES_ENCHANTED_ITEMS)) {
                if(tmp.hasItemMeta()) {
                    remaining--;
                    if(tmp.getItemMeta().hasEnchants()) continue;
                }
            }

            if(main.getConfig().getBoolean(Config.RANDOM_ITEM_LOSS_IGNORES_SHULKERBOXES)) {
                if(tmp.hasItemMeta()) {
                    remaining--;
                    if(isShulkerBox(tmp)) continue;
                }
            }

            // TODO: if splitStacks is true, split the stack and only remove X items

            ItemStack toDrop = inventory.getItem(slots.get(slot)).clone();
            removedItems.add(inventory.getItem(slots.get(slot)));
            inventory.clear(slots.get(slot));
            slots.remove(slot);
            remaining--;
            // random-item-loss-drop
            if(drop) {
                dropLocation.getWorld().dropItemNaturally(dropLocation, toDrop);
            }
        }

        return removedItems;
    }

    private static List<ItemStack> removeSplitStacks(List<Integer> slots, PlayerInventory inventory, int amount, Location dropLocation, boolean drop) {
        List<ItemStack> result = removeSplitStacks0(slots, inventory, amount, dropLocation, drop);
            inventory.setContents(result.toArray(new ItemStack[0]));
        return result;
    }

    private static List<ItemStack> removeSplitStacks0(List<Integer> slots, PlayerInventory inventory, int amount, Location dropLocation, boolean drop) {

        main.debug("Removing "+amount+" items from inventory @ removeSplitStacks");

        List<ItemStack> set = new ArrayList<>();
        int remaining = amount;
        while (remaining-- > 0) {
            final int slot = rand.nextInt(slots.size());
            ItemStack tmp = inventory.getItem(slots.get(slot)).clone();
            main.debug("Adjusting item " + tmp);
            if (tmp == null) continue;
            if (main.getConfig().getBoolean(Config.RANDOM_ITEM_LOSS_IGNORES_ENCHANTED_ITEMS)) {
                if (tmp.hasItemMeta()) {
                    remaining--;
                    if (tmp.getItemMeta().hasEnchants()) continue;
                }
            }

            if (main.getConfig().getBoolean(Config.RANDOM_ITEM_LOSS_IGNORES_SHULKERBOXES)) {
                if (tmp.hasItemMeta()) {
                    remaining--;
                    if (isShulkerBox(tmp)) continue;
                }
            }

            remaining--;

            ItemStack toDrop = tmp.clone();
            toDrop.setAmount(1);
            set.add(toDrop);

            if (drop) {
                dropLocation.getWorld().dropItemNaturally(dropLocation, toDrop.clone());
            }

            ItemStack newItem = tmp.getAmount() == 1 ? null : tmp.clone();
            if (newItem == null) {
                slots.remove(slot);
            } else {
                newItem.setAmount(tmp.getAmount() - 1);
            }

            inventory.setItem(slot, newItem != null ? newItem.clone() : null);

            main.debug("New tmp: " + tmp);
        }

        return set;
    }


    private static boolean isShulkerBox(final ItemStack tmp) {
        if(Tag.SHULKER_BOXES.isTagged(tmp.getType())) {
            return true;
        }
        return false;
    }

    public static int countTotalItems(PlayerInventory inventory) {
        List<ItemStack> list = new ArrayList<>();
        list.addAll(Arrays.asList(inventory.getContents()));
        list.addAll(Arrays.asList(inventory.getArmorContents()));
        list.addAll(Arrays.asList(inventory.getExtraContents()));
        return list.stream().filter(Objects::nonNull).mapToInt(ItemStack::getAmount).sum();
    }
}
