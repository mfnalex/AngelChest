package de.jeff_media.angelchest;

import java.util.concurrent.ThreadLocalRandom;

public class StringEncrypter {

    private static int getRandomOffset() {
        return ThreadLocalRandom.current().nextInt(100,1000);
    }

    public static String encryptString(String string) {
        int offset = getRandomOffset();
        char[] chars = string.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] + i*offset);
        }
        return offset + new String(chars);
    }

    public static String decryptString(String string) {
        int offset = Integer.parseInt(string.substring(0,3));
        string = string.substring(3);
        char[] chars = string.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] - i*offset);
        }
        return new String(chars);
    }


    public static String note() {
        return "This class is not used in the free version of AngelChest :-)";
    }

}
