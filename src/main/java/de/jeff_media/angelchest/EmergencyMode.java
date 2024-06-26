package de.jeff_media.angelchest;

import de.jeff_media.angelchest.config.Messages;
import com.jeff_media.jefflib.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Emergency mode. Gets enabled when a config file is broken, the free AND paid version are installed
 * or when using MC 1.13.2 or older
 */
public final class EmergencyMode {

    public static final String[] BROKEN_CONFIG_FILE = {"§c ",
            "§c§l! ! ! W A R N I N G ! ! !", "§c ",
            "§cYour AngelChest config file \"{filename}\" is broken! This can happen if you messed up the YAML syntax while editing the file, or when you used an editor that does not support UTF8. Please do NOT use online editors to edit the file. Please delete the file and use a fresh copy to start editing it again. You can validate YAML files here: http://www.yamllint.com/ Contact me on Discord if you need help:",
            "§c ",
            "§c-> " + AngelChestMain.DISCORD_LINK + " <-",
            "§c",
            "§c(This message is only shown to server operators)"};
    public static final String[] FREE_VERSION_INSTALLED = {"§c ",
            "§c§l! ! ! W A R N I N G ! ! !",
            "§c ",
            "§cYou have installed AngelChest " + AngelChestMain.getInstance().getDescription().getVersion() + " but did not remove the old version. The plugin will not work correctly until you remove the old .jar file. Make sure to properly RESTART (NOT RELOAD) your server afterwards! You do NOT have to remove the old AngelChest config folder, it gets updated automatically.",
            "§c",
            "§c(This message is only shown to server operators)"};
    public static final String[] UNSUPPORTED_MC_VERSION_1_13 = {"§c ",
            "§c§l! ! ! W A R N I N G ! ! !",
            "§c ",
            "§cAngelChest " + AngelChestMain.getInstance().getDescription().getVersion() + " is only compatible with Minecraft versions 1.14.1 and newer. Please use AngelChest version 2.22.2 for Minecraft 1.12.X - 1.13.2:",
            "§c ",
            "§c" + AngelChestMain.UPDATECHECKER_LINK_DOWNLOAD_FREE + "/history",
            "§c",
            "§c(This message is only shown to server operators)"};

    public static void severe(final String[] text) {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(AngelChestMain.getInstance(), () -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                Messages.send(player, text);
            }
            for (final String line : text) {
                AngelChestMain.getInstance().getLogger().severe(line);
            }
        }, 0, Ticks.fromSeconds(30));

        Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, new Listener() {
        }, EventPriority.MONITOR, ((listener, event) -> {
            final PlayerJoinEvent playerJoinEvent = (PlayerJoinEvent) event;
            Bukkit.getScheduler().scheduleSyncDelayedTask(AngelChestMain.getInstance(), () -> Messages.send(playerJoinEvent.getPlayer(), text), Ticks.fromSeconds(0.5));
        }), AngelChestMain.getInstance());


    }

    public static void warnBrokenConfig() {

        if (AngelChestMain.getInstance().invalidConfigFiles == null) return;

        final String[] text = BROKEN_CONFIG_FILE.clone();

        for (final String file : AngelChestMain.getInstance().invalidConfigFiles) {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    for (final String line : text) {
                        AngelChestMain.getInstance().getLogger().warning(line.replace("{filename}", file));
                    }
                }
            }
            for (final String line : text) {
                AngelChestMain.getInstance().getLogger().warning(line.replace("{filename}", file));
            }
        }
    }

}
