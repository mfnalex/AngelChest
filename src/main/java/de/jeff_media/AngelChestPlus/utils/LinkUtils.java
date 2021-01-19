package de.jeff_media.AngelChestPlus.utils;

import de.jeff_media.AngelChestPlus.config.Config;
import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class LinkUtils {
	
	protected static TextComponent getLinks(Player sendTo, Player affectedPlayer, String preText, String commandTp, String commandUnlock, String commandFetch, Main main) {




		  String placeholder = " ";
		  if((sendTo.hasPermission("angelchest.tp") && commandTp!=null)
				  || (sendTo.hasPermission("angelchest.fetch")&&commandFetch!=null)
				  || (sendTo.hasPermission("angelchest.lock") && commandUnlock!=null)) {
				if(main.getConfig().getBoolean(Config.SHOW_LINKS_ON_SEPARATE_LINE)) preText= preText+"\n";
		  }

		  	TextComponent text = new TextComponent(preText);

		  if(commandTp != null) {
			  TextComponent link = createCommandLink(main.messages.LINK_TP,commandTp);
			  text.addExtra(link);
			  placeholder = " ";
		  }
		  if(commandFetch != null) {
			TextComponent link = createCommandLink(main.messages.LINK_FETCH,commandFetch);
			text.addExtra(placeholder);
			text.addExtra(link);
		}
		  if(commandUnlock != null) {
			  TextComponent link = createCommandLink(main.messages.LINK_UNLOCK,commandUnlock);
			  text.addExtra(placeholder);
			  text.addExtra(link);
		  }
	        
		  return text;
	}
	
	private static TextComponent createCommandLink(String text, String command) {
		// Hover text
		/*ComponentBuilder hoverCB = new ComponentBuilder(
                text+" Link: ").bold(true)
                .append(link).bold(false);*/
		
		TextComponent tc = new TextComponent(text);
		tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command));
		return tc;
	}

}
