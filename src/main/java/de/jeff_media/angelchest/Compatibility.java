package de.jeff_media.angelchest;

import java.io.*;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class Compatibility {

    public static void removeOldDeathCause(File file) {
        try (
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            ListIterator<String> it = lines.listIterator();
            boolean foundDeathCause = false;
            boolean removed = false;
            while (it.hasNext()) {
                String line = it.next();
                if(line.startsWith("deathCause:")) {
                    foundDeathCause = true;
                    continue;
                }
                if(foundDeathCause) {
                    if(line.trim().startsWith("==")) {
                        it.remove();
                        removed = true;
                    }
                    break;
                }
            }
            if(removed) {
                try (FileWriter fileWriter = new FileWriter(file,false);
                     BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)
                ) {
                    for(String line : lines) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    }
                    AngelChestMain.getInstance().getLogger().info("Migrated old AngelChest save file " + file.getAbsolutePath());
                } catch (IOException ignored) {

                }
            }
        } catch (IOException ignored) {

        }
    }
}
