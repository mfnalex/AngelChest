package de.jeff_media.AngelChestPlus;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import de.jeff_media.AngelChestPlus.utils.AngelChestCommandUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Hologram {

    final ArrayList<UUID> armorStandUUIDs;
    final String text;
    final double lineOffset;
    private final Main main;
    //double offset = 0.0D;
    boolean usePapi = false;

    /*public Hologram(Block block, String text, Main main, AngelChest chest) {
        this(block.getLocation().add(new Vector(0.5, -0.5 + main.getConfig().getDouble(Config.HOLOGRAM_OFFSET), 0.5)), text, main, chest);
    }*/

    public void update(AngelChest chest) {

        Scanner scanner = new Scanner(text);
        int lineNumber = 0;
        while (scanner.hasNextLine()) {

            ArmorStand armorStand = getArmorStandByLineNumber(lineNumber);
			String line = scanner.nextLine();
            if (armorStand != null) {

                line = line.replaceAll("\\{time}", AngelChestCommandUtils.getTimeLeft(chest));
                if (line.equals("")) {
                    armorStand.setCustomName(" ");
                    armorStand.setCustomNameVisible(false);
                } else {
                    armorStand.setCustomNameVisible(true);
                }

                if (usePapi) {
                    line = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(chest.owner), line);
                }

                armorStand.setCustomName(line);
                //System.out.println("updated hologram "+armorStands.get(lineNumber).getUniqueId()+" "+ line);
            }

            lineNumber++;
        }
    }

    //public Hologram(Location location , String text, Main main, AngelChest chest) {
    public Hologram(Block block, String text, Main main, AngelChest chest) {
        this.main=main;
        int totalLineNumbers = text.split("\n").length;
        lineOffset = main.getConfig().getDouble(Config.HOLOGRAM_OFFSET_PER_LINE);
        Location location = block.getLocation()
                .add(new Vector(0.5, -1.3 + main.getConfig().getDouble(Config.HOLOGRAM_OFFSET), 0.5))
                .add(new Vector(0,lineOffset * totalLineNumbers,0));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            usePapi = true;
        }

        main.debug("Creating hologram with text " + text + " at " + location.toString());
        this.text = text;

        armorStandUUIDs = new ArrayList<>();
        int lineNumber = 0;

        Scanner scanner = new Scanner(text);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = line.replaceAll("\\{time}", AngelChestCommandUtils.getTimeLeft(chest));
            boolean customNameVisible = true;
            if (line.equals("")) {
                line = " ";
                customNameVisible = false;
            }

            //plugin.hookUtils.hologramToBeSpawned=true;

            ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(new Vector(0, -lineOffset*lineNumber, 0)), EntityType.ARMOR_STAND); // Spawn the ArmorStand
            armorStandUUIDs.add(as.getUniqueId());

            as.setGravity(false);
            as.setCanPickupItems(false);
            as.setCustomName(line);
            as.setCustomNameVisible(customNameVisible);
            as.setVisible(false);

            //currentOffset += lineOffset;

            //plugin.hookUtils.hologramToBeSpawned=false;
            lineNumber++;
        }
        scanner.close();
        main.watchdog.save();
    }

    public void destroy() {
        for (ArmorStand armorStand : getArmorStands()) {
            //System.out.println("DESTROYING ARMOR STAND @ " + armorStand.getLocation().toString());
            if (armorStand != null) armorStand.remove();

            armorStandUUIDs.remove(armorStand.getUniqueId());
        }
        main.watchdog.save();
    }

    public @Nullable ArmorStand getArmorStandByLineNumber(int line) {
        if (armorStandUUIDs.size() <= line) return null;
        return (ArmorStand) Bukkit.getEntity(armorStandUUIDs.get(line));
    }

    public @NotNull List<ArmorStand> getArmorStands() {
        ArrayList<ArmorStand> armorStands = new ArrayList<>();
        for (UUID uuid : armorStandUUIDs) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof ArmorStand) {
                armorStands.add((ArmorStand) entity);
            }
        }
        return armorStands;
    }

}