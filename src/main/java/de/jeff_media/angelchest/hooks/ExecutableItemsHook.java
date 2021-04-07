package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ExecutableItemsHook {

    @SuppressWarnings("rawtypes") private static Class executableItemsAPIClass = null;
    private static boolean isExecutableItemsInstalled = false;
    private static Method getExecutableItemConfigMethod = null;
    private static Method isExecutableItemMethod = null;
    private static Method isKeepItemOnDeathMethod = null;

    /**
     * Yeah, I know. I don't care. Just don't look at it, alright?
     * This is the only way without forcing devs to download the ExecutableItems .jar manually.
     */
    public static void init() {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("ExecutableItems");
        if (plugin != null && Main.getInstance().getConfig().getBoolean(Config.USE_EXECUTABLEITEMS)) {
            try {
                executableItemsAPIClass = Class.forName("com.ssomar.executableitems.api.ExecutableItemsAPI");
                //noinspection unchecked
                isExecutableItemMethod = executableItemsAPIClass.getDeclaredMethod("isExecutableItem", ItemStack.class);
                @SuppressWarnings("rawtypes") final Class itemClass = Class.forName("com.ssomar.executableitems.items.Item");
                //noinspection unchecked
                getExecutableItemConfigMethod = executableItemsAPIClass.getDeclaredMethod("getExecutableItemConfig", ItemStack.class);
                //noinspection unchecked
                isKeepItemOnDeathMethod = itemClass.getDeclaredMethod("isKeepItemOnDeath", null);
                isExecutableItemsInstalled = true;
            } catch (final ClassNotFoundException | NoSuchMethodException e) {
                Main.getInstance().getLogger().warning("Warning: Could not hook into ExecutableItems although it's installed.");
                e.printStackTrace();
            }
        }
    }

    public static boolean isKeptOnDeath(final ItemStack item) {
        if (!isExecutableItemsInstalled) {
            return false;
        }

        try {
            if (!((boolean) isExecutableItemMethod.invoke(executableItemsAPIClass, item))) {
                return false;
            }

            final Object config = getExecutableItemConfigMethod.invoke(executableItemsAPIClass, item);
            final boolean isKeptOnDeath = (boolean) isKeepItemOnDeathMethod.invoke(config, null);

            if (isKeptOnDeath) {
                Main.getInstance().debug("Found \"Keep on Death\" Item by ExecutableItems: " + item.toString());
                return true;
            }
            return false;

        } catch (final InvocationTargetException | IllegalAccessException e) {
            Main.getInstance().getLogger().warning("Warning: Could not access ExecutableItems's API although it's installed.");
            e.printStackTrace();
            isExecutableItemsInstalled = false;
            return false;
        }
    }

}
