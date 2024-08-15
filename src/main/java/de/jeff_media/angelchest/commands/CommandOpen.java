package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.listeners.PlayerListener;
import de.jeff_media.angelchest.utils.Utils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandOpen implements CommandExecutor {

    private final AngelChestMain main;
    private final PlayerListener listener;

    public CommandOpen(AngelChestMain main, PlayerListener listener) {
        this.main = main;
        this.listener = listener;
    }

    private static final Comparator<AbstractMap.SimpleEntry<AngelChest, Double>> ENTRY_COMPARATOR = Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue);


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(main.messages.MSG_PLAYERSONLY);
            return true;
        }
        Player player = (Player) sender;
        World playerWorld = player.getWorld();
        List< AbstractMap.SimpleEntry<AngelChest, Double>> chestsInWorld = main.getAllAngelChests().stream().filter(angelchest -> {
            World chestWorld = angelchest.getWorld();
            if(!playerWorld.equals(chestWorld)) return false;
            return true;
        }).map(chest -> {
            return new AbstractMap.SimpleEntry<AngelChest, Double>((AngelChest) chest, chest.getBlock().getLocation().clone().add(0.5, 0.5, 0.5).distance(player.getLocation()));
        }).collect(Collectors.toList());

        int maxDistance = Utils.getMaxOpenDistance(player);

        //System.out.println("Chests in world:" );
        chestsInWorld.forEach(entry -> {
            //System.out.println(entry.getKey().getBlock().getLocation().toString() + " - " + entry.getValue());
        });

        AngelChest nearest = chestsInWorld.stream().filter(entry -> {
            //System.out.println("Checking distance: "+entry.getValue());
            boolean isLess = entry.getValue() <= maxDistance;
            //System.out.println("is less: " + isLess);
            return isLess;
        }).min(ENTRY_COMPARATOR).map(AbstractMap.SimpleEntry::getKey).orElse(null);

        if(nearest == null) {
            //System.out.println("nearest is null");
            player.sendMessage(main.messages.MSG_NO_CHEST_NEARBY);
            return true;
        }

        // Test here if player is allowed to open THIS angelchest
        if (!main.protectionUtils.playerMayOpenThisChest(player, nearest)) {
            Messages.send(player, main.messages.MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS);
            return true;
        }

        final boolean firstOpened = !nearest.openedBy.contains(player.getUniqueId().toString());
        listener.openGUIorFastLoot(player, nearest, firstOpened, true);
        return true;
    }
}
