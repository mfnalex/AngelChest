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
import de.jeff_media.angelchest.utils.Prompt;
import de.jeff_media.daddy.Daddy_Stepsister;
import com.jeff_media.jefflib.LocationUtils;
import com.jeff_media.jefflib.ParticleUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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

        if (box == null) {
            player.sendMessage("§cYou must install WorldEdit and have an active selection!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cYou must define a name for this graveyard!");
            return;
        }

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Block max = box.getMaxBlock();
        Block min = box.getMinBlock();

        if (!max.getWorld().equals(min.getWorld())) {
            throw new IllegalStateException("Both corners must be in the same world!");
        }

        if (GraveyardYamlManager.createGraveyard(name, min, max)) {
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

        if (args.length <= 1) {
            return Arrays.asList("create", "list", "setmaterial", "addgravelocation", "removegravelocation", "info", "delete", "setspawn", "sethologramtext",
                    "addpotioneffect", "removepotioneffect", "settime", "setweather", "setinstantrespawn","setglobal");
        } else if (args.length == 2 &&
                (args[0].equalsIgnoreCase("setmaterial")
                        || args[0].equalsIgnoreCase("addgravelocation")
                        || args[0].equalsIgnoreCase("removegravelocation"))) {
            return GenericTabCompleter.getBlockMaterials(args[1]);
        } else if (args.length == 2 &&
                (args[0].equalsIgnoreCase("addpotioneffect")
                        || args[0].equalsIgnoreCase("removepotioneffect"))) {
            return GenericTabCompleter.getPotionEffectTypes(args[1]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setweather")) {
            return Arrays.asList("sun", "rain");
        } else if(args.length==2
                && (args[0].equalsIgnoreCase("setinstantrespawn")
                        || args[0].equalsIgnoreCase("setglobal"))) {
            return Arrays.asList("true","false");
        }
        return null;
    }

    public void addSpawnOn(Player player, Graveyard yard, String[] args) {
        Material mat = getMatOrNull(player, args, 1);
        if (mat == null) return;
        if (!yard.getSpawnOn().contains(mat)) {
            yard.getSpawnOn().add(mat);
        }
        GraveyardYamlManager.updateSpawnOn(yard);
        player.sendMessage("§b" + mat.name() + " §ahas been added to the list of valid grave spawn blocks for §b" + yard.getName());
    }

    public void removeSpawnOn(Player player, Graveyard yard, String[] args) {
        Material mat = getMatOrNull(player, args, 1);
        if (mat == null) return;
        yard.getSpawnOn().remove(mat);
        GraveyardYamlManager.updateSpawnOn(yard);
        player.sendMessage("§b" + mat.name() + " §ahas been removed from the list of valid grave spawn blocks for §b" + yard.getName());
    }

    private Material getMatOrNull(Player player, String[] args, int index) {
        if (args.length < index + 1) {
            player.sendMessage("§cYou must specify a block material.");
            return null;
        }
        Material mat = Enums.getIfPresent(Material.class, args[index].toUpperCase(Locale.ROOT)).orNull();
        if (mat == null) {
            player.sendMessage("§cUnknown block: " + args[index].toUpperCase(Locale.ROOT));
            return null;
        }
        return mat;
    }

    private BlockData getBlockDataOrNull(Player player, String[] args, int index) {
        Block lookingAt = player.getTargetBlockExact(16, FluidCollisionMode.NEVER);
        if (lookingAt == null && args.length < index + 1) {
            player.sendMessage("§cYou must specify a block material or look at an existing block.");
            return null;
        }
        BlockData data;
        if (args.length == index + 1) {
            Material mat = Enums.getIfPresent(Material.class, args[index].toUpperCase(Locale.ROOT)).orNull();
            if (mat == null) {
                player.sendMessage("§cUnknown block: " + args[index].toUpperCase(Locale.ROOT));
                return null;
            }
            data = Bukkit.getServer().createBlockData(mat);
        } else {
            data = lookingAt.getBlockData();
        }
        return data;
    }

    public void setMaterial(Player player, Graveyard yard, String[] args) {
        BlockData mat = getBlockDataOrNull(player, args, 1);
        if (mat == null) return;
        GraveyardYamlManager.setMaterial(yard, mat);
        //player.sendMessage("§aSet chest material for §b" + yard.getName() + " §ato §b" + mat.getAsString());
        Prompt.showSuccess(player, "material",mat.getAsString(), yard);
        showReloadNotice(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!Daddy_Stepsister.allows(PremiumFeatures.GRAVEYARDS)) {
            commandSender.sendMessage(main.messages.MSG_PREMIUMONLY);
            return true;
        }

        if (args.length == 0) {
            help(commandSender);
            return true;
        }

        /*
        Console commands
         */

        switch (args[0].toLowerCase(Locale.ROOT)) {
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
        if (yard == null) {
            player.sendMessage("§cThere are no graveyards in this world.");
            return true;
        }

        /*
        Player commands requiring a nearby graveyard
         */

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "delete":
                deleteGraveyard(player, yard);
                return true;
            case "setmaterial":
                setMaterial(player, yard, args);
                return true;
            case "addgravelocation":
                addSpawnOn(player, yard, args);
                return true;
            case "removegravelocation":
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
            case "settime":
                setTime(player, yard, args);
                return true;
            case "setweather":
                setWeather(player, yard, args);
                return true;
            case "setinstantrespawn":
                setInstantRespawn(player, yard, args);
                return true;
            case "setglobal":
                setGlobal(player, yard, args);
                return true;
            case "info":
                info(player, yard, args);
                return true;
        }

        help(commandSender);

        return true;
    }

    private void setGlobal(Player player, Graveyard yard, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cToo few arguments.");
            return;
        }
        Boolean bool = Prompt.getBoolean(player, args[1]);
        if (bool == null) return;
        GraveyardYamlManager.setGlobal(yard, bool);
        Prompt.showSuccess(player, "global", String.valueOf(bool),yard);
    }

    private void setInstantRespawn(Player player, Graveyard yard, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cToo few arguments.");
            return;
        }
        Boolean bool = Prompt.getBoolean(player, args[1]);
        if (bool == null) return;
        GraveyardYamlManager.setInstantRespawn(yard, bool);
        Prompt.showSuccess(player, "instant-respawn", String.valueOf(bool),yard);
    }

    private void setTime(Player player, Graveyard yard, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§aReset §blocal-time §ain §b"+yard.getName());
            GraveyardYamlManager.setTime(yard, null);
        } else {
            try {
                long time = Long.parseLong(args[1]);
                //player.sendMessage("§aLocal time for graveyard §b" + yard.getName() + " §has been set to §b" + time);
                Prompt.showSuccess(player, "local-time", String.valueOf(time), yard);
                GraveyardYamlManager.setTime(yard, time);
            } catch (Exception exception) {
                player.sendMessage("§c" + args[1] + " is not a valid number.");
            }
        }
    }

    // TODO: add
    private void setWeather(Player player, Graveyard yard, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§aReset §blocal-weather §ain §b"+yard.getName());
            GraveyardYamlManager.setWeather(yard, null);
        } else {
            WeatherType weatherType;
            if (args[1].equalsIgnoreCase("sun")) weatherType = WeatherType.CLEAR;
            else if (args[1].equalsIgnoreCase("rain")) weatherType = WeatherType.DOWNFALL;
            else {
                player.sendMessage("§c" + args[1] + " is not a valid weather type. Must be §bsun§c or §brain§b.");
                return;
            }
            //player.sendMessage("§aLocal weather for graveyard §b" + yard.getName() + " §has been set to §b" + weatherType.name());
            Prompt.showSuccess(player, "local-weather", args[1].toLowerCase(Locale.ROOT), yard);
            GraveyardYamlManager.setWeather(yard, weatherType);
        }
    }

    private PotionEffectType getPotionEffectFromArgs(Player player, String[] args, int index) {
        args = Arrays.copyOfRange(args, 1, args.length);
        if (args.length == 0) {
            player.sendMessage("§cYou must enter a potion effect type.");
            return null;
        }
        PotionEffectType type = PotionEffectType.getByName(args[0].toUpperCase(Locale.ROOT));
        if (type == null) {
            player.sendMessage("§c" + args[0] + " is not a valid potion effect type.");
            return null;
        }
        return type;
    }

    private void removePotionEffect(Player player, Graveyard yard, String[] args) {
        PotionEffectType type = getPotionEffectFromArgs(player, args, 1);
        if (type == null) return;
        GraveyardYamlManager.removePotionEffect(yard, type);
        player.sendMessage("§aRemoved potion effect §b" + type + " §a from §b" + yard.getName());
    }

    private void addPotionEffect(Player player, Graveyard yard, String[] args) {
        PotionEffectType type = getPotionEffectFromArgs(player, args, 1);
        if (type == null) return;
        args = Arrays.copyOfRange(args, 1, args.length);

        Integer amplifier = 0;
        if (args.length == 2) {
            amplifier = Prompt.getPositiveInteger(player, args[1]);
            if(amplifier==null) return;
        }
        GraveyardYamlManager.addPotionEffect(yard, new PotionEffect(type, Integer.MAX_VALUE, amplifier));
        player.sendMessage("§aAdded potion effect §b" + type + "§a (amplifier §b" + amplifier + "§a) to §b" + yard.getName());
    }

    private void setHologramText(Player player, Graveyard yard, String[] args) {
        args = Arrays.copyOfRange(args, 1, args.length);
        if (args.length == 0) {
            player.sendMessage("§aRemoved §bhologram-text §afrom §b" + yard.getName());
            GraveyardYamlManager.setHologramText(yard, null);
        } else {
            StringBuilder hologram = new StringBuilder();
            Iterator<String> it = Arrays.stream(args).iterator();
            while (it.hasNext()) {
                hologram.append(it.next());
                if (it.hasNext()) hologram.append(" ");
            }
            //player.sendMessage("§aSet §bhologram-text §ato §b" + hologram + "§a in §b" + yard.getName());
            Prompt.showSuccess(player,"hologram-text",hologram.toString(),yard);
            GraveyardYamlManager.setHologramText(yard, hologram.toString());
        }
    }

    private void setSpawn(Player player, Graveyard yard) {
        //player.sendMessage("§aSet §bspawn-point§a to §b" + LocationUtils.toPrettyString(player.getLocation(), true, true) + "§a in §b" + yard.getName());
        Prompt.showSuccess(player,"spawn-point",LocationUtils.toPrettyString(player.getLocation(),true,true),yard);
        GraveyardYamlManager.setSpawn(player, yard);
    }

    private void deleteGraveyard(Player player, Graveyard yard) {
        GraveyardYamlManager.deleteGraveyard(yard);
        player.sendMessage("§aGraveyard " + yard.getName() + " has been §cdeleted§a.");
        showReloadNotice(player);
    }

    private void info(Player player, Graveyard yard, String[] args) {
        for (String[] entry : yard.toPrettyString()) {
            player.sendMessage("§6" + entry[0] + "§r: " + entry[1]);
        }
        ParticleUtils.drawHollowCube(yard.getWorldBoundingBox().getWorld(), yard.getWorldBoundingBox().getBoundingBox(), player, Particle.COMPOSTER, 1,null).runTaskTimer(main, 0, 20);
        if (yard.getSpawnOn().size() != 0) {
            new BlockMarkerTask(yard, player).runTaskTimer(main, 0, 10);
        }
    }

    private void help(CommandSender commandSender) {
        Messages.send(commandSender, "§a§l§nGeneral graveyards commands:",
                "§6§l/acgraveyard info","Shows information, highlights its area and the valid grave locations"," ",
                "§6§l/acgraveyard list","Lists all graveyards"," ",
                "§6§l/acgraveyard create <name>","Creates a new graveyard from your WorldEdit selection"," ",
                "§a§l§nCommands affecting the nearest graveyard:",
                "§6§l/acgraveyard delete","Deletes this graveyard"," ",
                "§6§l/acgraveyard setglobal <true|false>","Sets whether this graveyard is the global graveyard"," ",
                "§6§l/acgraveyard setspawn","Sets the spawn point"," ",
                "§6§l/acgraveyard setinstantrespawn <true|false>","Sets whether players will be instantly respawned"," ",
                "§6§l/acgraveyard setmaterial [block]","Sets the chest material to the given block, or the block you are looking at"," ",
                "§6§l/acgraveyard addgravelocation <block>","Adds a material to the list of valid grave locations"," ",
                "§6§l/acgraveyard removegravelocation <block>","Removes a material from the list of valid grave locations"," ",
                "§6§l/acgraveyard sethologramtext [text]","Sets or resets the hologram text"," ",
                "§6§l/acgraveyard addpotioneffect <potionType> [multiplier]","Adds a potion effect"," ",
                "§6§l/acgraveyard removepotioneffect <potionType>","Removes a potion effect"," ",
                "§6§l/acgraveyard settime [time]","Sets or resets the local time"," ",
                "§6§l/acgraveyard setweather [sun|rain]","Sets or resets the local weather");
    }

    private void listGraveyards(CommandSender player) {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            Collection<Graveyard> graveyards = GraveyardManager.getGraveyards(world);
            if (graveyards.isEmpty()) continue;
            player.sendMessage("§6=== §b" + world.getName() + "§6 ===");
            for (Graveyard graveyard : graveyards) {
                player.sendMessage("- " + graveyard.getName());
                total++;
            }
        }
        if (total == 0) {
            player.sendMessage("§cYou didn't create any graveyards yet.");
        }
    }
}
