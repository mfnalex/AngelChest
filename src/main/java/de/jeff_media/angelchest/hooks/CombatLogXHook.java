package de.jeff_media.angelchest.hooks;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CombatLogXHook {

    private static final ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
    private static final ICombatManager combatManager = plugin.getCombatManager();

    public static boolean isInCombat(Player player) {
        return combatManager.isInCombat(player);
    }
}
