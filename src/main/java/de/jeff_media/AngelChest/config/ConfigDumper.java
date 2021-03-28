package de.jeff_media.AngelChest.config;

import de.jeff_media.AngelChest.Main;
import de.jeff_media.AngelChest.enums.Features;
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

import static de.jeff_media.AngelChest.utils.FileUtils.appendLines;

public class ConfigDumper {

    public static void dump(CommandSender sender) {
        Main main = Main.getInstance();
        File dumpDir = new File(main.getDataFolder(),"dump.zip");
        if(dumpDir.exists()) {
            dumpDir.delete();
        }

        dumpDir.mkdir();

        File log = new File(dumpDir,"server.txt");

        File loadedConfig = new File(dumpDir,"loaded-config.txt");
        File copiedConfig = new File(dumpDir,"original-config.yml");

        File loadedBlacklist = new File(dumpDir,"loaded-blacklist.txt");
        File copiedBlacklist = new File(dumpDir,"original-blacklist.yml");

        File loadedGroups = new File(dumpDir,"loaded-groups.txt");
        File copiedGroups = new File(dumpDir,"original-groups.yml");

        File blacklist = new File(main.getDataFolder(),"blacklist.yml");
        File groups = new File(main.getDataFolder(),"groups.yml");
        File angelchestsDir = new File(main.getDataFolder(),"angelchests");
        

        // Delete old dump
        sender.sendMessage("Cleaning up latest dump...");
        log.delete();
        loadedConfig.delete();
        loadedBlacklist.delete();
        loadedGroups.delete();
        try {
            FileUtils.deleteDirectory(new File(dumpDir,"angelchests"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // Server information
        sender.sendMessage("Saving server information to log.txt...");
        appendLines(log,banner("Server information"));
        appendLines(log,"Server Version: "+Bukkit.getVersion());
        appendLines(log,"Bukkit API Version: "+Bukkit.getBukkitVersion());
        appendLines(log,"Plugin version: " + main.getDescription().getName()+(Daddy.allows(Features.GENERIC) ? "Plus" : "")+ " "+main.getDescription().getVersion());

        // Broken config files
        sender.sendMessage("Saving config check to log.txt...");
        appendLines(log,"\n"+banner("Config check"));
        if(main.invalidConfigFiles==null || main.invalidConfigFiles.length==0) {
            appendLines(log,"Config OK.");
        } else {
            appendLines(log,"Broken config files: " + StringUtils.join(main.invalidConfigFiles,", "));
        }
        if(!blacklist.exists()) {
            appendLines(log,"blacklist.yml does not exist");
        }
        if(!groups.exists()) {
            appendLines(log,"groups.yml does not exist");
        }

        // Other plugins
        sender.sendMessage("Saving plugin list to log.txt...");
        appendLines(log,"\n"+banner("Installed plugins"));
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            appendLines(log,plugin.getName()+" "+plugin.getDescription().getVersion()
                    + (plugin.isEnabled() ? "" : " (DISABLED)"));
        }

        // Gamerules
        sender.sendMessage("Saving relevant gamerules...");
        appendLines(log,"\n"+banner("Gamerules"));
        for(World world : Bukkit.getWorlds().stream()
                .sorted(Comparator.comparing(World::getName))
                .collect(Collectors.toList())) {
            appendLines(log,world.getName() + "["+world.getUID().toString()+"]");
            @SuppressWarnings("rawtypes") GameRule[] rules = new GameRule[] {GameRule.DO_ENTITY_DROPS, GameRule.KEEP_INVENTORY};
            //noinspection rawtypes
            for(GameRule rule : rules) {
                appendLines(log,"- "+rule.getName()+": "+world.getGameRuleValue(rule).toString());
            }
        }

        // Scheduled tasks
        sender.sendMessage("Saving BukkitScheduler information to log.txt...");
        appendLines(log,"\n"+banner("BukkitScheduler: Workers"));
        for(BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
            appendLines(log,worker.getOwner().getName()+": "+worker.getTaskId()+" ("+worker.toString()+")");
        }
        appendLines(log,"\n"+banner("BukkitScheduler: Tasks"));
        for(BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
            appendLines(log, task.getOwner().getName()+": "+task.getTaskId()+" ("+task.toString()+")");
        }

        // Online player's permissions
        sender.sendMessage("Saving online player's permissions to log.txt...");
        appendLines(log,"\n"+banner("Player Permissions"));
        for(Player player : Bukkit.getOnlinePlayers()) {
            appendLines(log,player.getName());
            for(Permission permission : main.getDescription().getPermissions()) {
                appendLines(log,"- "+permission.getName()+": "+player.hasPermission(permission));
            }
            appendLines(log,"- essentials.keepinv: "+player.hasPermission("essentials.keepinv"));
        }

        // Dump configs
        try {
            sender.sendMessage("Copying config.yml...");
            org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(new File(main.getDataFolder(),"config.yml"),copiedConfig);
            sender.sendMessage("Dumping loaded config.yml");
            dumpYaml(main.getConfig(),loadedConfig);
            if(groups.exists()) {
                sender.sendMessage("Copying groups.yml...");
                org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(groups, copiedGroups);
                sender.sendMessage("Dumping loaded groups.yml...");
                YamlConfiguration groupsYaml = new YamlConfiguration();
                try {
                    groupsYaml.load(groups);
                    dumpYaml(groupsYaml,loadedGroups);
                } catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }
            }
            if(blacklist.exists()) {
                sender.sendMessage("Copying blacklist.yml...");
                org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(blacklist, copiedBlacklist);
                sender.sendMessage("Dumping loaded blacklist.yml...");
                YamlConfiguration blacklistYaml = new YamlConfiguration();
                try {
                    blacklistYaml.load(blacklist);
                    dumpYaml(blacklistYaml,loadedBlacklist);
                } catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // Dump AngelChests
        sender.sendMessage("Saving and copying AngelChests...");
        Collection<File> existingChests = FileUtils.listFiles(angelchestsDir,null,false);
        main.saveAllAngelChestsToFile(false);
        Collection<File> allChests = FileUtils.listFiles(angelchestsDir, null, false);
        try {
            FileUtils.copyDirectory(angelchestsDir,new File(dumpDir,"angelchests"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        for(File file : allChests) {
            if(existingChests.contains(file)) continue;
            file.delete();
        }

        // Latest.log
        sender.sendMessage("Copying latest.log...");
        try {
            org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils.copyFile(
                    new File(new File(main.getDataFolder().getParentFile().getParentFile(),"logs"),"latest.log"),
                    new File(dumpDir,"latest.log"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        sender.sendMessage("Compressing all files into zip archive...");
        ZipUtil.unexplode(dumpDir);
        sender.sendMessage("Cleaning up...");
        sender.sendMessage("Done!");
    }

    private static String banner(String header) {
        return StringUtils.center(" "+header+" ",60,"=");
    }

    private static void dumpYaml(FileConfiguration input, File output) {
        // Get max length
        int maxLength = 20;
        for(String node : input.getKeys(true)) {
            maxLength = Math.max(node.length(),maxLength);
            String value = input.get(node).toString();
            if(value==null) continue;
            if(value.contains("\n")) value = value.split("\n")[0];
            maxLength = Math.max(value.length(),maxLength);
        }
        if(maxLength>230) maxLength=230;

        // Go for it
        for(String node : input.getKeys(true).stream().sorted().collect(Collectors.toList())) {
            appendLines(output,"ᐁ "+StringUtils.center("  "+node+"  ",maxLength,". ")+" ᐁ");
            appendLines(output,input.get(node)+"\n\n");
        }
    }

}
