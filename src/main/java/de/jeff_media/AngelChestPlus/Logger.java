package de.jeff_media.AngelChestPlus;

import de.jeff_media.AngelChestPlus.config.Config;
import jdk.internal.jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    Main main;
    String path;

    public Logger() {
        main=Main.getInstance();
        path = main.getDataFolder()+File.separator+"logs";
        if(!Files.isDirectory(new File(path).toPath())) {
            main.getLogger().info("Created log folder at "+path);
            new File(path).mkdirs();
        }
    }

    public String getLogFileName(PlayerDeathEvent event) {
        String player = event.getEntity().getName();
        String uuid = event.getEntity().getUniqueId().toString();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        //String filename = String.format("%s_%s_%s",event.getEntity().getLocation().getWorld().getName(),player,timestamp);
        //return filename+".log";
        return main.getConfig().getString(Config.LOG_FILENAME)
                .replaceAll("\\{player}",player)
                .replaceAll("\\{uuid}",uuid)
                .replaceAll("\\{world}",event.getEntity().getLocation().getWorld().getName())
                .replaceAll("\\{date}",timestamp);
    }

    public File getLogFile(PlayerDeathEvent event) {

        return new File(path+File.separator+getLogFileName(event));
    }

    private void write(String text, File file) {
        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(text);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            main.getLogger().severe("Could not write to logfile " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private void writeWithTime(String text, File file) {
        FileWriter fw = null;
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try {
            fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(String.format("[%s] %s",timestamp,text));
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            main.getLogger().severe("Could not write to logfile "+file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public File getLogFile(String name) {
        return new File(path+File.separator+name);
    }

    private String loc2string(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String world = location.getWorld().getName();
        return String.format("%d %d %d @ %s",x,y,z,world);
    }

    public void logItemTaken(Player player, @Nullable ItemStack item, File file) {
        if(item==null) return;
        writeWithTime(String.format("Player \"%s\" took item: %s",player.getName(),item.toString()),file);
    }

    public void logLastItemTaken(Player player, File file) {
        write("",file);
        writeWithTime(String.format("Player \"%s\" took the last item. Removing AngelChest!",player.getName()),file);
        write("",file);
        write("=== AngelChest removed ===",file);
    }

    public File logDeath(PlayerDeathEvent event, AngelChest ac) {
        File file = getLogFile(event);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        write("=== AngelChest spawned ===",file);
        write("Player: "+event.getEntity().getName(),file);
        write("Player UUID: "+event.getEntity().getUniqueId().toString(),file);
        write("Death Time: " + timestamp,file);
        write("Death Location: " + loc2string(event.getEntity().getLocation()),file);
        write("Chest Location: "+loc2string(ac.block.getLocation()),file);
        write("Death Cause: " + event.getEntity().getLastDamageCause().getCause().name(),file);
        write("Player XP: " + event.getEntity().getExp(),file);
        write("Chest XP: "+ac.experience,file);
        write("Chest Protection: "+ac.isProtected,file);
        write("Chest Duration: " + (ac.infinite ? "infinite" : ac.secondsLeft+" seconds"),file);
        int lineNo = 1;
        for(String line : ac.hologram.text.split("\n")) {
            write("Hologram Line "+lineNo+": " + line, file);
            lineNo++;
        }
        write("",file);
        write("=== INFORMATION ===",file);
        write("Please note that some plugins remove certain items on death (soulbound items etc.), while other plugins add certain drops (player heads etc.). That's why this log file show you three different item lists:",file);
        write("1. The original inventory as it was when the player died. This does not include 3rd party plugin drops, but includes items that would have been removed on death, e.g. curse of vanishing items.",file);
        write("2. The items the player would have dropped if AngelChest wouldn't be used. This does include 3rd party plugin drops but does not include non-droppable items like curse of vanishing items.",file);
        write("3. The final AngelChest contents, which is a combination of 1. and 2.: it includes all custom drops and does not include non-droppable items.",file);
        write("",file);

        write("=== Player Inventory ===",file);
        write("The player had the following items in his inventory at the time of his death:",file);
        for(ItemStack item : event.getEntity().getInventory().getContents()) {
            if(item==null) continue;
            write("> "+item.toString(),file);
        }
        write("",file);
        write("=== Player Drops ===", file);
        write("The player would have dropped the following items at the time of this death:",file);
        for(ItemStack item : event.getDrops()) {
            if(item==null) continue;
            write("> "+item.toString(),file);
        }
        write("",file);
        write("=== AngelChest inventory ===",file);
        write("The AngelChest contains the following items:",file);
        for(ItemStack item : ac.storageInv) {
            if(item==null) continue;
            write("> "+item.toString(),file);
        }
        for(ItemStack item : ac.armorInv) {
            if(item==null) continue;
            write("> "+item.toString(),file);
        }
        for(ItemStack item : ac.extraInv) {
            if(item==null) continue;
            write("> "+item.toString(),file);
        }
        for(ItemStack item : ac.overflowInv) {
            if(item==null) continue;
            write("> "+item.toString(),file);
        }
        write("",file);
        return file;
    }
}
