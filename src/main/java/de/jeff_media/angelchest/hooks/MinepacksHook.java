package de.jeff_media.angelchest.hooks;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import de.jeff_media.angelchest.AngelChestMain;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Hooks into MinePacks
 */
public final class MinepacksHook {

    boolean disabled = false;
    MinepacksPlugin minepacks = null;
    boolean skipReflection = false;

    /**
     * Checks whether an ItemStack is a MinePacks backpack
     *
     * @param is The ItemStack to check
     * @return true if it's a backpack, otherwise false
     */
    public boolean isMinepacksBackpack(final ItemStack is) {
        if (disabled) return false;
        if (is == null) return false;
        if (skipReflection) {
            return minepacks.isBackpackItem(is);
        }
        final Plugin minepacksCandidate = Bukkit.getPluginManager().getPlugin("Minepacks");
        final AngelChestMain main = AngelChestMain.getInstance();
        if (minepacksCandidate == null) {
            if (main.debug) main.debug("Minepacks is not installed");
            disabled = true;
            return false;
        }

        if (!(minepacksCandidate instanceof MinepacksPlugin)) {
            main.getLogger().warning("You are using a version of Minepacks that is too old and does not implement or extend MinecpacksPlugin: " + minepacksCandidate.getClass().getName());
            disabled = true;
            return false;
        }

        minepacks = (MinepacksPlugin) minepacksCandidate;

        try {
            if (minepacks.getClass().getMethod("isBackpackItem", ItemStack.class) != null) {
                skipReflection = true;
                return (minepacks.isBackpackItem(is));
            }
        } catch (final NoSuchMethodException | SecurityException e) {
            main.getLogger().warning("You are using a version of Minepacks that is too old and does not implement every API method needed by AngelChest. Minepacks hook will be disabled.");
            disabled = true;
            return false;
        }
        return false;
    }
}