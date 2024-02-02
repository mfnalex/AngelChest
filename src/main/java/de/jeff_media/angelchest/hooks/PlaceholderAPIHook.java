package de.jeff_media.angelchest.hooks;

import com.jeff_media.jefflib.PDCUtils;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.utils.AngelChestUtils;
import de.jeff_media.angelchest.utils.CommandUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Hooks into PlaceholderAPI
 */
public final class PlaceholderAPIHook extends PlaceholderExpansion {

    final AngelChestMain main;

    public PlaceholderAPIHook(final AngelChestMain main) {
        this.main = main;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "mfnalex";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "angelchest";
    }

    @Override
    public @NotNull String getVersion() {
        return "GENERIC";
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link OfflinePlayer OfflinePlayer}.
     * @param identifier A String containing the identifier/value.
     * @return Possibly-null String of the requested identifier.
     */
    @Override
    public String onRequest(final OfflinePlayer player, final String identifier) {

        //UUID uuid = player.getUniqueId();
        final ArrayList<AngelChest> allChests = AngelChestUtils.getAllAngelChestsFromPlayer(player);

        Player onlinePlayer = player.isOnline() ? player.getPlayer() : null;

        switch (identifier) {
            case "price":
                return main.getCurrencyFormatter().format(onlinePlayer == null ? main.getConfig().getDouble(Config.PRICE) : main.groupManager.getSpawnPricePerPlayer(onlinePlayer));
            case "price_teleport":
                return main.getCurrencyFormatter().format(onlinePlayer == null ? main.getConfig().getDouble(Config.PRICE_TELEPORT) : main.groupManager.getTeleportPricePerPlayer(onlinePlayer));
            case "price_fetch":
                return main.getCurrencyFormatter().format(onlinePlayer == null ? main.getConfig().getDouble(Config.PRICE_FETCH) : main.groupManager.getFetchPricePerPlayer(onlinePlayer));
            case "price-open":
            case "price_open":
                return main.getCurrencyFormatter().format(onlinePlayer == null ? main.getConfig().getDouble(Config.PRICE_OPEN) : main.groupManager.getOpenPricePerPlayer(onlinePlayer));
            case "activechests":
                return Integer.toString(allChests.size());
            case "enabled":
                if(!player.isOnline()) {
                    return "false";
                } else {
                    return PDCUtils.has(player.getPlayer(), NBTTags.HAS_ANGELCHEST_DISABLED, PersistentDataType.STRING) ? "false" : "true";
                }
            case "allowed":
                if(!player.isOnline()) {
                    return "false";
                } else {
                    return player.getPlayer().hasPermission(Permissions.USE) ? "true" : "false";
                }

            case "enabled_and_allowed":
                if(!player.isOnline()) {
                    return "false";
                } else {
                    return PDCUtils.has(player.getPlayer(), NBTTags.HAS_ANGELCHEST_DISABLED, PersistentDataType.STRING) ? "false" : player.getPlayer().hasPermission(Permissions.USE) ? "true" : "false";
                }
        }

        final String[] split = identifier.split("_");
        if (split.length != 2) {
            return null;
        }
        Integer id;
        try {
            id = Integer.parseInt(split[1]);
            if (id == null) {
                return null;
            }
        } catch (final Throwable t) {
            return null;
        }
        if (id < 1) return null;
        id--;

        //noinspection SwitchStatementWithTooFewBranches
        switch (split[0]) {
            case "isactive":
                if (id >= allChests.size()) {
                    return "false";
                }
                return "true";
        }

        if (id >= allChests.size()) {
            return "";
        }
        switch (split[0]) {
            case "time":
                return CommandUtils.getTimeLeft(allChests.get(id));
            case "world":
                return allChests.get(id).block.getWorld().getName();
            case "x":
                return Integer.toString(allChests.get(id).block.getX());
            case "y":
                return Integer.toString(allChests.get(id).block.getY());
            case "z":
                return Integer.toString(allChests.get(id).block.getZ());
        }

        // We return null if an invalid placeholder (f.e. %example_placeholder3%)
        // was provided
        return null;
    }
}
