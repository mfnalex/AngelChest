package de.jeff_media.daddy;

import de.jeff_media.angelchest.enums.PremiumFeatures;

/**
 * Small inline replacement for the former Stepsister dependency.
 *
 * <p>The placeholder is intentionally evaluated in a method body. Spigot
 * replaces premium-resource placeholders in class files, but not in static
 * final fields.</p>
 */
public final class Daddy_Stepsister {

    private static boolean premium;

    private Daddy_Stepsister() {
    }

    public static void init(final Object ignoredPlugin) {
        final String resource = "%%__RESOURCE__%%";
        final String unreplacedPlaceholder = new String(new char[]{'%', '%', '_', '_', 'R', 'E', 'S', 'O', 'U', 'R', 'C', 'E', '_', '_', '%', '%'});
        premium = !resource.equals(unreplacedPlaceholder);
    }

    public static boolean allows(final PremiumFeatures feature) {
        return premium;
    }

    public static boolean allows(final String feature) {
        return premium;
    }
}
