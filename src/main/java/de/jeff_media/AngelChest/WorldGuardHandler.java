package de.jeff_media.AngelChest;

import com.sk89q.worldedit.bukkit.BukkitAdapter;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Objects;

public class WorldGuardHandler {


    final Main main;
    WorldGuardPlugin wg;
    RegionContainer container;
    boolean disabled = false;

    // This is for WorldGuard 7+ only.
    // If an older version is installed, this class will redirect the check to the legacy handler
    WorldGuardLegacyHandler legacyHandler = null;

    WorldGuardHandler(Main main) {
        this.main=main;

        if(main.getConfig().getBoolean("disable-worldguard-integration")) {
            disabled = true;
            main.getLogger().info("WorldGuard integration has been disabled in the config.yml.");
            return;
        }

        if(main.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            main.debug("WorldGuard is not installed at all.");
            disabled = true;
            return;
        }

        try {
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin").getMethod("inst");
            wg = WorldGuardPlugin.inst();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            //System.out.println("WorldGuard not found");
            disabled = true;
            return;
        }

        // Getting here means WorldGuard is installed

        if(wg != null) {
            try {
                // This only works on WorldGuard 7+
                container = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(WorldGuard.getInstance(),"WorldGuard#getInstance is null")
                        .getPlatform(),"WorldGuard#getInstance#getPlatform is null").getRegionContainer(),"WorldGuard#getInstance#getRegionContainer is null");
                main.getLogger().info("Successfully hooked into WorldGuard 7+");
            } catch(NoClassDefFoundError e) {
                // Ok, try again with version 6
                legacyHandler = new WorldGuardLegacyHandler(this);
            } catch(NullPointerException e) {
                disabled = true;
                main.getLogger().info("You are using a version of WorldGuard that does not fully support your Minecraft version. WorldGuard integration is disabled.");
            }
        }


    }

    boolean isBlacklisted(Block block) {
        if(disabled) return false;
        if(legacyHandler!=null) return legacyHandler.isBlacklisted(block);
        if(wg==null) return false;
        if(main.disabledRegions==null || main.disabledRegions.size()==0) return false;

        RegionManager regions = container.get(BukkitAdapter.adapt(block.getWorld()));
        List<String> regionList = regions.getApplicableRegionsIDs(getBlockVector3(block));

        main.debug("Checking Regions in WG7+");

        for(String r : regionList) {
            main.debug("Player died in region " +r);
            if(main.disabledRegions.contains(r)) {
                main.debug("Preventing AngelChest from spawning in disabled worldguard region");
                return true;
            }
        }
        return false;
    }

    BlockVector3 getBlockVector3(Block block) {
        return BlockVector3.at(block.getX(),block.getY(),block.getZ());
    }
}
