package de.jeff_media.AngelChestPlus;

import org.bukkit.ChatColor;


public class Messages {
	//private AngelChestPlugin plugin;

	final Main main;



	public final String MSG_PLAYERSONLY,MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS,MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS,
	MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED, MSG_UNLOCKED_ONE_ANGELCHEST, MSG_UNLOCKED_MORE_ANGELCHESTS, MSG_INVENTORY_WAS_EMPTY,
	MSG_ANGELCHEST_CREATED, MSG_ANGELCHEST_DISAPPEARED, MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS, MSG_YOU_GOT_YOUR_INVENTORY_BACK
	, MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK, HOLOGRAM_TEXT, ANGELCHEST_INVENTORY_NAME, ANGELCHEST_LIST, MSG_ANGELCHEST_LOCATION, MSG_NOT_ENOUGH_MONEY,
	MSG_PLEASE_SELECT_CHEST, MSG_ANGELCHEST_EXPLODED, MSG_NO_CHEST_IN_PVP, MSG_RETRIEVED, MSG_CONFIRM,
	MSG_NOT_ENOUGH_MONEY_CHEST;
	
	final String LINK_TP, LINK_FETCH, LINK_UNLOCK;

	// The following messages shouldn't really appear
	final String ERR_NOTOWNER = ChatColor.RED+"You do not own this AngelChest.";
	final String ERR_ALREADYUNLOCKED;
	final String ERR_INVALIDCHEST = ChatColor.RED + "Invalid AngelChest!";

	Messages(Main main) {
		this.main = main;
		//this.plugin = plugin;

		MSG_PLAYERSONLY = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-error-players-only", "&cError: This command can only be run by players."));
		
		MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-not-allowed-to-break-other-angelchests", "&cYou are not allowed to break other people's AngelChest."));
		
		MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-not-allowed-to-open-other-angelchests", "&cYou are not allowed to open other people's AngelChest."));
		
		MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-you-dont-have-any-angelchests", "&eYou don't have any AngelChests."));
		
		MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-all-your-angelchests-were-already-unlocked", "&eAll your AngelChests were already unlocked."));
		
		MSG_UNLOCKED_ONE_ANGELCHEST = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-unlocked-one-angelchest", "&aYou have unlocked your AngelChest."));
		
		MSG_ANGELCHEST_DISAPPEARED = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-angelchest-disappeared", "&cYou were too slow... Your AngelChest has disappeared and dropped its contents."));
		
		MSG_UNLOCKED_MORE_ANGELCHESTS = ChatColor.translateAlternateColorCodes('&', main.getConfig()
				.getString("message-unlocked-more-angelchests", "&aYou have unlocked %d AngelChests."));
		
		MSG_INVENTORY_WAS_EMPTY = ChatColor.translateAlternateColorCodes('&',  main.getConfig().getString("message-inventory-was-empty", "&eAn Angel searched for your stuff but could not find anything."));
		
		MSG_ANGELCHEST_CREATED = ChatColor.translateAlternateColorCodes('&',  main.getConfig().getString("message-angelchest-created", "&aAn Angel collected your stuff and put it into a chest located at the place of your death."));
		
		MSG_YOU_GOT_YOUR_INVENTORY_BACK = ChatColor.translateAlternateColorCodes('&',  main.getConfig().getString("message-you-got-your-inventory-back", "&aYou got your inventory back!"));
		
		MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK = ChatColor.translateAlternateColorCodes('&',  main.getConfig().getString("message-you-got-part-of-your-inventory-back", "&eYou got a part of your inventory back, but some items are still in the AngelChest."));

		MSG_NOT_ENOUGH_MONEY = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-not-enough-money","&cYou don't have enough money."));

		MSG_NOT_ENOUGH_MONEY_CHEST = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-not-enough-money2","&cAn Angel tried to collect your stuff but you didn't have enough money."));
		
		HOLOGRAM_TEXT = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.HOLOGRAM_TEXT,"&a&l[AngelChest]&r\n&b{player}\n&6{time}"));
		
		ANGELCHEST_INVENTORY_NAME = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.ANGELCHEST_INVENTORY_NAME,"&a[AngelChest] &b{player}&r"));

		ANGELCHEST_LIST = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.ANGELCHEST_LIST,"[{id}] {time} &aX:&f {x} &aY:&f {y} &aZ:&f {z} | {world} "));
		
		MSG_ANGELCHEST_LOCATION = ChatColor.translateAlternateColorCodes('&',  main.getConfig().getString("message-angelchest-location","&eLocation of your AngelChests:").replaceAll(": %s", ""));

		MSG_PLEASE_SELECT_CHEST = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-please-select-chest","&7Please specify which AngelChest you would like to select."));
		
		LINK_TP = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.LINK_TELEPORT,"&6[TP]&r"));

		LINK_FETCH = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.LINK_FETCH,"&6[Fetch]&r"));
		
		LINK_UNLOCK = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.LINK_UNLOCK,"&5[Unlock]&r"));

		ERR_ALREADYUNLOCKED = getMsg("already-unlocked","&cThis AngelChest is already unlocked.");

		MSG_ANGELCHEST_EXPLODED = getMsg("too-many-angelchests","&cYou had more AngelChests than your guardian angel could handle... Your oldest AngelChest has exploded.");

		MSG_NO_CHEST_IN_PVP = getMsg("no-angelchest-in-pvp","&cAn Angel tried to collect your stuff but was put to flight by the presence of your killer.");

		MSG_RETRIEVED = getMsg("angelchest-retrieved","&aAngelChest retrieved!");

		MSG_CONFIRM = getMsg("confirm","&6You are about to spend {price}{currency}. Click this message to continue.");
	}


	private String getMsg(String path, String defaultText) {
		return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-"+path,defaultText));
	}
}