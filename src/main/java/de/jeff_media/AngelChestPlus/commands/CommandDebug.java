package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

        if(args.length>0) {
            switch(args[0].toLowerCase()) {
                case "config": config(commandSender, shift(args)); break;
                case "info": info(commandSender, shift(args)); break;
                case "group": group(commandSender, shift(args)); break;
            }
            return true;
        }

        commandSender.sendMessage(new String[] {
                "Available debug commands:",
                "- info",
                "- group",
                "- config"
        });

        return true;
    }

    private void group(CommandSender commandSender, String[] args) {
        Player player = null;
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
        commandSender.sendMessage("Max Chests: "+maxChests);
        commandSender.sendMessage("Duration: "+duration);
        commandSender.sendMessage("Price Spawn: "+priceSpawn);
        commandSender.sendMessage("Price Open:" +priceOpen);
        commandSender.sendMessage("Price Teleport: "+priceTeleport);
        commandSender.sendMessage("Price Fetch:" +priceFetch);
        commandSender.sendMessage("XP Percentage: "+xpPercentage);
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

    private void setConfig(CommandSender commandSender, String[] args) {
    }

    private void getConfig(CommandSender commandSender, String[] args) {
    }


}
