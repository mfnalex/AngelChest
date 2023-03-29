package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy_Stepsister;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public final class ProtectionUtils {

    private final YamlConfiguration yamlFile;
    private final Main main = Main.getInstance();

    public ProtectionUtils(final File file) {
        if (!file.exists()) {
            main.saveResource("protected.yml", false);
        }
        yamlFile = YamlConfiguration.loadConfiguration(file);
    }

    public static boolean playerMayBuildHere(final Player p, final Location loc) {
        final Main main = Main.getInstance();
        final BlockPlaceEvent event = new BlockPlaceEvent(loc.getBlock(), loc.getBlock().getState(), loc.getBlock().getRelative(BlockFace.DOWN), new ItemStack(Material.DIRT), p, true, EquipmentSlot.HAND);
        main.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (main.debug)
                main.debug("AngelChest spawn prevented because player " + p.getName() + " is not allowed to place blocks at " + loc);
            return false;
        }
        return true;
    }

    public boolean playerMayOpenThisChest(final Player openingPlayer, final AngelChest angelChest) {
        if (!Daddy_Stepsister.allows(PremiumFeatures.PROTECTION_SETTINGS)) {
            if (!angelChest.isProtected) return true;
            if (angelChest.owner.equals(openingPlayer.getUniqueId())) return true;
            return openingPlayer.hasPermission(Permissions.PROTECT_IGNORE);
        }
        main.debug("Checking whether " + openingPlayer.getName() + " may open this chest.");
        main.debug("  Owner: " + angelChest.owner);
        main.debug("  Killer: " + angelChest.killer);
        if (!angelChest.isProtected) {
            main.debug("Player " + openingPlayer.getName() + " may open the Chest " + angelChest + " because it isnt protected.");
            return true;
        }
        if (openingPlayer.hasPermission(Permissions.PROTECT_IGNORE)) {
            main.debug("Player " + openingPlayer.getName() + " may open the Chest " + angelChest + " because he has the permissiosn angelchest.protect.ignore");
            return true;
        }
        if (openingPlayer.getUniqueId().equals(angelChest.owner) && angelChest.killer == null) {
            if (yamlFile.getBoolean("owner-outside-pvp")) {
                main.debug("Player " + openingPlayer.getName() + " may open the Chest " + angelChest + " bcause he is the owner, didnt die in PvP and owner-outside-pvp: true in protected.yml.");
                return true;
            }
        }
        if (openingPlayer.getUniqueId().equals(angelChest.owner) && angelChest.killer != null) {
            if (yamlFile.getBoolean("owner-in-pvp")) {
                main.debug("Player " + openingPlayer.getName() + " may open the Chest " + angelChest + " because he is the owner, died in PvP and owner-in-pvp: true in protected.yml.");
                return true;
            }
        }
        if (openingPlayer.getUniqueId().equals(angelChest.killer)) {
            if (yamlFile.getBoolean("killer")) {
                main.debug("Player " + openingPlayer.getName() + " may open the Chest " + angelChest + " because he is the killer and killer: true in protected.yml.");
                return true;
            }
        }
        if (!openingPlayer.getUniqueId().equals(angelChest.owner)
                && !openingPlayer.getUniqueId().equals(angelChest.getKiller())) {
            if (yamlFile.getBoolean("others")) {
                main.debug("Player " + openingPlayer.getName() + " may open the Chest " + angelChest + " because he is neuither owner nor killer and others: true in protected.yml.");
                return true;
            }
        }
        final List<String> groups = yamlFile.getStringList("groups");
        if (groups != null) {
            for (final String group : groups) {
                main.debug(" Checking whether " + openingPlayer.getName() + " has permission " + Permissions.PREFIX_GROUP + group);
                if (openingPlayer.hasPermission(Permissions.PREFIX_GROUP + group)) {
                    main.debug("Player " + openingPlayer.getName() + " may open the Chest " + angelChest + " because he has the permission angelchest.group." + group + " and that group is defined in protected.yml");
                    return true;
                } else {
                    main.debug("  He hasnt");
                }
            }
        }
        main.debug("Player " + openingPlayer.getName() + " is not allowed to open the Chest " + angelChest);
        return false;
    }

}
