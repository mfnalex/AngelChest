package de.jeff_media.angelchest.hooks;

import com.github.yannicklamprecht.worldborder.api.Position;
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import de.jeff_media.angelchest.AngelChestMain;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class WorldBorderApiHook {

    public static boolean isWithinWorldWorder(Location loc, Player player) {

        if(!AngelChestMain.getInstance().getConfig().getBoolean("use-worldborder-api")) return true;

        RegisteredServiceProvider<WorldBorderApi> worldBorderApiRegisteredServiceProvider = getServer().getServicesManager().getRegistration(WorldBorderApi.class);

        if (worldBorderApiRegisteredServiceProvider == null) {
            return true;
        }

        Position max = worldBorderApiRegisteredServiceProvider.getProvider().getWorldBorder(player).getMax();
        Position min = worldBorderApiRegisteredServiceProvider.getProvider().getWorldBorder(player).getMin();

        if(max == null || min == null) {
            AngelChestMain.getInstance().debug("  Yes, because min or max or null");
            return true;
        }

        double x = loc.getX();
        double z = loc.getZ();

        return !(x < min.x()) && !(x > max.x()) && !(z < min.z()) && !(z > max.z());

    }

}
