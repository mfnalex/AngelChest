package de.jeff_media.AngelChestPlus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Test {

    public static void giveEnchantmentBookToPlayer(Player player) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.addEnchant(Enchantment.MENDING,1,false);
        itemStack.setItemMeta(itemMeta);
        player.getInventory().addItem(itemStack);
    }
}
