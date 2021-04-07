package de.jeff_media.angelchest.commands;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.Features;
import de.jeff_media.daddy.Daddy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
                "§e" + Bukkit.getVersion(), null,};

        final TextComponent discord = new TextComponent("(Click here for Discord support)");
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.DISCORD_LINK));
        discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Discord: " + Main.DISCORD_LINK)));
        discord.setItalic(true);
        discord.setColor(ChatColor.GOLD);

        /*TextComponent links = new TextComponent("");

        if(Daddy.allows(Features.GENERIC)) {
            links.addExtra(LinkUtils.createURLLink("§6§lDownload", Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS));
            links.addExtra(PLACEHOLDER);
        } else {
            links.addExtra(LinkUtils.createURLLink("§6§lDownload (Plus)", Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS));
            links.addExtra(PLACEHOLDER);
            links.addExtra(LinkUtils.createURLLink("§6§lDownload (Free)", Main.UPDATECHECKER_LINK_DOWNLOAD_FREE));
            links.addExtra(PLACEHOLDER);
        }
        links.addExtra(LinkUtils.createURLLink("§6§lDonate", Main.UPDATECHECKER_LINK_DONATE));
        links.addExtra(PLACEHOLDER);
        links.addExtra(LinkUtils.createURLLink("§6§lDiscord","https://discord.jeff-media.de"));*/
        commandSender.sendMessage(output);
        commandSender.spigot().sendMessage(discord);
        commandSender.sendMessage((String) null);
        //commandSender.spigot().sendMessage(links);
        if (Main.getInstance().updateChecker == null) {
            commandSender.sendMessage(ChatColor.RED + "Update checker is disabled.");
        } else {
            Main.getInstance().updateChecker.check(commandSender);
        }
        return true;
    }
}
