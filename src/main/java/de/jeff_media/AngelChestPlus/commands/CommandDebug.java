package de.jeff_media.AngelChestPlus.commands;

import de.jeff_media.AngelChestPlus.AngelChest;
import de.jeff_media.AngelChestPlus.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandDebug implements CommandExecutor {

    private final Main main;

    public CommandDebug(Main main) {
        this.main=main;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        for(Entity entity : ((Player) (commandSender)).getNearbyEntities(20,20,20)) {
            if(entity instanceof ArmorStand) {
                commandSender.sendMessage(entity.getUniqueId().toString()+": "+entity.getCustomName());
            }
        }

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

        return true;
    }


}
