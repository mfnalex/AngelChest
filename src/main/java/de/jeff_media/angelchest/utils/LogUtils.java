package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.AngelChestMain;

import java.util.stream.Stream;

public final class LogUtils {

    public static void debugBanner(final String[] lines) {
        final AngelChestMain main = AngelChestMain.getInstance();
        final StringBuilder sb = new StringBuilder();
        int longestLine = 0;
        for (final String line : lines) {
            longestLine = Math.max(line.length(), longestLine);
        }
        longestLine += 4;
        final StringBuilder dash = new StringBuilder(longestLine);
        Stream.generate(() -> "*").limit(longestLine).forEach(dash::append);

        if (main.debug) main.debug(dash.toString());
        //sb.append(dash);
        for (final String line : lines) {
            if (main.debug) main.debug("* " + line);
        }
        if (main.debug) main.debug(dash.toString());
    }

}
