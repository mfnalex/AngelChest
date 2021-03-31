package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils {

    /**
     * Replaces strings in a file line by line.
     *
     * @param file     File
     * @param toSearch String to search for
     * @param replace  String for replacement
     * @return true if the file has been changed, otherwise false
     */
    public static boolean replaceStringsInFile(File file, String toSearch, String replace) throws IOException {

        boolean changed = false;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder inputBuffer = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains(toSearch)) {
                line = line.replace(toSearch, replace);
                changed = true;
            }
            inputBuffer.append(line);
            inputBuffer.append('\n');
        }
        bufferedReader.close();

        if (changed) {
            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(file);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();
        }
        return changed;
    }

    public static void appendLines(File file, String line) {
        appendLines(file, new String[]{line});
    }

    public static void appendLines(File file, String[] lines) {
        try {
            Writer output = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);
            for (String line : lines) {
                output.append(line).append(System.lineSeparator());
            }
            output.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static List<String> readFileFromResources(String fileName) {
        InputStream input = Main.getInstance().getResource(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
        List<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return lines;
    }
}
