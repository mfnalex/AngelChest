package de.jeff_media.angelchest.hooks;

import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AdvancedEnchantmentsHook {

    private static Method hasHolyWhiteScrollMethod = null;

    static {
        try {
            hasHolyWhiteScrollMethod = Class.forName("net.advancedplugins.ae.api.AEAPI").getMethod("hasHolyWhiteScroll", ItemStack.class);
        } catch (Exception ignored) {

        }
    }

    public static boolean hasWhiteScroll(ItemStack item) {
        if(hasHolyWhiteScrollMethod != null) {
            try {
                return (boolean) hasHolyWhiteScrollMethod.invoke(null, item);
            } catch (IllegalAccessException | InvocationTargetException ignored) {

            }
        }
        return AEAPI.hasWhitescroll(item);
    }

}
