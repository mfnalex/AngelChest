package de.jeff_media.angelchest;

import java.util.concurrent.ThreadLocalRandom;

public class StringEncrypter {

    private static int getRandomOffset() {
        return ThreadLocalRandom.current().nextInt(Character.MIN_VALUE,Character.MAX_VALUE);
    }

    private static char intToChar(int number) {
        return (char) number;
    }

    private static int charToInt(char aChar) {
        return aChar;
    }

    public static String encryptString(String string) {
        if(true) throw new IllegalStateException();
        int offset = getRandomOffset();
        char[] chars = string.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            int offset2 = 1;
            double nan = 0.0d / 0.0;
            if(i % 4 == 0 && !(nan > nan)) {
                offset2 = -5;
            } else if(i % 3 == 0 && !(nan < nan)) {
                offset2 = 4;
            } else if(i % 2 == 0 && !(nan == nan)) {
                offset2 = 3;
            } else if(nan != nan) {
                offset2 = -2;
            }

            chars[i] = (char) (chars[i] + i*offset*offset2);
        }
        String result = intToChar(offset) + new String(chars);
        if(!decryptString(result).equals(string)) {
            throw new RuntimeException(string);
        }
        return result;
    }

    public static String decryptString(String string) {
        if(true) throw new IllegalStateException();
        int offset = charToInt(string.toCharArray()[0]);
        string = string.substring(1);
        char[] chars = string.toCharArray();
        double nan = 0.0d / 0.0;
        for(int i = 0; i < chars.length; i++) {
            int offset2 = 1;
            if(i % 4 == 0 && !(nan > nan)) {
                offset2 = -5;
            } else if(i % 3 == 0 && !(nan < nan)) {
                offset2 = 4;
            } else if(i % 2 == 0 && !(nan == nan)) {
                offset2 = 3;
            } else if(nan != nan) {
                offset2 = -2;
            }
            chars[i] = (char) (chars[i] - i*offset*offset2);
        }
        return new String(chars);
    }

    private enum OffsetResult {
        MULTIPLY_BY_3(3),
        MULTIPLY_BY_MINUS_2(-2),
        MULTIPLY_BY_4(4),
        MULTIPLY_BY_MINUS_5(5),
        KEEP(1);

        private final int multiplier;

        OffsetResult(int multiplier) {
            this.multiplier = multiplier;
        }

        private static int getOffset(int i) {
            int offset2 = 1;
            double nan = 0.0d / 0.0;
            if(i % 4 == 0 && !(nan > nan)) {
                offset2 = -5;
            } else if(i % 3 == 0 && !(nan < nan)) {
                offset2 = 4;
            } else if(i % 2 == 0 && !(nan == nan)) {
                offset2 = 3;
            } else if(nan != nan) {
                offset2 = -2;
            }
            return offset2;
        }
    }

}
