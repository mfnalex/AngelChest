package de.jeff_media.angelchest.hooks;

import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ExecutableItems2Hook extends IExecutableItemsHook{
    @Override
    public boolean isKeptOnDeath(ItemStack item) {
        Optional<ExecutableItemInterface> optional = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(item);
        if(!optional.isPresent()) return false;
        ExecutableItemInterface execItem = optional.get();
        return execItem.hasKeepItemOnDeath();
    }
}
