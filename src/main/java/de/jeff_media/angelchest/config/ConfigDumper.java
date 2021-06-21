package de.jeff_media.angelchest.config;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class ConfigDumper {

    private static String banner(final String header) {
        return StringUtils.center(" " + header + " ", 60, "=");
    }

    public static void dump(final CommandSender sender) {
        final Main main = Main.getInstance();
        final File dumpDir = new File(main.getDataFolder(), "dump.zip");
        if (dumpDir.exists()) {
            dumpDir.delete();
        }

        dumpDir.mkdir();

        final File log = new File(dumpDir, "server.txt");

        final File loadedConfig = new File(dumpDir, "loaded-config.txt");
        final File copiedConfig = new File(dumpDir, "original-config.yml");

        final File loadedBlacklist = new File(dumpDir, "loaded-blacklist.txt");
        final File copiedBlacklist = new File(dumpDir, "original-blacklist.yml");

        final File loadedGroups = new File(dumpDir, "loaded-groups.txt");
        final File copiedGroups = new File(dumpDir, "original-groups.yml");

        final File blacklist = new File(main.getDataFolder(), "blacklist.yml");
        final File groups = new File(main.getDataFolder(), "groups.yml");
        final File angelchestsDir = new File(main.getDataFolder(), "angelchests");


        // Delete old dump
        Messages.send(sender, "Cleaning up latest dump...");
        log.delete();
        loadedConfig.delete();
        loadedBlacklist.delete();
        loadedGroups.delete();
        try {
            FileUtils.deleteDirectory(new File(dumpDir, "angelchests"));
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }

        // Server information
        Messages.send(sender, "Saving server informationp...");
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, banner("Server information"));
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "Server Version: " + Bukkit.getVersion());
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "Bukkit API Version: " + Bukkit.getBukkitVersion());
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "Plugin version: " + main.getDescription().getName() + (Daddy.allows(PremiumFeatures.GENERIC) ? "Plus" : "") + " " + main.getDescription().getVersion());

        // Broken config files
        Messages.send(sender, "Saving config checkp...");
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "\n" + banner("Config check"));
        if (main.invalidConfigFiles == null || main.invalidConfigFiles.length == 0) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "Config OK.");
        } else {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "Broken config files: " + StringUtils.join(main.invalidConfigFiles, ", "));
        }
        if (!blacklist.exists()) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "blacklist.yml does not exist");
        }
        if (!groups.exists()) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "groups.yml does not exist");
        }

        // Other plugins
        Messages.send(sender, "Saving plugin list...");
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "\n" + banner("Installed plugins"));
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, plugin.getName() + " " + plugin.getDescription().getVersion() + (plugin.isEnabled() ? "" : " (DISABLED)"));
        }

        // Gamerules
        Messages.send(sender, "Saving relevant gamerules...");
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "\n" + banner("Gamerules"));
        for (final World world : Bukkit.getWorlds().stream().sorted(Comparator.comparing(World::getName)).collect(Collectors.toList())) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, world.getName() + "[" + world.getUID() + "]");
            @SuppressWarnings("rawtypes") final GameRule[] rules = new GameRule[]{GameRule.DO_ENTITY_DROPS, GameRule.KEEP_INVENTORY};
            //noinspection rawtypes
            for (final GameRule rule : rules) {
                //noinspection unchecked
                de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "- " + rule.getName() + ": " + world.getGameRuleValue(rule).toString());
            }
        }


        // Online player's permissions
        Messages.send(sender, "Saving online player's permissionsp...");
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "\n" + banner("Player Permissions"));
        for (final Player player : Bukkit.getOnlinePlayers()) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, player.getName());
            for (final Permission permission : main.getDescription().getPermissions()) {
                de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "- " + permission.getName() + ": " + player.hasPermission(permission));
            }
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "- essentials.keepinv: " + player.hasPermission("essentials.keepinv"));
        }

        // Scheduled tasks
        Messages.send(sender, "Saving BukkitScheduler informationp...");
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "\n" + banner("BukkitScheduler: Workers"));
        for (final BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, worker.getOwner().getName() + ": " + worker.getTaskId() + " (" + worker + ")");
        }
        de.jeff_media.angelchest.utils.FileUtils.appendLines(log, "\n" + banner("BukkitScheduler: Tasks"));
        for (final BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(log, task.getOwner().getName() + ": " + task.getTaskId() + " (" + task + ")");
        }

        // Dump configs
        try {
            Messages.send(sender, "Copying config.yml...");
            org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(new File(main.getDataFolder(), "config.yml"), copiedConfig);
            Messages.send(sender, "Dumping loaded config.yml");
            dumpYaml(main.getConfig(), loadedConfig);
            if (groups.exists()) {
                Messages.send(sender, "Copying groups.yml...");
                org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(groups, copiedGroups);
                Messages.send(sender, "Dumping loaded groups.yml...");
                final YamlConfiguration groupsYaml = new YamlConfiguration();
                try {
                    groupsYaml.load(groups);
                    dumpYaml(groupsYaml, loadedGroups);
                } catch (final InvalidConfigurationException e) {
                    e.printStackTrace();
                }
            }
            if (blacklist.exists()) {
                Messages.send(sender, "Copying blacklist.yml...");
                org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(blacklist, copiedBlacklist);
                Messages.send(sender, "Dumping loaded blacklist.yml...");
                final YamlConfiguration blacklistYaml = new YamlConfiguration();
                try {
                    blacklistYaml.load(blacklist);
                    dumpYaml(blacklistYaml, loadedBlacklist);
                } catch (final InvalidConfigurationException e) {
                    e.printStackTrace();
                }
            }
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }

        // Dump AngelChests
        Messages.send(sender, "Saving and copying AngelChests...");
        final Collection<File> existingChests = FileUtils.listFiles(angelchestsDir, null, false);
        main.saveAllAngelChestsToFile(false);
        final Collection<File> allChests = FileUtils.listFiles(angelchestsDir, null, false);
        try {
            FileUtils.copyDirectory(angelchestsDir, new File(dumpDir, "angelchests"));
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }
        for (final File file : allChests) {
            if (existingChests.contains(file)) continue;
            file.delete();
        }

        // Latest.log
        Messages.send(sender, "Copying latest.log...");
        try {
            org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(new File(new File(main.getDataFolder().getParentFile().getParentFile(), "logs"), "latest.log"), new File(dumpDir, "latest.log"));
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        }

        // debug.log
        if (new File(main.getDataFolder(), "debug.log").exists()) {
            Messages.send(sender, "Copying debug.log...");
            try {
                org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(new File(main.getDataFolder(), "debug.log"), new File(dumpDir, "debug.log"));
            } catch (final IOException ioException) {
                ioException.printStackTrace();
            }
        }


        Messages.send(sender, "Compressing all files into zip archive...");
        ZipUtil.unexplode(dumpDir);
        Messages.send(sender, "Cleaning up...");
        Messages.send(sender, "Done!");
    }

    private static void dumpYaml(final FileConfiguration input, final File output) {
        // Get max length
        int maxLength = 20;
        for (final String node : input.getKeys(true)) {
            maxLength = Math.max(node.length(), maxLength);
            String value = input.get(node).toString();
            if (value == null) continue;
            if (value.contains("\n")) value = value.split("\n")[0];
            maxLength = Math.max(value.length(), maxLength);
        }
        if (maxLength > 230) maxLength = 230;

        // Go for it
        for (final String node : input.getKeys(true).stream().sorted().collect(Collectors.toList())) {
            de.jeff_media.angelchest.utils.FileUtils.appendLines(output, "ᐁ " + StringUtils.center("  " + node + "  ", maxLength, ". ") + " ᐁ");
            de.jeff_media.angelchest.utils.FileUtils.appendLines(output, input.get(node) + "\n\n");
        }
    }

}
