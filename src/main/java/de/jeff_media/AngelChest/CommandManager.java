package de.jeff_media.AngelChest;

import de.jeff_media.AngelChest.utils.FileUtils;
import de.jeff_media.AngelChest.utils.ListUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public final class CommandManager {

    private static final String pathToCommandDescriptions = "assets/lang/commands/";

    public static void registerCommand(String permission, String... aliases) {
        Main main = Main.getInstance();
        main.debug("Registering command "+aliases[0]);
        for(String alias : aliases) {
            if(alias.equals(aliases[0])) continue;
            main.debug("  Alias: "+alias);
        }
        PluginCommand command = getCommand(aliases[0], main);
        command.setAliases(Arrays.asList(aliases));
        try {
            List<String> usage = FileUtils.readFileFromResources(pathToCommandDescriptions + aliases[0]+".usage");
            List<String> description = FileUtils.readFileFromResources(pathToCommandDescriptions + aliases[0]+".description");
            command.setDescription(ListUtils.getStringFromList(description,"\n"));
            command.setUsage(ListUtils.getStringFromList(usage,System.lineSeparator()));
            command.setPermissionMessage(main.messages.MSG_NO_PERMISSION);
            command.setPermission(permission);
        } catch(Exception e) {
            main.getLogger().warning("Could not add usage/description to command "+aliases[0]);
        }
        getCommandMap().register(main.getDescription().getName(), command);
    }

    private static PluginCommand getCommand(String name, Plugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            command = c.newInstance(name, plugin);
        } catch (SecurityException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return command;
    }

    private static CommandMap getCommandMap() {
        CommandMap commandMap = null;

        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);

                commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
            }
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }

        return commandMap;
    }
}
