package de.jeff_media.AngelChestPlus;


import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("JavaReflectionMemberAccess")
public class WorldGuardLegacyHandler {

    final WorldGuardPlugin wgLegacy;
    final WorldGuardHandler handler;
    RegionContainer container;
    Method toVectorMethod;

    /**
     * This is so dirty that I love it. Has to be done because the Compiler doesn't know we are actually talking with the API version 6
     * @param handler
     */
    WorldGuardLegacyHandler(WorldGuardHandler handler) {
        this.handler=handler;
        wgLegacy = WGBukkit.getPlugin();
        if(wgLegacy==null) {
            handler.main.getLogger().warning("Failed to hook into WorldGuard because it returned null.");
            handler.disabled=true;
            return;
        }
        try {
            Method method = wgLegacy.getClass().getMethod("getRegionContainer");
            container = (RegionContainer) method.invoke(wgLegacy);

            toVectorMethod = BukkitUtil.class.getMethod("toVector", Location.class);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            handler.main.getLogger().warning("Failed to hook into WorldGuard although it seems to be installed.");
            if(handler.main.debug) {
                e.printStackTrace();
            }
            handler.disabled=true;
        }
        handler.main.getLogger().info("Successfully hooked into WorldGuard legacy");
    }
    boolean isBlacklisted(Block block) {
        handler.main.debug("Checking with WorldGuard 6 API if player died in blacklisted region");
        if(wgLegacy==null) return false;
        if(handler.main.disabledRegions==null || handler.main.disabledRegions.size()==0) return false;
        RegionManager regions = container.get(block.getWorld());
        if(regions==null) return false;
        ApplicableRegionSet regionSet;
        try {
            //noinspection rawtypes
            Class vectorClazz = Class.forName("com.sk89q.worldedit.Vector");

            Method getRegionsMethod = regions.getClass().getMethod("getApplicableRegions", vectorClazz);
            regionSet = (ApplicableRegionSet) getRegionsMethod.invoke(regions, toVectorMethod.invoke(BukkitUtil.class,block.getLocation()));

        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            handler.main.getLogger().warning("Error while trying to check regions in WorldGuard 6 API. Enable debug mode for more information");
            if(handler.main.debug) {
                e.printStackTrace();
            }
            return false;
        }

        handler.main.debug("Checking Regions in legacy WorldGuard");

        for(ProtectedRegion region : regionSet.getRegions()) {
                handler.main.debug("Player died in region " +region.getId());
                if(handler.main.disabledRegions.contains(region.getId())) {
                    handler.main.debug("Preventing AngelChest from spawning in disabled worldguard region");
                    return true;
                }

        }
        handler.main.debug("Player died outside of any blacklisted regions");
        return false;
    }

}
