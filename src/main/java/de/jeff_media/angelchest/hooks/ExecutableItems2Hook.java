package de.jeff_media.angelchest.hooks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Optional;

// This is what you get if you don't upload your API to a maven repository.
public class ExecutableItems2Hook extends IExecutableItemsHook {

    private static final String EXECUTABLE_ITEMS_API_CLASS =
            "com.ssomar.score.api.executableitems.ExecutableItemsAPI";
    private static final String EXECUTABLE_ITEMS_MANAGER_CLASS =
            "com.ssomar.score.api.executableitems.config.ExecutableItemsManagerInterface";
    private static final String EXECUTABLE_ITEM_CLASS =
            "com.ssomar.score.api.executableitems.config.ExecutableItemInterface";

    private final Method getExecutableItemsManagerMethod;
    private final Method getExecutableItemMethod;
    private final Method hasKeepItemOnDeathMethod;

    public ExecutableItems2Hook() throws ReflectiveOperationException {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("ExecutableItems");
        if (plugin == null) {
            throw new ClassNotFoundException("ExecutableItems is not installed");
        }

        final ClassLoader classLoader = plugin.getClass().getClassLoader();
        final Class<?> executableItemsApiClass = Class.forName(EXECUTABLE_ITEMS_API_CLASS, true, classLoader);
        final Class<?> executableItemsManagerClass = Class.forName(EXECUTABLE_ITEMS_MANAGER_CLASS, true, classLoader);
        final Class<?> executableItemClass = Class.forName(EXECUTABLE_ITEM_CLASS, true, classLoader);

        getExecutableItemsManagerMethod = executableItemsApiClass.getMethod("getExecutableItemsManager");
        getExecutableItemMethod = executableItemsManagerClass.getMethod("getExecutableItem", ItemStack.class);
        hasKeepItemOnDeathMethod = executableItemClass.getMethod("hasKeepItemOnDeath");
    }

    @Override
    public boolean isKeptOnDeath(ItemStack item) {
        try {
            final Object manager = getExecutableItemsManagerMethod.invoke(null);
            final Optional<?> optional = (Optional<?>) getExecutableItemMethod.invoke(manager, item);
            if (optional.isEmpty()) {
                return false;
            }

            return (boolean) hasKeepItemOnDeathMethod.invoke(optional.get());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
