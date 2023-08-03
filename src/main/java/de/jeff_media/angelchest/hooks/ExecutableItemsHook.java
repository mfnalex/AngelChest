package de.jeff_media.angelchest.hooks;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ExecutableItemsHook extends IExecutableItemsHook {

    @SuppressWarnings("rawtypes")
    private static Class executableItemsAPIClass = null;
    private static Method getExecutableItemConfigMethod = null;
    private static Method isExecutableItemMethod = null;
    private static boolean isExecutableItemsInstalled = false;
    private static Method isKeepItemOnDeathMethod = null;

    /**
     * Yeah, I know. I don't care. Just don't look at it, alright?
     * Here is the reason why I have to use reflection at the current point:
     * https://www.spigotmc.org/threads/accessing-other-plugins-api-without-having-them-as-maven-dependency.499653/page-2#post-4134458
     * <p>
     * If you think that this has performance problems, get a new computer because it doesn't slow ANYTHING down.
     * This is the only way without forcing devs to download the ExecutableItems .jar manually.
     * If you think I should just ask all volunteers to download the .jar, I would get no pull requests at all.
     * If you think I should use jitpack.io: the API is not in the public ExecutableItems repo, at least I didnt find it.
     */
    public ExecutableItemsHook() throws NoSuchMethodException, ClassNotFoundException {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("ExecutableItems");
        if (plugin != null && AngelChestMain.getInstance().getConfig().getBoolean(Config.USE_EXECUTABLEITEMS)) {
                executableItemsAPIClass = Class.forName("com.ssomar.executableitems.api.ExecutableItemsAPI");
                //noinspection unchecked
                isExecutableItemMethod = executableItemsAPIClass.getDeclaredMethod("isExecutableItem", ItemStack.class);
                @SuppressWarnings("rawtypes") final Class itemClass = Class.forName("com.ssomar.executableitems.items.Item");
                //noinspection unchecked
                getExecutableItemConfigMethod = executableItemsAPIClass.getDeclaredMethod("getExecutableItemConfig", ItemStack.class);
                //noinspection unchecked
                isKeepItemOnDeathMethod = itemClass.getDeclaredMethod("isKeepItemOnDeath", null);
                isExecutableItemsInstalled = true;
        }
    }

    public boolean isKeptOnDeath(final ItemStack item) {
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
                AngelChestMain.getInstance().debug("Found \"Keep on Death\" Item by ExecutableItems: " + item.toString());
                return true;
            }
            return false;

        } catch (final InvocationTargetException | IllegalAccessException e) {
            AngelChestMain.getInstance().getLogger().warning("Warning: Could not access ExecutableItems's API although it's installed.");
            e.printStackTrace();
            isExecutableItemsInstalled = false;
            return false;
        }
    }

}
