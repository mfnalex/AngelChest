package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.Features;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.updatechecker.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandVersion implements CommandExecutor {

    // --Commented out by Inspection (31.03.2021 23:26):private static final String PLACEHOLDER = "§r §7|§r ";


    @Override
    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {

        if (!commandSender.hasPermission(Permissions.VERSION)) {
            commandSender.sendMessage(Main.getInstance().messages.MSG_NO_PERMISSION);
            return true;
        }

        final String[] output = new String[] {"§6===[§bAngelChest Version§6]===", "§eAngelChest" + (Daddy.allows(Features.GENERIC) ? "Plus " : " ") + Main.getInstance().getDescription().getVersion(),
                //"§e" + Bukkit.getBukkitVersion(),
                "§e" + Bukkit.getVersion(), "",};

        final TextComponent discord = new TextComponent("(Click here for Discord support)");
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.DISCORD_LINK));
        discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Discord: " + Main.DISCORD_LINK)));
        discord.setItalic(true);
        discord.setColor(ChatColor.GOLD);

        commandSender.sendMessage(output);
        if(commandSender instanceof Player) {
            commandSender.spigot().sendMessage(discord);
        } else {
            commandSender.sendMessage(ChatColor.GOLD+"Discord support: https://discord.jeff-media.de");
        }

        if(!Main.getInstance().getConfig().getString(Config.CHECK_FOR_UPDATES).equalsIgnoreCase("false")) {
            commandSender.sendMessage("");
            UpdateChecker.getInstance().checkNow(commandSender);
        }
        return true;
    }
}
