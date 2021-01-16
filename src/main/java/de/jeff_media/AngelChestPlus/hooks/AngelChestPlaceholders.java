package de.jeff_media.AngelChestPlus.hooks;

import de.jeff_media.AngelChestPlus.*;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
import de.jeff_media.AngelChestPlus.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class AngelChestPlaceholders extends PlaceholderExpansion {

    final Main main;

    public AngelChestPlaceholders(Main main) {
        this.main=main;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "angelchest";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mfnalex";
    }

    @Override
    public @NotNull String getVersion() {
        return "GENERIC";
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return Possibly-null String of the requested identifier.
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier){

        UUID uuid = player.getUniqueId();
        ArrayList<AngelChest> allChests = Utils.getAllAngelChestsFromPlayer(player,main);

        switch (identifier) {
            case "price":
                return Double.toString(main.getConfig().getDouble(Config.PRICE));
            case "price_teleport":
                return Double.toString(main.getConfig().getDouble(Config.PRICE_TELEPORT));
            case "price_fetch":
                return Double.toString(main.getConfig().getDouble(Config.PRICE_FETCH));
            case "activechests":
                return Integer.toString(allChests.size());
        }

        String[] split = identifier.split("_");
        if(split.length!=2) {
            return null;
        }
        Integer id;
        try {
            id = Integer.parseInt(split[1]);
            if (id == null) {
                return null;
            }
        } catch (Throwable t) {
            return null;
        }
        if(id < 1) return null;
        id--;

        switch (split[0]) {
            case "isactive":
                if (id >= allChests.size()) {
                    return "false";
                }
                return "true";
        }

        if(id >= allChests.size()) {
            return "";
        }
        switch(split[0]) {
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
