package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.ConfigDumper;
import de.jeff_media.angelchest.config.ConfigUtils;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.BlacklistEntry;
import de.jeff_media.angelchest.enums.BlacklistResult;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.utils.BlacklistUtils;
import de.jeff_media.angelchest.utils.HologramFixer;
import de.jeff_media.angelchest.utils.Utils;
import de.jeff_media.daddy.Daddy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CommandDebug implements CommandExecutor, TabCompleter {

    private final Main main;

    public CommandDebug() {
        this.main = Main.getInstance();
    }

    private static String[] shift(final String[] args) {
        return Arrays.stream(args).skip(1).toArray(String[]::new);
    }

    private void blacklist(final CommandSender commandSender, String[] args) {

        if (!Daddy.allows(PremiumFeatures.GENERIC)) {
            Messages.send(commandSender,main.messages.MSG_PREMIUMONLY);
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("add")) {
            if (!(commandSender instanceof Player)) {
                Messages.send(commandSender,main.messages.MSG_PLAYERSONLY);
                return;
            }
            final Player player = (Player) commandSender;
            final ItemStack item = player.getInventory().getItemInMainHand();

            if (Utils.isEmpty(item)) {
                Messages.send(player,ChatColor.RED + "You must hold an item in your hand.");
                return;
            }

            if (args.length < 2) {
                Messages.send(player,ChatColor.RED + "You must specify a name for this blacklist entry.");
                return;
            }

            final String[] lines = BlacklistUtils.addToBlacklist(item, args[1]);
            if (lines != null) {
                Messages.send(player,ChatColor.GREEN + "Added following blacklist entry:");
                Messages.send(player,lines);
                ConfigUtils.reloadCompleteConfig(true);
            } else {
                Messages.send(player,ChatColor.RED + "Blacklist already contains an entry called \"" + args[1] + "\"");
            }

        } else if (args.length > 0 && args[0].equalsIgnoreCase("info")) {

            args = shift(args);

            Messages.send(commandSender," ");
            Messages.send(commandSender,"§6===[§bAngelChest Blacklist Info§6]===");

            if (!(commandSender instanceof Player) && args.length == 0) {
                Messages.send(commandSender,"Use this command as player or specify a player name.");
                return;
            }
            final Player player;
            boolean isAnotherPlayer = false;
            if (args.length > 0) {
                if (Bukkit.getPlayer(args[0]) == null) {
                    Messages.send(commandSender,"Player " + args[0] + " not found.");
                    return;
                } else {
                    player = Bukkit.getPlayer(args[0]);
                    isAnotherPlayer = true;
                }
            } else {
                player = (Player) commandSender;
            }
            assert player != null;
            final ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null) {
                Messages.send(commandSender,(isAnotherPlayer ? player.getName() : "You") + " must hold an item in the main hand.");
                return;
            }
            final ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());

            Messages.send(commandSender,"§e===[§6Material§e]===");
            Messages.send(commandSender,item.getType().name().toUpperCase());
            Messages.send(commandSender,"§e===[§6Item Name§e]===");
            Messages.send(commandSender,meta.hasDisplayName() ? "\"" + meta.getDisplayName().replaceAll("§", "&") + "\"" : " ");
            Messages.send(commandSender,"§e===[§6Lore§e]===");
            if (meta.hasLore()) {
                for (final String line : meta.getLore()) {
                    Messages.send(commandSender,"- \"" + line.replaceAll("§", "&") + "\"");
                }
            } else {
                Messages.send(commandSender," ");
            }
            Messages.send(commandSender,"§e===[§6Blacklist Status§e]===");
            final String blacklisted = main.isItemBlacklisted(item);
            Messages.send(commandSender,blacklisted == null ? "Not blacklisted" : "Blacklisted as \"" + blacklisted + "\"");
        } else if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            args = shift(args);
            Messages.send(commandSender,new String[] {" ", "§6===[§bAngelChest Blacklist Test§6]==="});


            if (!(commandSender instanceof Player)) {
                Messages.send(commandSender,"You must be a player to use this command.");
                return;
            }
            final Player player = (Player) commandSender;

            if (args.length == 0) {
                Messages.send(commandSender,"You must specify a blacklist definition (e.g. \"exampleAllHelmets\" from the example blacklist).");
                return;
            }

            final BlacklistEntry blacklistEntry = main.itemBlacklist.get(args[0].toLowerCase());
            if (blacklistEntry == null) {
                Messages.send(commandSender,"Blacklist definition \"" + args[0] + "\" not found.");
                return;
            }

            final ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null) {
                Messages.send(commandSender,"You must hold an item in the main hand.");
                return;
            }

            final BlacklistResult result = blacklistEntry.matches(item);

            if (result == BlacklistResult.MATCH) {
                Messages.send(commandSender,"§aThis item matches the blacklist definition \"" + result.getName() + "\"");
            } else {
                Messages.send(commandSender,"§cThis item does not match the blacklist definition \"" + main.itemBlacklist.get(args[0].toLowerCase()).getName() + "\"");
                Messages.send(commandSender,"§eReason: " + result.name());
            }


        } else {

            Messages.send(commandSender,new String[] {
                    "§eAvailable blacklist commands:",
                    "/acd blacklist add <name> §6Adds current item to to the blacklist as <name>",
                    "/acd blacklist info §6Shows material, name and lore of the current item including the blacklist definition it matches",
                    "/acd blacklist test <item> §6Shows whether the current item matches a given blacklist definition including the reason when it does not match"});
        }

    }

    /*private void createhologram(CommandSender commandSender) {
        Location loc = ((Player)commandSender).getLocation();
        int rand = new Random().nextInt(Integer.MAX_VALUE);
        ArmorStand entity = (ArmorStand) loc.getWorld().spawnEntity(loc,EntityType.ARMOR_STAND);
        entity.setVisible(false);
        entity.setCustomName(""+rand);
        entity.setCustomNameVisible(true);
        entity.setInvulnerable(true);
        NBTAPI.addNBT(entity, NBTTags.IS_HOLOGRAM, NBTValues.TRUE);
    }*/

    private void checkconfig(final CommandSender commandSender) {
        Messages.send(commandSender,"§6");
        Messages.send(commandSender,"§6===[§bAngelChest ConfigCheck§6]===");
        Messages.send(commandSender,"§6Please not that you have to run /acreload after making changes to your config.");
        final List<String> errors = main.invalidConfigFiles == null ? new ArrayList<>() : Arrays.asList(main.invalidConfigFiles);
        if (main.invalidConfigFiles == null) {
            Messages.send(commandSender,"§aAll your config files are valid.");
        } else {
            Messages.send(commandSender,"§cSome of your config files are invalid.");
        }

        if (errors.contains("config.yml")) {
            Messages.send(commandSender,"§e- config.yml: §cinvalid");
        } else {
            Messages.send(commandSender,"§e- config.yml: §avalid");
        }

        if (new File(main.getDataFolder(), "groups.yml").exists()) {
            if (errors.contains("groups.yml")) {
                Messages.send(commandSender,"§e- groups.yml: §cinvalid");
            } else {
                Messages.send(commandSender,"§e- groups.yml: §avalid");
            }
        } else {
            Messages.send(commandSender,"§e- groups.yml: §6does not exist");
        }
        if (new File(main.getDataFolder(), "blacklist.yml").exists()) {
            if (errors.contains("blacklist.yml")) {
                Messages.send(commandSender,"§e- blacklist.yml: §cinvalid");
            } else {
                Messages.send(commandSender,"§e- blacklist.yml: §avalid");
            }
        } else {
            Messages.send(commandSender,"§e- blacklist.yml: §6does not exist");
        }
    }

    private void debug(final CommandSender commandSender, final boolean enabled) {
        ConfigUtils.reloadCompleteConfig(true);
        main.debug = enabled;
        main.getConfig().set("debug", enabled);
        Messages.send(commandSender,ChatColor.GRAY + "AngelChest debug mode has been " + (enabled ? "enabled" : "disabled"));
    }

    private void dump(final CommandSender commandSender) {
        ConfigDumper.dump(commandSender);
    }

    private void fixholograms(final CommandSender commandSender) {
        int deadHolograms = 0;
        for (final World world : Bukkit.getWorlds()) {
            deadHolograms += HologramFixer.removeDeadHolograms(world);
        }

        if (deadHolograms == 0) {
            Messages.send(commandSender,new String[] {ChatColor.GRAY + "There are no dead AngelChest holograms.", ChatColor.GRAY + "Please note that this command can only remove holograms in loaded chunks created in AngelChest 3.3.0 or later. Join my discord to get a command that can remove all dead holograms (including those created by other plugins): " + Main.DISCORD_LINK});
        } else {
            Messages.send(commandSender,ChatColor.GREEN + "Removed " + deadHolograms + " dead AngelChest holograms.");
        }
    }

    private @Nullable List<String> getMatching(final String[] commands, final String entered) {
        final List<String> list = new ArrayList<>(Arrays.asList(commands));
        list.removeIf(current->!current.startsWith(entered));
        return list;
    }

    private void group(final CommandSender commandSender, final String[] args) {
        final Player player;
        if (args.length == 0) {
            if (!(commandSender instanceof Player)) {
                Messages.send(commandSender,"Use this command as player or specify a player name.");
                return;
            } else {
                player = (Player) commandSender;
            }
        } else {
            if (Bukkit.getPlayer(args[0]) == null) {
                Messages.send(commandSender,"Player " + args[0] + " not found.");
                return;
            } else {
                player = Bukkit.getPlayer(args[0]);
            }
        }

        final int maxChests = main.groupUtils.getChestsPerPlayer(player);
        final int duration = main.groupUtils.getDurationPerPlayer(player);
        final double priceSpawn = main.groupUtils.getSpawnPricePerPlayer(player);
        final double priceOpen = main.groupUtils.getOpenPricePerPlayer(player);
        final double priceTeleport = main.groupUtils.getTeleportPricePerPlayer(player);
        final double priceFetch = main.groupUtils.getFetchPricePerPlayer(player);
        final double priceSummon = main.groupUtils.getSummonPricePerPlayer(player);
        final double xpPercentage = main.groupUtils.getXPPercentagePerPlayer(player);
        final int unlockDuration = main.groupUtils.getUnlockDurationPerPlayer(player);
        final double spawnChance = main.groupUtils.getSpawnChancePerPlayer(player);
        final int itemLoss = main.groupUtils.getItemLossPerPlayer(player);
        final boolean allowTPAcrossWorlds = main.groupUtils.getAllowTpAcrossWorlds(player);
        final boolean allowFetchAcrossWorlds = main.groupUtils.getAllowFetchAcrossWorlds(player);
        final int maxTpDistance = main.groupUtils.getMaxTpDistance(player);
        final int maxFetchDistance = main.groupUtils.getMaxFetchDistance(player);

        Messages.send(commandSender,"§6Max Chests:§b " + maxChests);
        Messages.send(commandSender,"§6Duration:§b " + duration);
        Messages.send(commandSender,"§6Price Spawn:§b " + priceSpawn + " §8(depending on current balance)");
        Messages.send(commandSender,"§6Price Open:§b " + priceOpen + " §8(depending on current balance)");
        Messages.send(commandSender,"§6Price Teleport:§b " + priceTeleport + " §8(depending on current balance)");
        Messages.send(commandSender,"§6Price Fetch:§b " + priceFetch + " §8(depending on current balance)");
        Messages.send(commandSender,"§6Price Summon:§b " + priceSummon + " §8(depending on current balance)");
        Messages.send(commandSender,"§6XP Percentage:§b " + xpPercentage);
        Messages.send(commandSender,"§6Unlock Duration:§b " + unlockDuration);
        Messages.send(commandSender,"§6Spawn Chance:§b " + spawnChance);
        Messages.send(commandSender,"§6Item Loss:§b " + itemLoss + " §8(depending on current inv)");
        Messages.send(commandSender,"§6TP across worlds:§b " + allowTPAcrossWorlds);
        Messages.send(commandSender,"§6Fetch across worlds:§b " + allowFetchAcrossWorlds);
        Messages.send(commandSender,"§6Max TP distance:§b " + maxTpDistance);
        Messages.send(commandSender,"§6Max Fetch distance:§b " + maxFetchDistance);

    }

    private void info(final CommandSender commandSender) {
        final int expectedAngelChests = main.angelChests.size();
        int realAngelChests = 0;
        final int expectedHolograms = main.getAllArmorStandUUIDs().size();
        int realHolograms = 0;

        for (final AngelChest angelChest : main.angelChests.values()) {
            if (angelChest != null) {
                realAngelChests++;
            }
        }

        for (final UUID uuid : main.getAllArmorStandUUIDs()) {
            if (Bukkit.getEntity(uuid) != null) {
                realHolograms++;
            }
        }

        final String text1 = "AngelChests: %d (%d), Holograms: %d (%d)";
        final String text2 = "Watchdog: %d Holograms";

        Messages.send(commandSender,String.format(text1, realAngelChests, expectedAngelChests, realHolograms, expectedHolograms));
        Messages.send(commandSender,String.format(text2, main.watchdog.getCurrentUnsavedArmorStands()));
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {

        if (!commandSender.hasPermission(Permissions.DEBUG)) {
            Messages.send(commandSender,main.messages.MSG_NO_PERMISSION);
            return true;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "on":
                    debug(commandSender, true);
                    return true;
                case "off":
                    debug(commandSender, false);
                    return true;
                case "blacklist":
                    blacklist(commandSender, shift(args));
                    return true;
                case "info":
                    info(commandSender);
                    return true;
                case "group":
                    group(commandSender, shift(args));
                    return true;
                case "checkconfig":
                    checkconfig(commandSender);
                    return true;
                case "dump":
                    dump(commandSender);
                    return true;
                case "fixholograms":
                    fixholograms(commandSender);
                    return true;
                case "disableac":
                    main.disableDeathEvent = true;
                    Messages.send(commandSender,"§cDisabled AngelChest spawning");
                    return true;
                case "enableac":
                    main.disableDeathEvent = false;
                    Messages.send(commandSender,"§aEnabled AngelChest spawning");
                    return true;
            }
        }

        Messages.send(commandSender,new String[] {
                "§eAvailable debug commands:",
                "/acd on §6Enables debug mode",
                "/acd off §6Disables debug mode",
                "/acd blacklist §6Shows blacklist information",
                "/acd checkconfig §6Checks config files for errors",
                "/acd info §6Shows general debug information",
                "/acd group §6Shows group information",
                "/acd dump §6Dump debug information",
                "/acd fixholograms §6Removes dead holograms",
                "/acd disableac §6Disables AngelChest spawning",
                "/acd enableac §6Enables AngelChest spawning"
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
        final String[] mainCommands = {"on", "off", "blacklist", "info", "group", "checkconfig", "dump", "fixholograms","disableac","enableac"};
        final String[] blacklistCommands = {"info", "test", "add"};

        // Debug
        /*main.verbose("args.lengh = "+args.length);
        for(int i = 0; i < args.length; i++) {
            main.verbose("args["+i+"] = "+args[i]);
        }*/

        if (args.length == 0) {
            return Arrays.asList(mainCommands);
        }
        if (args.length == 1) {
            return getMatching(mainCommands, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("blacklist")) {
            return getMatching(blacklistCommands, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("blacklist") && args[1].equalsIgnoreCase("test")) {
            final String[] definedItems = new String[main.itemBlacklist.size()];
            int i = 0;
            for (final BlacklistEntry blacklistEntry : main.itemBlacklist.values()) {
                definedItems[i] = blacklistEntry.getName();
                i++;
            }
            return getMatching(definedItems, args[2]);
        }

        return null;
    }
}
