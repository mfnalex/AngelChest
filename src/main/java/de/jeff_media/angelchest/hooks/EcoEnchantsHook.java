package de.jeff_media.angelchest.hooks;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * This is for situations when a player gets killed by another player with the Telekinesis enchantment.
 */
public class EcoEnchantsHook {

    private static final Enchantment telekinesisEnchant;

    static {
        telekinesisEnchant = getTelekinesisEnchant();
    }

    public static boolean dontSpawnChestBecausePlayerWasKilledByTelekinesis(final PlayerDeathEvent event) {
        if(telekinesisEnchant == null) return false;
        Player victim = event.getEntity();
        if(victim.getKiller() == null) return false;
        Player killer = victim.getKiller();
        ItemStack item = killer.getInventory().getItemInMainHand();
        if(item==null || item.getAmount()==0 || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasEnchant(telekinesisEnchant);
    }

    @Nullable
    private static Enchantment getTelekinesisEnchant() {
        return Enchantment.getByKey(NamespacedKey.minecraft("telekinesis"));
    }

}
