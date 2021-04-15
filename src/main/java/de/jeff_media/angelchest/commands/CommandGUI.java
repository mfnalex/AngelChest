package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.Features;
import de.jeff_media.daddy.Daddy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandGUI implements CommandExecutor {

    final Main main;

    public CommandGUI() {
        this.main = Main.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, final String[] args) {

        if (!Daddy.allows(Features.GUI)) {
            Messages.send(sender,main.messages.MSG_PREMIUMONLY);
            return true;
        }

        if (!sender.hasPermission(Permissions.USE)) {
            Messages.send(sender,main.messages.MSG_NO_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            Messages.send(sender,main.messages.MSG_PLAYERSONLY);
            return true;
        }

        final Player player = (Player) sender;

        main.guiManager.showMainGUI(player);

        return true;
    }
}
