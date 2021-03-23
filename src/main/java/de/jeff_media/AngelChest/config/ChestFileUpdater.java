package de.jeff_media.AngelChest.config;

import de.jeff_media.AngelChest.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class ChestFileUpdater {

    public static void updateChestFilesToNewDeathCause() {
        Main main = Main.getInstance();

        if(!main.getDataFolder().exists()) return;
        if(!new File(main.getDataFolder(),"angelchests").exists()) return;
        for(File file2 : new File(main.getDataFolder(),"angelchests").listFiles()) {
            boolean changed = false;
            try {
                // input the (modified) file content to the StringBuffer "input"
                BufferedReader file = new BufferedReader(new FileReader(file2));
                StringBuffer inputBuffer = new StringBuffer();
                String line;

                while ((line = file.readLine()) != null) {
                    if(line.contains("de.jeff_media.AngelChestPlus.data.DeathCause")) {
                        line = line.replaceAll("de.jeff_media.AngelChestPlus.data.DeathCause","de.jeff_media.AngelChest.data.DeathCause");
                        changed=true;
                    }
                    inputBuffer.append(line);
                    inputBuffer.append('\n');
                }
                file.close();

                if(changed) {
                    // write the new string with the replaced line OVER the same file
                    FileOutputStream fileOut = new FileOutputStream(file2);
                    fileOut.write(inputBuffer.toString().getBytes());
                    fileOut.close();
                    main.getLogger().info("Updated old AngelChest file "+ file2.getName());
                }

            } catch (Exception e) {
                main.getLogger().severe("Problem updating AngelChest file "+file2.getName());
            }
        }
    }

}
