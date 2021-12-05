package de.jeff_media.angelchest.hooks;

public class MMOItemsHook {

    /**
     * MMOItems thinks it's a good idea to call a PlayerDeathEvent right after the original PlayerDeathEvent was called.
     * This is a very bad idea and there's no way to detect whether a listener gets passed a REAL PlayerDeathEvent,
     * or MMOItems' cursed fake PlayerDeathEvent, except by going through the current Thread's Stacktrace... smh
     * @param stackTrace StackTrace to check
     * @return true when the StackTrace contains MMOItems' classes, otherwise false
     */
    public static boolean isFakeDeathEvent(StackTraceElement[] stackTrace) {
        for(StackTraceElement element : stackTrace) {
            if(element.getClassName().contains("io.lumine.mythic")
                    || element.getClassName().contains("net.Indyuce.mmoitems")) {
                return true;
            }
        }
        return false;
    }
}
