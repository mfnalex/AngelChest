package de.jeff_media.angelchest.config;

import de.jeff_media.angelchest.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Contains all translatable messages. Loads translations from the config file, or falls back to hardcoded default values
 * DO NOT USE HARDCODED MESSAGES unless they will only be shown to console or server operators.
 */
public final class Messages {

    public static final String[] usingFreeVersion = new String[] {"========================================================", "You are using the free version of AngelChest. There is", "also a premium version available, called AngelChestPlus.", "It includes TONS of new features and exclusive Discord", "support. The free version will still receive bugfixes,", "but there won't be ANY new features!", "If you like AngelChest, you will LOVE AngelChestPlus, so", "please consider upgrading! Thank you for using AngelChest.", "", Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS, "========================================================",};
    public static final String[] usingPlusVersion = new String[] {"========================================================", "Thanks for buying AngelChestPlus! Premium features have", "been unlocked successfully. Have fun!", "========================================================",};
    public final String MSG_TP_ACROSS_WORLDS_NOT_ALLOWED;
    public final String MSG_FETCH_ACROSS_WORLDS_NOT_ALLOWED;
    public final String ANGELCHEST_INVENTORY_NAME;
    public final String ANGELCHEST_LIST;
    public final String ERR_ALREADYUNLOCKED;
    public final String ERR_INVALIDCHEST = ChatColor.RED + "Invalid AngelChest!";
    // The following messages shouldn't really appear
    public final String ERR_NOTOWNER = ChatColor.RED + "You do not own this AngelChest.";
    public final String GUI_ACCEPT;
    public final String GUI_BACK;
    public final String GUI_DECLINE;
    public final String GUI_FETCH;
    public final String GUI_INFO;
    public final String GUI_INFO_LORE;
    public final String GUI_PREVIEW;
    public final String GUI_TELEPORT;
    public final String GUI_TITLE_CHEST;
    public final String GUI_TITLE_MAIN;
    public final String GUI_UNLOCK;
    public final String HOLOGRAM_TEXT;
    public final String LINK_FETCH;
    public final String LINK_TP;
    //public final String LINK_UNLOCK_FOR;
    //public final String MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED;
    //public final String MSG_UNLOCKED_MORE_ANGELCHESTS;
    public final String LINK_UNLOCK;
    public final String MSG_ACTIONBAR_INVULNERABLE;
    public final String MSG_ACTIONBAR_VULNERABLE;
    public final String MSG_ANGELCHEST_CREATED;
    public final String MSG_ANGELCHEST_DISAPPEARED;
    public final String MSG_ANGELCHEST_EXPLODED;
    public final String MSG_ANGELCHEST_LOCATION;
    public final String MSG_CONFIRM;
    public final String MSG_EMPTIED;
    public final String MSG_INVENTORY_WAS_EMPTY;
    public final String MSG_MUST_SPECIFY_PLAYER;
    public final String MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS;
    public final String MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS;
    public final String MSG_NOT_ENOUGH_MONEY;
    public final String MSG_NOT_ENOUGH_MONEY_CHEST;
    public final String MSG_NO_CHEST_IN_PVP;
    public final String MSG_NO_PERMISSION;
    public final String MSG_OPENED;
    public final String MSG_PAID_OPEN;
    public final String MSG_PLAYERSONLY;
    public final String MSG_PLEASE_SELECT_CHEST;
    public final String MSG_PREMIUMONLY;
    public final String MSG_RETRIEVED;
    public final String MSG_SPAWN_CHANCE_UNSUCCESFULL;
    public final String MSG_UNKNOWN_PLAYER;
    public final String MSG_UNLOCKED_AUTOMATICALLY;
    public final String MSG_UNLOCKED_ONE_ANGELCHEST;
    public final String MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS;
    public final String MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK;
    public final String MSG_YOU_GOT_YOUR_INVENTORY_BACK;
    public final String PREFIX;
    private final Main main;

    public Messages(final Main main) {
        this.main = Main.getInstance();
        if (main.getConfig().getBoolean(Config.PREFIX_MESSAGES)) {
            PREFIX = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.PREFIX));
        } else {
            PREFIX = "";
        }

        MSG_PREMIUMONLY = PREFIX + "§6This feature is only available in AngelChestPlus.";

        MSG_NO_PERMISSION = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-no-permission", "&cYou do not have the permission to use this command."));

        MSG_SPAWN_CHANCE_UNSUCCESFULL = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-spawn-chance-unsuccessful", "&cYour Angel forgot to collect your stuff, unlucky!"));

        MSG_PLAYERSONLY = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-error-players-only", "&cError: This command can only be used by players."));

        MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-not-allowed-to-break-other-angelchests", "&cYou are not allowed to break other people's AngelChest."));

        MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-not-allowed-to-open-other-angelchests", "&cYou are not allowed to open other people's AngelChest."));

        MSG_YOU_DONT_HAVE_ANY_ANGELCHESTS = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-you-dont-have-any-angelchests", "&eYou don't have any AngelChests."));

        //MSG_ALL_YOUR_ANGELCHESTS_WERE_ALREADY_UNLOCKED =PREFIX +  ChatColor.translateAlternateColorCodes('&', main.getConfig()
        //		.getString("message-all-your-angelchests-were-already-unlocked", "&eAll your AngelChests were already unlocked."));

        MSG_UNLOCKED_ONE_ANGELCHEST = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-unlocked-one-angelchest", "&aYou have unlocked your AngelChest."));

        MSG_ANGELCHEST_DISAPPEARED = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-angelchest-disappeared", "&cYou were too slow... Your AngelChest has disappeared and dropped its contents."));

        //MSG_UNLOCKED_MORE_ANGELCHESTS = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig()
        //		.getString("message-unlocked-more-angelchests", "&aYou have unlocked %d AngelChests."));

        MSG_INVENTORY_WAS_EMPTY = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-inventory-was-empty", "&eAn Angel searched for your stuff but could not find anything."));

        MSG_ANGELCHEST_CREATED = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-angelchest-created", "&aAn Angel collected your stuff and put it into a chest located at the place of your death."));

        MSG_YOU_GOT_YOUR_INVENTORY_BACK = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-you-got-your-inventory-back", "&aYou got your inventory back!"));

        MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-you-got-part-of-your-inventory-back", "&eYou got a part of your inventory back, but some items are still in the AngelChest."));

        MSG_NOT_ENOUGH_MONEY = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-not-enough-money", "&cYou don't have enough money."));

        MSG_NOT_ENOUGH_MONEY_CHEST = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-not-enough-money2", "&cAn Angel tried to collect your stuff but you didn't have enough money."));

        MSG_OPENED = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-angelchest-opened", "&6{player} has opened your AngelChest."));

        MSG_EMPTIED = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-angelchest-emptied", "&c{player} has emptied your AngelChest."));

        HOLOGRAM_TEXT = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.HOLOGRAM_TEXT, "&a&l[AngelChest]&r\n&b{player}\n&6{time}\n&cKilled by {deathcause}"));

        ANGELCHEST_INVENTORY_NAME = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.ANGELCHEST_INVENTORY_NAME, "&a[AngelChest] &b{player}&r"));

        ANGELCHEST_LIST = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.ANGELCHEST_LIST, "[{id}] {time} &aX:&f {x} &aY:&f {y} &aZ:&f {z} | {world} "));

        MSG_ANGELCHEST_LOCATION = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-angelchest-location", "&eLocation of your AngelChests:").replaceAll(": %s", ""));

        MSG_PLEASE_SELECT_CHEST = PREFIX + ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-please-select-chest", "&7Please specify which AngelChest you would like to select."));

        LINK_TP = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.LINK_TELEPORT, "&6[TP]&r"));

        LINK_FETCH = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.LINK_FETCH, "&6[Fetch]&r"));

        LINK_UNLOCK = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.LINK_UNLOCK, "&5[Unlock]&r"));

        //LINK_UNLOCK_FOR = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.LINK_UNLOCK_FOR,"&5[Unlock for...]&r"));

        ERR_ALREADYUNLOCKED = PREFIX + getMsg("already-unlocked", "&cThis AngelChest is already unlocked.");

        MSG_ANGELCHEST_EXPLODED = PREFIX + getMsg("too-many-angelchests", "&cYou had more AngelChests than your guardian angel could handle... Your oldest AngelChest has exploded.");

        MSG_NO_CHEST_IN_PVP = PREFIX + getMsg("no-angelchest-in-pvp", "&cAn Angel tried to collect your stuff but was put to flight by the presence of your killer.");

        MSG_RETRIEVED = PREFIX + getMsg("angelchest-retrieved", "&aAngelChest retrieved!");

        MSG_CONFIRM = PREFIX + getMsg("confirm", "&6You are about to spend {price}{currency}. Click this message to continue.");

        MSG_PAID_OPEN = PREFIX + getMsg("paid-open", "&8You spent {price}{currency} to open your AngelChest.");

        MSG_UNLOCKED_AUTOMATICALLY = PREFIX + getMsg("unlocked-automatically", "&8Your AngelChest has been unlocked automatically.");

        MSG_TP_ACROSS_WORLDS_NOT_ALLOWED = PREFIX + getMsg("tp-across-worlds-not-allowed","&cYou cannot teleport across worlds.");

        MSG_FETCH_ACROSS_WORLDS_NOT_ALLOWED = PREFIX + getMsg("fetch-across-worlds-not-allowed","&cYou cannot fetch across worlds.");

        MSG_ACTIONBAR_INVULNERABLE = getMsg("invulnerable", "&a&lYou are invulnerable for {time}.");

        MSG_ACTIONBAR_VULNERABLE = getMsg("vulnerable", "&c&lYou are no longer invulnerable.");

        GUI_TITLE_CHEST = getGui(Config.GUI_TITLE_CHEST, "§4§l[§c§lAngelChest§4§l] §c#{id} §4| §c{time}");

        GUI_TITLE_MAIN = getGui(Config.GUI_TITLE_MAIN, "§4§l[§c§lAngelChest§4§l]");

        GUI_ACCEPT = getGui("gui-accept", "&aAccept");
        GUI_DECLINE = getGui("gui-decline", "&cDecline");
        GUI_BACK = getGui("gui-back", "&6Back");
        GUI_INFO = getGui("gui-info", "&6Info");
        GUI_INFO_LORE = getGui("gui-info-lore", "§6You are about to spend {price}{currency}.");
        GUI_TELEPORT = getGui("gui-teleport", "&6Teleport");
        GUI_FETCH = getGui("gui-fetch", "&6Fetch");
        GUI_UNLOCK = getGui("gui-unlock", "&6Unlock");
        GUI_PREVIEW = getGui("gui-preview", "&6Preview");

		/*
		Hardcoded
		 */
        MSG_MUST_SPECIFY_PLAYER = PREFIX + "§cYou must specify a player when running this command from console.";
        MSG_UNKNOWN_PLAYER = PREFIX + "§cCould not find player §7%s";
    }

    public static void send(final CommandSender receiver, final String message) {
        if (receiver == null) return;
        if (message.equals("")) return;
        receiver.sendMessage(message);
    }

    public static void sendActionBar(final Player receiver, final String message) {
        if (receiver == null || !receiver.isOnline()) return;
        receiver.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static void sendPremiumOnlyConsoleMessage(final String configNode) {
        Main.getInstance().getLogger().warning("You are using the config option \"" + configNode + "\". This is only available in AngelChestPlus, see here: " + Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
    }

    private String getGui(final String path, final String defaultText) {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(path, defaultText));
    }

    private String getMsg(final String path, final String defaultText) {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message-" + path, defaultText));
    }
}