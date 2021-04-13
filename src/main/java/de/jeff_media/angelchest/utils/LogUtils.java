package de.jeff_media.angelchest.utils;

import jdk.nashorn.internal.runtime.logging.DebugLogger;

import de.jeff_media.angelchest.Main;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class LogUtils {

    public static void debugBanner(String[] lines) {
        Main main = Main.getInstance();
        StringBuilder sb  = new StringBuilder();
        int longestLine = 0;
        for (String line : lines) {
            longestLine = Math.max(line.length(), longestLine);
        }
        longestLine += 4;
        StringBuilder dash = new StringBuilder(longestLine);
        Stream.generate(()->"*").limit(longestLine).forEach(dash::append);

        main.debug(dash.toString());
        //sb.append(dash);
        for (String line : lines) {
            main.debug("* " + line);
        }
        main.debug(dash.toString());
    }

}
