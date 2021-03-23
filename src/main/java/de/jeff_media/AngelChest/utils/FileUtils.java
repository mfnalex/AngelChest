package de.jeff_media.AngelChest.utils;

import java.io.*;

public class FileUtils {

    /**
     * Replaces strings in a file line by line.
     * @param file File
     * @param toSearch String to search for
     * @param replace String for replacement
     * @return true if the file has been changed, otherwise false
     */
    public static boolean replaceStringsInFile(File file, String toSearch, String replace) throws IOException {

        boolean changed = false;
            // input the (modified) file content to the StringBuffer "input"
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            StringBuilder inputBuffer = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if(line.contains(toSearch)) {
                    line = line.replace(toSearch,replace);
                    changed=true;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            bufferedReader.close();

            if(changed) {
                // write the new string with the replaced line OVER the same file
                FileOutputStream fileOut = new FileOutputStream(file);
                fileOut.write(inputBuffer.toString().getBytes());
                fileOut.close();
            }


            return changed;
    }
}
