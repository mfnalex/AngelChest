package de.jeff_media.angelchest.hooks;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// This is what you get if you don't upload your API to a maven repository.
public class AdvancedEnchantmentsHook {

    private static final Method hasHolyWhiteScrollMethod = findMethod("hasHolyWhiteScroll");
    private static final Method hasWhiteScrollMethod = findMethod("hasWhitescroll");

    private static Method findMethod(final String methodName) {
        try {
            return Class.forName("net.advancedplugins.ae.api.AEAPI").getMethod(methodName, ItemStack.class);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | LinkageError ignored) {
            return null;
        }
    }

    public static boolean hasWhiteScroll(ItemStack item) {
        final Boolean hasHolyWhiteScroll = invoke(hasHolyWhiteScrollMethod, item);
        if (hasHolyWhiteScroll != null) {
            return hasHolyWhiteScroll;
        }
        final Boolean hasWhiteScroll = invoke(hasWhiteScrollMethod, item);
        return hasWhiteScroll != null && hasWhiteScroll;
    }

    private static Boolean invoke(final Method method, final ItemStack item) {
        if (method == null) {
            return null;
        }
        try {
            return (Boolean) method.invoke(null, item);
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException | IllegalArgumentException ignored) {
            return null;
        }
    }

}
