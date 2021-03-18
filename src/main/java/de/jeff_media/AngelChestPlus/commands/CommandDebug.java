package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.config.ConfigUtils;
import de.jeff_media.AngelChestPlus.config.Permissions;
import de.jeff_media.AngelChestPlus.data.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class CommandDebug implements CommandExecutor {

    private final Main main;

    public CommandDebug(Main main) {
        this.main=main;
    }

    private static String[] shift(String[] args) {
        return Arrays.stream(args).skip(1).toArray(String[]::new);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(!commandSender.hasPermission(Permissions.DEBUG)) {
            commandSender.sendMessage(command.getPermissionMessage());
            return true;
        }

        if(args.length>0) {
            switch(args[0].toLowerCase()) {
                case "on": debug(commandSender, true); break;
                case "off": debug(commandSender,false); break;
                case "blacklist": blacklist(commandSender, shift(args)); break;
                case "config": config(commandSender, shift(args)); break;
                case "info": info(commandSender, shift(args)); break;
                case "group": group(commandSender, shift(args)); break;
            }
            return true;
        }

        commandSender.sendMessage(new String[] {
                "Available debug commands:",
                "- on §6Enables debug mode",
                "- off §6Disables debug mode",
                "- blacklist §6Shows blacklist information",
                "- info §6Shows general debug information",
                "- group §6Shows group information"
                //"- config"
        });

        return true;
    }

    private void blacklist(CommandSender commandSender, String args[]) {
        if(!(commandSender instanceof Player) && args.length==0) {
            commandSender.sendMessage("Use this command as player or specify a player name.");
            return;
        }
        Player player;
        boolean isAnotherPlayer = false;
        if(args.length>0) {
            if(Bukkit.getPlayer(args[0])==null) {
                commandSender.sendMessage("Player "+args[0]+" not found.");
                return;
            } else {
                player = Bukkit.getPlayer(args[0]);
                isAnotherPlayer=true;
            }
        } else {
            player = (Player) commandSender;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item==null) {
            commandSender.sendMessage((isAnotherPlayer ? player.getName() : "You")+" must hold an item in the main hand.");
            return;
        }
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        commandSender.sendMessage(" ");
        commandSender.sendMessage("§e=== Material ===");
        commandSender.sendMessage(item.getType().name().toUpperCase());
        commandSender.sendMessage("§e=== Item Name ===");
        commandSender.sendMessage(meta.hasDisplayName() ? "\""+meta.getDisplayName().replaceAll("§", "&") +"\"" : " ");
        commandSender.sendMessage("§e=== Lore ===");
        if(meta.hasLore()) {
            for(String line : meta.getLore()) {
                commandSender.sendMessage("- \"" + line.replaceAll("§","&")+"\"");
            }
        } else {
            commandSender.sendMessage(" ");
        }
        commandSender.sendMessage("§e=== Blacklist Status ===");
        String blacklisted = main.isItemBlacklisted(item);
        commandSender.sendMessage(blacklisted==null ? "Not blacklisted" : "Blacklisted as \""+blacklisted+"\"");

    }

    private void debug(CommandSender commandSender, boolean enabled) {
        ConfigUtils.reloadCompleteConfig(true);
        main.debug=enabled;
        main.getConfig().set("debug",enabled);
        commandSender.sendMessage(ChatColor.GRAY+"AngelChest debug mode has been " + (enabled ? "enabled" : "disabled"));
    }

    private void group(CommandSender commandSender, String[] args) {
        Player player;
        if(args.length==0) {
            if(!(commandSender instanceof Player)) {
                commandSender.sendMessage("Use this command as player or specify a player name.");
                return;
            } else {
                player = (Player) commandSender;
            }
        } else {
            if(Bukkit.getPlayer(args[0])==null) {
                commandSender.sendMessage("Player "+args[0]+" not found.");
                return;
            } else {
                player = Bukkit.getPlayer(args[0]);
            }
        }

        int maxChests = main.groupUtils.getChestsPerPlayer(player);
        int duration = main.groupUtils.getDurationPerPlayer(player);
        double priceSpawn = main.groupUtils.getSpawnPricePerPlayer(player);
        double priceOpen = main.groupUtils.getOpenPricePerPlayer(player);
        double priceTeleport = main.groupUtils.getTeleportPricePerPlayer(player);
        double priceFetch = main.groupUtils.getFetchPricePerPlayer(player);
        double xpPercentage = main.groupUtils.getXPPercentagePerPlayer(player);
        int unlockDuration = main.groupUtils.getUnlockDurationPerPlayer(player);
        double spawnChance = main.groupUtils.getSpawnChancePerPlayer(player);   

        commandSender.sendMessage("Max Chests: "+maxChests);
        commandSender.sendMessage("Duration: "+duration);
        commandSender.sendMessage("Price Spawn: "+priceSpawn);
        commandSender.sendMessage("Price Open:" +priceOpen);
        commandSender.sendMessage("Price Teleport: "+priceTeleport);
        commandSender.sendMessage("Price Fetch:" +priceFetch);
        commandSender.sendMessage("XP Percentage: "+xpPercentage);
        commandSender.sendMessage("Unlock Duration: "+ unlockDuration);
        commandSender.sendMessage("Spawn Chance: "+spawnChance);

    }

    private void info(CommandSender commandSender, String[] args) {
        int expectedAngelChests = main.angelChests.size();
        int realAngelChests = 0;
        int expectedHolograms = main.getAllArmorStandUUIDs().size();
        int realHolograms = 0;

        for(AngelChest angelChest : main.angelChests.values()) {
            if(angelChest != null) {
                realAngelChests++;
            }
        }

        for(UUID uuid : main.getAllArmorStandUUIDs()) {
            if(Bukkit.getEntity(uuid) != null) {
                realHolograms++;
            }
        }

        String text1 = "AngelChests: %d (%d), Holograms: %d (%d)";
        String text2 = "Watchdog: %d Holograms";

        Bukkit.broadcastMessage(String.format(text1,realAngelChests,expectedAngelChests,realHolograms,expectedHolograms));
        Bukkit.broadcastMessage(String.format(text2,main.watchdog.getCurrentUnsavedArmorStands()));
    }

    private void config(CommandSender commandSender, String[] args) {
        if(args.length==0) {
            commandSender.sendMessage(new String[] {
                    "Available config commands:" +
                    "- get <option>",
                    "- set <type> <option> <value>"
            });
        }

        switch(args[0].toLowerCase()) {
            case "set":
                setConfig(commandSender,shift(args));
                break;
            case "get":
                getConfig(commandSender,shift(args));
                break;
        }
    }

    @SuppressWarnings("EmptyMethod")
    private void setConfig(CommandSender commandSender, String[] args) {
        if(args.length>=2) {
            String node = args[0].toLowerCase();
            args = shift(args);
            String value = String.join(" ",args);
            main.getConfig().set(node,value);
            commandSender.sendMessage(String.format("Set \"%s\" to \"%s\"",node,value));
        } else {
            commandSender.sendMessage("Usage: /acd config set <option> <value>");
        }
    }

    @SuppressWarnings("EmptyMethod")
    private void getConfig(CommandSender commandSender, String[] args) {
        if(args.length==1) {
            String node = args[0].toLowerCase();
            commandSender.sendMessage(String.format("%s = %s",node,main.getConfig().get(node).toString()));
        } else {
            commandSender.sendMessage("Usage: /acd config get <option>");
        }
    }


}
