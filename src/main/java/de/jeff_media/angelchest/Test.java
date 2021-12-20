package de.jeff_media.angelchest;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Test {

    public static int getTotalXP(final @NotNull Player p){
        return getTotalXPRequiredForLevel(p.getExpToLevel()) + getTotalXPRequiredForNextLevel(p.getExpToLevel()+1) * p.getExp();
    }
}
