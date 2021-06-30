package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class GenericTabCompleter implements TabCompleter {

    private final Main main;
    private static final List<String> BLOCKS = new ArrayList<>();
    private static final List<String> POTION_EFFECT_TYPES = new ArrayList<>();

    public GenericTabCompleter() {
        this.main = Main.getInstance();
    }

    private int getChests(final UUID uuid) {
        return main.getAllAngelChestsFromPlayer(Bukkit.getOfflinePlayer(uuid)).size();
    }

    static {
        for(Material mat : Material.values()) {
            if(mat.isBlock() && !mat.isAir()) {
                BLOCKS.add(mat.name());
            }
        }
        for(PotionEffectType type : PotionEffectType.values()) {
            POTION_EFFECT_TYPES.add(type.getName());
        }
    }

    public static List<String> getBlockMaterials(String entered) {
        if(entered == null) return BLOCKS;
        List<String> list = new ArrayList<>();
        entered = entered.toUpperCase(Locale.ROOT);
        for(String mat : BLOCKS) {
            if(mat.startsWith(entered)) {
                list.add(mat);
            }
        }
        return list;
    }

    public static List<String> getPotionEffectTypes(String entered) {
        if(entered == null) return POTION_EFFECT_TYPES;
        List<String> list = new ArrayList<>(POTION_EFFECT_TYPES);
        list.removeIf(s -> !s.startsWith(entered));
        return list;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
        final List<String> list = new ArrayList<>();
        UUID uuid = commandSender instanceof Player ? ((Player) commandSender).getUniqueId() : Main.consoleSenderUUID;
        if (args.length == 1) {
            for (int i = 1; i <= getChests(uuid); i++) {
                if (String.valueOf(i).startsWith(args[0])) {
                    list.add(String.valueOf(i));
                }
            }
            if (commandSender.hasPermission(Permissions.OTHERS)) {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().startsWith(args[0])) {
                        list.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2) {
            if (!commandSender.hasPermission(Permissions.OTHERS)) {
                return null;
            } else {
                //noinspection deprecation
                final OfflinePlayer candidate = Bukkit.getOfflinePlayer(args[0]);
                if (candidate == null) return null;
                uuid = candidate.getUniqueId();
                for (int i = 1; i <= getChests(uuid); i++) {
                    if (String.valueOf(i).startsWith(args[1])) {
                        list.add(String.valueOf(i));
                    }
                }
            }
        } else {
            return null;
        }
        return list;
    }

}
