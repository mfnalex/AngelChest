package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy_Stepsister;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles /acgui
 */
public final class CommandGUI implements CommandExecutor {

    final AngelChestMain main;

    public CommandGUI() {
        this.main = AngelChestMain.getInstance();
        //Chicken.wing(main, AngelChestMain.SCHEDULE_TASKS);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, final String[] args) {

        if (!Daddy_Stepsister.allows(PremiumFeatures.GUI)) {
            Messages.send(sender, main.messages.MSG_PREMIUMONLY);
            return true;
        }

        if (!sender.hasPermission(Permissions.USE) || !sender.hasPermission(Permissions.GUI)) {
            Messages.send(sender, main.messages.MSG_NO_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            Messages.send(sender, main.messages.MSG_PLAYERSONLY);
            return true;
        }

        final Player player = (Player) sender;

        main.guiManager.showMainGUI(player);

        return true;
    }
}
