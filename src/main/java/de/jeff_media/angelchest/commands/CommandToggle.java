package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.CommandManager;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.nbt.NBTValues;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.NBTAPI;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public class CommandToggle implements CommandExecutor {

    private final Main main;

    public CommandToggle() {
        this.main = Main.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {

        if(!Daddy.allows(PremiumFeatures.ACTOGGLE)) {
            Messages.send(commandSender,main.messages.MSG_PREMIUMONLY);
            return true;
        }

        if(!(commandSender instanceof Player)) {
            Messages.send(commandSender,main.messages.MSG_PLAYERSONLY);
            return true;
        }

        if(CommandManager.toggleableCommandAliases.containsKey(alias)) {
            toggle((Player)commandSender,CommandManager.toggleableCommandAliases.get(alias));
            return true;
        }

        toggle((Player)commandSender);
        return true;
    }

    private void toggle(final Player player) {
        toggle(player, NBTAPI.hasNBT(player, NBTTags.HAS_ANGELCHEST_DISABLED));
    }

    private void toggle(final Player player, final boolean enable) {
        if(enable) {
            NBTAPI.removeNBT(player, NBTTags.HAS_ANGELCHEST_DISABLED);
            Messages.send(player,main.messages.MSG_ANGELCHEST_ENABLED);
        } else {
            if(main.getConfig().getBoolean(Config.USING_ACTOGGLE_BREAKS_EXISTING_CHESTS)) {
                boolean hasChests = false;
                final Iterator<Map.Entry<Block,AngelChest>> it = main.angelChests.entrySet().iterator();
                while (it.hasNext()) {
                    final Map.Entry<Block,AngelChest> entry = it.next();
                    if (!entry.getValue().owner.equals(player.getUniqueId())) continue;
                    hasChests = true;
                    entry.getValue().destroy(false);
                    it.remove();
                }
                if (hasChests) {
                    Messages.send(player,main.messages.MSG_ANGELCHEST_EXPLODED);
                }
            }
            NBTAPI.addNBT(player,NBTTags.HAS_ANGELCHEST_DISABLED, NBTValues.TRUE);
            Messages.send(player,main.messages.MSG_ANGELCHEST_DISABLED);
        }
    }
}
