package de.jeff_media.angelchest.hooks;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import de.jeff_media.angelchest.data.WorldBoundingBox;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class WorldEditHandler {

    @Nullable
    public static WorldBoundingBox getSelection(org.bukkit.entity.Player player) {
        try {
            Player actor = BukkitAdapter.adapt(player);
            SessionManager manager = WorldEdit.getInstance().getSessionManager();
            LocalSession session = manager.get(actor);
            World selectionWorld = session.getSelectionWorld();
            Region region = session.getSelection(selectionWorld);
            region.getMaximumPoint();
            Block max = BukkitAdapter.adapt(player.getWorld(),region.getMaximumPoint()).getBlock();
            Block min = BukkitAdapter.adapt(player.getWorld(),region.getMinimumPoint()).getBlock();
            return new WorldBoundingBox(player.getWorld(), BoundingBox.of(min, max));
        } catch (Throwable throwable) {
            return null;
        }
    }

}
