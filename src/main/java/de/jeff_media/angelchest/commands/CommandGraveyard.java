package de.jeff_media.angelchest.commands;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.angelchest.data.WorldBoundingBox;
import de.jeff_media.angelchest.debug.tasks.BlockMarkerTask;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.handlers.GraveyardManager;
import de.jeff_media.angelchest.handlers.GraveyardYamlManager;
import de.jeff_media.angelchest.hooks.WorldEditWrapper;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.ParticleUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static de.jeff_media.angelchest.config.Messages.showReloadNotice;


public class CommandGraveyard implements CommandExecutor, TabCompleter {

    private static final Main main = Main.getInstance();
    private static final File FILE = new File(main.getDataFolder(), "graveyards.yml");

    public CommandGraveyard() {
        Objects.requireNonNull(main.getCommand("acgraveyard")).setTabCompleter(this);
    }

    private void createGraveyard(Player player, String[] args) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(FILE);
        config.options().copyHeader(true);

        WorldBoundingBox box = WorldEditWrapper.getSelection(player);

        if(box == null) {
            player.sendMessage("§cYou must install WorldEdit and have an active selection!");
            return;
        }

        if(args.length < 2) {
            player.sendMessage("§cYou must define a name for this graveyard!");
            return;
        }

        String name = String.join(" ",Arrays.copyOfRange(args,1,args.length));

        Block max = box.getMaxBlock();
        Block min = box.getMinBlock();

        if(!max.getWorld().equals(min.getWorld())) {
            throw new IllegalStateException("Both corners must be in the same world!");
        }

        if(GraveyardYamlManager.createGraveyard(name,min,max)) {
            player.sendMessage("§aCreated Graveyard §b" + name + "§a.");
        } else {
            player.sendMessage("§cGraveyard §b" + name + "§c already exists.");
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        /*System.out.println("TAB");
        for(int i = 0; i < args.length; i++) {
            System.out.println("  args["+i+"]: " + args[i]);
        }*/

        if(args.length <= 1) {
            return Arrays.asList("create","list","setmaterial","addspawnon","removespawnon","info","delete","setspawn","sethologramtext",
                    "addpotioneffect","removepotioneffect");
        } else if(args.length == 2 &&
                (args[0].equalsIgnoreCase("setmaterial")
                || args[0].equalsIgnoreCase("addspawnon")
                || args[0].equalsIgnoreCase("removespawnon"))) {
            return GenericTabCompleter.getBlockMaterials(args[1]);
        } else if(args.length ==2 &&
                (args[0].equalsIgnoreCase("addpotioneffect")
                || args[0].equalsIgnoreCase("removepotioneffect"))) {
            return GenericTabCompleter.getPotionEffectTypes(args[1]);
        }
        return null;
    }

    public void addSpawnOn(Player player, Graveyard yard, String[] args) {
        Material mat = getMatOrNull(player, args, 1);
        if(mat==null) return;
        if(!yard.getSpawnOn().contains(mat)) {
            yard.getSpawnOn().add(mat);
        }
        GraveyardYamlManager.updateSpawnOn(yard);
        player.sendMessage("§b"+mat.name()+" §ahas been §4added §ato the list of valid grave spawn blocks for §b"+yard.getName());
    }

    public void removeSpawnOn(Player player, Graveyard yard, String[] args) {
        Material mat = getMatOrNull(player, args, 1);
        if(mat==null) return;
        yard.getSpawnOn().remove(mat);
        GraveyardYamlManager.updateSpawnOn(yard);
        player.sendMessage("§b"+mat.name()+" §ahas been §cremoved §afrom the list of valid grave spawn blocks for §b"+yard.getName());
    }

    private Material getMatOrNull(Player player, String[] args, int index) {
        if(args.length < index+1) {
            player.sendMessage("§cYou must specify a block material for the graves.");
            return null;
        }
        Material mat = Enums.getIfPresent(Material.class,args[index].toUpperCase(Locale.ROOT)).orNull();
        if(mat == null) {
            player.sendMessage("§cUnknown block: " + args[index].toUpperCase(Locale.ROOT));
            return null;
        }
        return mat;
    }

    public void setMaterial(Player player, Graveyard yard, String[] args) {
        Material mat = getMatOrNull(player, args, 1);
        if(mat==null) return;
        GraveyardYamlManager.setMaterial(yard,mat);
        player.sendMessage("§aSet chest material for §b" + yard.getName()+ " §ato §b" + mat.name());
        showReloadNotice(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(!Daddy.allows(PremiumFeatures.GRAVEYARDS)) {
            commandSender.sendMessage(main.messages.MSG_PREMIUMONLY);
            return true;
        }

        if(args.length==0) {
            help(commandSender);
            return true;
        }

        /*
        Console commands
         */

        switch(args[0].toLowerCase(Locale.ROOT)) {
            case "list":
                listGraveyards(commandSender);
                return true;
        }

        if (!commandSender.hasPermission(Permissions.ADMIN)) {
            Messages.send(commandSender, main.messages.MSG_NO_PERMISSION);
            return true;
        }

        Player player = (Player) commandSender;

        /*
        Player commands
         */

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create":
                createGraveyard(player, args);
                return true;
        }

        Graveyard yard = GraveyardManager.getNearestGraveyard(player.getLocation());
        if(yard == null) {
            player.sendMessage("§cThere are no graveyards in this world.");
            return true;
        }

        /*
        Player commands requiring a nearby graveyard
         */

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create":
                createGraveyard(player, args);
                return true;
            case "delete":
                deleteGraveyard(player, yard);
                return true;
            case "setmaterial":
                setMaterial(player, yard, args);
                return true;
            case "addspawnon":
                addSpawnOn(player, yard, args);
                return true;
            case "removespawnon":
                removeSpawnOn(player, yard, args);
                return true;
            case "setspawn":
                setSpawn(player, yard);
                return true;
            case "sethologramtext":
                setHologramText(player, yard, args);
                return true;
            case "addpotioneffect":
                addPotionEffect(player, yard, args);
                return true;
            case "removepotioneffect":
                removePotionEffect(player, yard, args);
                return true;
            case "info":
                info(player, yard, args);
                return true;
        }

        help(commandSender);

        return true;
    }

    private PotionEffectType getPotionEffectFromArgs(Player player, String args[], int index) {
        args = Arrays.copyOfRange(args, 1, args.length);
        if(args.length == 0) {
            player.sendMessage("§cYou must enter a potion effect type.");
            return null;
        }
        PotionEffectType type = PotionEffectType.getByName(args[0].toUpperCase(Locale.ROOT));
        if(type == null) {
            player.sendMessage("§c"+args[0] + " is not a valid potion effect type.");
            return null;
        }
        return type;
    }

    private void removePotionEffect(Player player, Graveyard yard, String[] args) {
        PotionEffectType type = getPotionEffectFromArgs(player, args, 1);
        if(type == null) return;
        GraveyardYamlManager.removePotionEffect(yard, type);
    }

    private void addPotionEffect(Player player, Graveyard yard, String[] args) {
        PotionEffectType type = getPotionEffectFromArgs(player, args, 1);
        if(type == null) return;
        args = Arrays.copyOfRange(args, 1, args.length);

        int amplifier = 1;
        if(args.length==2) {
            try {
                amplifier = Integer.parseInt(args[1]);
            } catch (Exception exception) {
                player.sendMessage("§c"+args[1]+" is not a valid integer.");
                return;
            }
        }
        GraveyardYamlManager.addPotionEffect(yard, new PotionEffect(type, Integer.MAX_VALUE, amplifier));
        player.sendMessage("§aAdded potion effect §b" + type + "§a (amplifier §b" + amplifier + "§a) to graveyard §b" + yard.getName());
    }

    private void setHologramText(Player player, Graveyard yard, String[] args) {
        args = Arrays.copyOfRange(args,1,args.length);
        if(args.length == 0) {
            player.sendMessage("§aCustom hologram text §cremoved §afor graveyard §b" + yard.getName());
            GraveyardYamlManager.setHologramText(yard, null);
        } else {
            for(String arg : args) {
                System.out.println("arg[]="+arg);
            }
            StringBuilder hologram = new StringBuilder();
            Iterator<String> it = Arrays.stream(args).iterator();
            while(it.hasNext()) {
                hologram.append(it.next());
                if(it.hasNext()) hologram.append(" ");
            }
            player.sendMessage("§aCustom hologram text for graveyard §b"+yard.getName()+"§a set to §b" + hologram);
            GraveyardYamlManager.setHologramText(yard, hologram.toString());
        }
    }

    private void setSpawn(Player player, Graveyard yard) {
        GraveyardYamlManager.setSpawn(player, yard);
    }

    private void deleteGraveyard(Player player, Graveyard yard) {
        GraveyardYamlManager.deleteGraveyard(yard);
        player.sendMessage("§aGraveyard " + yard.getName() + " has been §cdeleted§a.");
        showReloadNotice(player);
    }

    private void info(Player player, Graveyard yard, String[] args) {
        for(String[] entry : yard.toPrettyString()) {
            player.sendMessage("§6"+entry[0]+"§r: "+entry[1]);
        }
        ParticleUtils.drawHollowCube(yard.getWorldBoundingBox().getWorld(), yard.getWorldBoundingBox().getBoundingBox(), player, Particle.BARRIER, 1).runTaskTimer(main, 0, 20);
        if(yard.getSpawnOn().size()!=0) {
            new BlockMarkerTask(yard, player).runTaskTimer(main, 0, 10);
        }
    }

    private void help(CommandSender commandSender) {
        Messages.send(commandSender, "§eAvailable commands:",
                "/acgraveyard info §6Shows information, hightlights its area and the valid grave locations",
                "/acgraveyard list §6Lists all graveyards",
                "/acgraveyard create <name> §6Creates a new graveyard from your WorldEdit selection",
                "/acgraveyard delete §cDeletes§6 the nearest graveyard",
                "/acgraveyard setspawn §6Sets the spawn point for the nearest graveyard",
                "/acgraveyard setmaterial <block> §6Sets the chest material for the nearest graveyard",
                "/acgraveyard addspawnon <block> §6Adds a material to the list of valid grave ground blocks for the nearest graveyard",
                "/acgraveyard removespawnon <block> §6Removes a material to the list of valid grave ground blocks for the nearest graveyard",
                "/acgraveyard sethologramtext <text> §6Sets the hologram text for the nearest graveyard",
                "/acgraveyard addpotioneffect §6Adds a potion effect to the nearest graveyard",
                "/acgraveyard removepotioneffect §6Removes a potion effect from the nearest graveyard");
    }

    private void listGraveyards(CommandSender player) {
        int total = 0;
        for(World world : Bukkit.getWorlds()) {
            Collection<Graveyard> graveyards = GraveyardManager.getGraveyards(world);
            if(graveyards.isEmpty()) continue;
            player.sendMessage("§6=== §b"+world.getName()+"§6 ===");
            for(Graveyard graveyard : graveyards) {
                player.sendMessage("- " + graveyard.getName());
                total++;
            }
        }
        if(total == 0) {
            player.sendMessage("§cYou didn't create any graveyards yet.");
        }
    }
}
