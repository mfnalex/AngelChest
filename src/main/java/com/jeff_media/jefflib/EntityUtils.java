package com.jeff_media.jefflib;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EntityUtils {

    private EntityUtils() {
    }

    public static void playTotemAnimation(final Player player, final Integer customModelData) {
        final ItemStack previous = player.getInventory().getItemInMainHand();
        final ItemStack totem = new ItemStack(org.bukkit.Material.TOTEM_OF_UNDYING);
        final ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            totem.setItemMeta(meta);
        }
        player.getInventory().setItemInMainHand(totem);
        player.playEffect(EntityEffect.TOTEM_RESURRECT);
        player.getInventory().setItemInMainHand(previous);
    }
}
