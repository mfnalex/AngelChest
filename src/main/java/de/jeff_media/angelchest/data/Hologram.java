package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.nbt.NBTValues;
import de.jeff_media.angelchest.utils.CommandUtils;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.NBTAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Represents a complete AngelChest hologram
 */
public final class Hologram {

    public final ArrayList<UUID> armorStandUUIDs;
    public final String text;
    final double lineOffset;
    private final Main main;
    boolean usePapi = false;

    /**
     * Creates a hologram with one or more lines
     *
     * @param block Block where the hologram should be spawned
     * @param text  The hologram text, lines separated by "\n"
     * @param chest AngelChest this hologram belongs to
     */
    public Hologram(final Block block, final String text, final AngelChest chest) {
        this.main = Main.getInstance();
        final int totalLineNumbers = text.split("\n").length;
        lineOffset = main.getConfig().getDouble(Config.HOLOGRAM_OFFSET_PER_LINE);
        final Location location = block.getLocation().add(new Vector(0.5, -1.3 + main.getConfig().getDouble(Config.HOLOGRAM_OFFSET), 0.5)).add(new Vector(0, lineOffset * totalLineNumbers, 0));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            usePapi = true;
        }

        if (main.debug) main.debug("Creating hologram with text\n" + text + "\n§rat " + location);
        this.text = text;

        armorStandUUIDs = new ArrayList<>();
        int lineNumber = 0;

        final Scanner scanner = new Scanner(text);
        while (scanner.hasNextLine()) {
            // TODO: Replace duplicates with calling update() method
            String line = scanner.nextLine();
            line = line.replace("{time}", CommandUtils.getTimeLeft(chest));
            if (Daddy.allows(PremiumFeatures.HOLOGRAM_SHOWS_PROTECTION_STATUS)) {
                line = line.replace("{protected}", getProtectedText(chest));
            }
            line = line.replace("{items}", Integer.toString(chest.getNumberOfItems()));
            line = line.replace("{xp}", Integer.toString(chest.getExperience()));
            boolean customNameVisible = true;
            if (line.equals("")) {
                line = " ";
                customNameVisible = false;
            }

            final ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(new Vector(0, -lineOffset * lineNumber, 0)), EntityType.ARMOR_STAND); // Spawn the ArmorStand
            armorStandUUIDs.add(as.getUniqueId());

            as.setGravity(false);
            as.setCanPickupItems(false);
            as.setCustomName(line);
            as.setCustomNameVisible(customNameVisible);
            as.setVisible(false);

            NBTAPI.addNBT(as, NBTTags.IS_HOLOGRAM, NBTValues.TRUE);

            lineNumber++;
        }
        scanner.close();
        main.watchdog.save();
    }

    /**
     * Destroys all armor stands belonging to this hologram
     */
    public void destroy() {
        for (final ArmorStand armorStand : getArmorStands()) {
            if (armorStand != null) armorStand.remove();

            armorStandUUIDs.remove(armorStand.getUniqueId());
        }
        main.watchdog.save();
    }

    /**
     * Returns the armor stand that belongs to a specific line number
     *
     * @param line line number, starting with 0
     * @return armor stand belonging to line number, null if it doesnt exist
     */
    public @Nullable ArmorStand getArmorStandByLineNumber(final int line) {
        if (armorStandUUIDs.size() <= line) return null;
        return (ArmorStand) Bukkit.getEntity(armorStandUUIDs.get(line));
    }

    /**
     * Returns a list of all ArmorStands
     *
     * @return list of all ArmorStands
     */
    public @NotNull List<ArmorStand> getArmorStands() {
        final ArrayList<ArmorStand> armorStands = new ArrayList<>();
        for (final UUID uuid : armorStandUUIDs) {
            final Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof ArmorStand) {
                armorStands.add((ArmorStand) entity);
            }
        }
        return armorStands;
    }

    private String getProtectedText(final AngelChest chest) {
        if (!chest.isProtected) {
            return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.HOLOGRAM_UNPROTECTED_TEXT));
        }
        if (chest.unlockIn != -1) {
            return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.HOLOGRAM_PROTECTED_COUNTDOWN_TEXT).replace("{time}", CommandUtils.getUnlockTimeLeft(chest)));
        }
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(Config.HOLOGRAM_PROTECTED_TEXT));
    }

    /**
     * Updates the hologram. Called once per second
     *
     * @param chest AngelChest this hologram belongs to
     */
    public void update(final AngelChest chest) {

        final Scanner scanner = new Scanner(text);
        int lineNumber = 0;
        while (scanner.hasNextLine()) {

            final ArmorStand armorStand = getArmorStandByLineNumber(lineNumber);
            String line = scanner.nextLine();
            if (armorStand != null) {

                line = line.replace("{time}", CommandUtils.getTimeLeft(chest));
                if (Daddy.allows(PremiumFeatures.GENERIC)) { // Don't add Feature here, this method gets called every second
                    line = line.replace("{protected}", getProtectedText(chest));
                }
                line = line.replace("{items}", Integer.toString(chest.getNumberOfItems()));
                line = line.replace("{xp}", Integer.toString(chest.getExperience()));
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

}