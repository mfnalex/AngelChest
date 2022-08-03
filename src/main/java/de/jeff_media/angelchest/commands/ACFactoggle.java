package de.jeff_media.angelchest.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.jeff_media.jefflib.NBTAPI;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.nbt.NBTValues;
import de.jeff_media.daddy.Stepsister;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%actoggle")
@CommandPermission("angelchest.toggle")
public class ACFactoggle extends BaseCommand {

    private static final Main main = Main.getInstance();

    @Default
    @CommandCompletion("@players")
    public static void onDefault(CommandSender sender, @Optional String playerName) {
        Player player = null;
        if(playerName == null && sender instanceof Player) {
            player = (Player) sender;
        }
        if(playerName == null && !(sender instanceof Player)) {
            sender.sendMessage("§cYou must specify a player.");
            return;
        }
        if(playerName != null) {
            player = Bukkit.getPlayer(playerName);
            if(player == null) {
                sender.sendMessage(String.format(main.messages.MSG_UNKNOWN_PLAYER, playerName));
                return;
            }
        }

        toggle(player, sender);
    }

    private static boolean isSamePlayer(CommandSender sender, Player player) {
        if(sender instanceof Player) {
            return ((Player) sender).getUniqueId().equals(player.getUniqueId());
        }
        return false;
    }


    private static void toggle(Player player, CommandSender sender) {
        toggle(player, sender, NBTAPI.hasNBT(player, NBTTags.HAS_ANGELCHEST_DISABLED));
    }

    private static void toggle(Player player, CommandSender sender, boolean enable) {

        if (!Stepsister.allows(PremiumFeatures.ACTOGGLE)) {
            Messages.send(sender, main.messages.MSG_PREMIUMONLY);
            return;
        }

        if(!sender.hasPermission(Permissions.OTHERS) && !isSamePlayer(sender, player)) {
            sender.sendMessage(main.messages.MSG_NO_PERMISSION);
            return;
        }

        if (enable) {
            NBTAPI.removeNBT(player, NBTTags.HAS_ANGELCHEST_DISABLED);
            if(sender instanceof Player && ((Player)sender).getUniqueId().equals(player.getUniqueId())) {
                Messages.send(sender, main.messages.MSG_ANGELCHEST_ENABLED);
            } else {
                Messages.send(sender, main.messages.MSG_ANGELCHEST_ENABLED_OTHERS.replace("{player}",player.getName()));
            }
        } else {
            if (main.getConfig().getBoolean(Config.USING_ACTOGGLE_BREAKS_EXISTING_CHESTS)) {
                boolean hasChests = false;
                for(AngelChest entry : main.angelChests) {
                    if (!entry.owner.equals(player.getUniqueId())) continue;
                    hasChests = true;
                    entry.destroy(false, false);
                    main.angelChests.remove(entry);
                }
                if (hasChests) {
                    Messages.send(player, main.messages.MSG_ANGELCHEST_EXPLODED);
                }
            }
            NBTAPI.addNBT(player, NBTTags.HAS_ANGELCHEST_DISABLED, NBTValues.TRUE);
            if(sender instanceof Player && ((Player)sender).getUniqueId().equals(player.getUniqueId())) {
                Messages.send(player, main.messages.MSG_ANGELCHEST_DISABLED);
            } else {
                Messages.send(sender, main.messages.MSG_ANGELCHEST_DISABLED_OTHERS.replace("{player}",player.getName()));
            }
        }

        //System.out.println(player + " is now protected: " + enable);

    }

}
