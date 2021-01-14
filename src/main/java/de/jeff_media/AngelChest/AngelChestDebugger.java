package de.jeff_media.AngelChest;

import org.bukkit.Bukkit;

public class AngelChestDebugger {


    final Main main;

    public AngelChestDebugger(Main main) {
        this.main=main;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this::printBlockArmorstandCombination,0,100L);

    }

    void printBlockArmorstandCombination() {
        for(BlockArmorStandCombination comb : main.blockArmorStandCombinations) {

            main.debug(comb.block.getLocation().toString()+" -> "+comb.armorStand.getUniqueId());

        }
    }
}
