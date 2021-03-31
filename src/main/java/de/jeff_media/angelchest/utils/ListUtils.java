package de.jeff_media.angelchest.utils;

import java.util.List;

public class ListUtils {

    public static String getStringFromList(List<String> list, String separator) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for(String line : list) {
            builder.append(line);
            if(index < list.size()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

}
