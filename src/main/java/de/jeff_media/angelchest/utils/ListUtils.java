package de.jeff_media.angelchest.utils;

import java.util.List;

public class ListUtils {

    public static String getStringFromList(final List<String> list, final String separator) {
        final StringBuilder builder = new StringBuilder();
        final int index = 1;
        for (final String line : list) {
            builder.append(line);
            if (index < list.size()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

}
