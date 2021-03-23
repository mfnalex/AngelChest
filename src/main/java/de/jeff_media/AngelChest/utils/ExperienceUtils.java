package de.jeff_media.AngelChest.utils;

public class ExperienceUtils {


	/* This whole Math is kind of broken!
	 * For now we will just disable taking the "full" xp
	 */
	
	/*
    // Calculate amount of EXP needed to level up
    public static int getExpToLevelUp(int level){
        if(level <= 15){
            return 2*level+7;
        } else if(level <= 30){
            return 5*level-38;
        } else {
            return 9*level-158;
        }
    }
 
    // Calculate total experience up to a level
    public static int getExpAtLevel(int level){
        if(level <= 16){
            return (int) (Math.pow(level,2) + 6*level);
        } else if(level <= 31){
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }
 
    // Calculate player's current EXP amount
    public static int getPlayerExp(Player player){
        int exp = 0;
        int level = player.getLevel();
     
        // Get the amount of XP in past levels
        exp += getExpAtLevel(level);
     
        // Get amount of XP towards next level
        exp += Math.round(getExpToLevelUp(level) * player.getExp());
     
        return exp;
    }

  // Give or take EXP
  /*public static int changePlayerExp(Player player, int exp){
   // Get player's current exp
   int currentExp = getPlayerExp(player);
   
   // Reset player's current exp to 0
   player.setExp(0);
   player.setLevel(0);
   
   // Give the player their exp back, with the difference
   int newExp = currentExp + exp;
   player.giveExp(newExp);
   
   // Return the player's new exp amount
   return newExp;
  }*/
}
