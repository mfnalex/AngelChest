package de.jeff_media.AngelChest.hooks;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import de.jeff_media.AngelChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.Plugin;

public class MinepacksHook {

	boolean disabled = false;
	boolean skipReflection = false;
	MinepacksPlugin minepacks = null;

	public boolean isMinepacksBackpack(ItemStack is,Main main) {
		if(disabled) return false;
		if(is==null) return false;
		if(skipReflection) {
			return minepacks.isBackpackItem(is);
		}
		Plugin minepacksCandidate = Bukkit.getPluginManager().getPlugin("Minepacks");
		if(minepacksCandidate == null) {
			main.debug("Minepacks is not installed");
			disabled = true;
			return false;
		}

		if(!(minepacksCandidate instanceof MinepacksPlugin)) {
			main.getLogger().warning("You are using a version of Minepacks that is too old and does not implement or extend MinecpacksPlugin: "+minepacksCandidate.getClass().getName());
			disabled=true;
			return false;
		}

		minepacks = (MinepacksPlugin) minepacksCandidate;

		try {
			if(minepacks.getClass().getMethod("isBackpackItem", ItemStack.class) != null) {
				skipReflection=true;
				return (minepacks.isBackpackItem(is));
			}
		} catch (NoSuchMethodException | SecurityException e) {
			main.getLogger().warning("You are using a version of Minepacks that is too old and does not implement every API method needed by AngelChest. Minepacks hook will be disabled.");
			disabled=true;
			return false;
		}
		return false;
	}
}