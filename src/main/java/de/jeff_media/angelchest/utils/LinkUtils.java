package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public final class LinkUtils {

    protected static TextComponent getLinks(final CommandSender sendTo, @SuppressWarnings("unused") final OfflinePlayer affectedPlayer, String preText, final String commandTp, final String commandUnlock, final String commandFetch) {

        final Main main = Main.getInstance();

        String placeholder = " ";
        if ((sendTo.hasPermission(Permissions.TP) && commandTp != null)
                || (sendTo.hasPermission(Permissions.FETCH) && commandFetch != null)
                || (sendTo.hasPermission(Permissions.PROTECT) && commandUnlock != null)) {
            if (main.getConfig().getBoolean(Config.SHOW_LINKS_ON_SEPARATE_LINE)) preText = preText + "\n";
        }

        final TextComponent text = new TextComponent(preText);

        if (commandTp != null) {
            final TextComponent link = createCommandLink(main.messages.LINK_TP, commandTp);
            text.addExtra(link);
            placeholder = " ";
        }
        if (commandFetch != null) {
            final TextComponent link = createCommandLink(main.messages.LINK_FETCH, commandFetch);
            text.addExtra(placeholder);
            text.addExtra(link);
        }
        if (commandUnlock != null) {
            final TextComponent link = createCommandLink(main.messages.LINK_UNLOCK, commandUnlock);
            text.addExtra(placeholder);
            text.addExtra(link);
        }

        return text;
    }

// --Commented out by Inspection START (31.03.2021 23:27):
//    public static TextComponent createURLLink(String text, String link) {
//        TextComponent tc = new TextComponent(text);
//        tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
//        return tc;
//    }
// --Commented out by Inspection STOP (31.03.2021 23:27)

    private static TextComponent createCommandLink(final String text, final String command) {
        // Hover text
		/*ComponentBuilder hoverCB = new ComponentBuilder(
                text+" Link: ").bold(true)
                .append(link).bold(false);*/

        final TextComponent tc = new TextComponent(text);
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return tc;
    }

}
