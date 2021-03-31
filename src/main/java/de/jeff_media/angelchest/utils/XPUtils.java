package de.jeff_media.angelchest.utils;

public final class XPUtils {

    // https://minecraft.gamepedia.com/Experience#Leveling_up

    public static int getXPRequiredForNextLevel(int currentLevel) {
        if(currentLevel <= 15) return (2*currentLevel) + 7;
        if(currentLevel <= 30) return (5*currentLevel) - 38;
        return (9*currentLevel) - 158;
    }

    public static int getTotalXPRequiredForLevel(int targetLevel) {
        if(targetLevel <= 16) return sqrt(targetLevel) + (6*targetLevel);
        if(targetLevel <= 31) return (int) ((2.5*sqrt(targetLevel)) - (40.5*targetLevel) + 360);
        return (int) ((4.5*sqrt(targetLevel)) - (162.5*targetLevel) + 2220);
    }

    public static String xpToString(int xp) {
        return String.format("%d XP",xp);
    }

    /*
    public static int getTotalXPRequiredForLevelRecursive(int targetLevel) {
        int totalXP = 0;
        for(int i = 0; i< targetLevel; i++) {
            totalXP += getXPRequiredForNextLevel(i);
        }
        return totalXP;
    }*/

    private static int sqrt(int a) {
        return a*a;
    }

}
