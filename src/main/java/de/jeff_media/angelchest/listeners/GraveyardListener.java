package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.angelchest.events.PlayerEnterLeaveGraveyardEvent;
import de.jeff_media.angelchest.handlers.GraveyardManager;
import de.jeff_media.angelchest.nms.NMSHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class GraveyardListener implements Listener {

    private static final int CHECK_RADIUS = 3;
    private static final Main main = Main.getInstance();
    private static final HashMap<Player, Graveyard> playerGraveyardMap = new HashMap<>();

    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    Graveyard currentYard = GraveyardManager.fromLocation(player.getLocation());
                    if(currentYard == playerGraveyardMap.get(player)) continue;
                    if(currentYard == null) {
                        Bukkit.getPluginManager().callEvent(new PlayerEnterLeaveGraveyardEvent(player, playerGraveyardMap.get(player), PlayerEnterLeaveGraveyardEvent.Action.LEAVE));
                    } else {
                        Graveyard oldYard = playerGraveyardMap.get(player);
                        if(oldYard != null) {
                            Bukkit.getPluginManager().callEvent(new PlayerEnterLeaveGraveyardEvent(player, oldYard, PlayerEnterLeaveGraveyardEvent.Action.LEAVE));
                        }
                        Bukkit.getPluginManager().callEvent(new PlayerEnterLeaveGraveyardEvent(player, currentYard, PlayerEnterLeaveGraveyardEvent.Action.ENTER));
                    }
                }
            }
        }.runTaskTimer(main, 0, 1);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        callGraveyardEnterEvent(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        callGraveyardLeaveEvent(event.getPlayer());
    }

    public static void callGraveyardLeaveEvent(Player player) {
        Graveyard yard = playerGraveyardMap.get(player);
        if(yard == null) return;
        Bukkit.getPluginManager().callEvent(new PlayerEnterLeaveGraveyardEvent(player, yard, PlayerEnterLeaveGraveyardEvent.Action.LEAVE));
    }

    public static void callGraveyardEnterEvent(Player player) {
        Graveyard yard = playerGraveyardMap.get(player);
        if(yard != GraveyardManager.fromLocation(player.getLocation())) return;
        Bukkit.getPluginManager().callEvent(new PlayerEnterLeaveGraveyardEvent(player, GraveyardManager.fromLocation(player.getLocation()), PlayerEnterLeaveGraveyardEvent.Action.ENTER));
    }

    @EventHandler
    public void onEnterLeaveYard(PlayerEnterLeaveGraveyardEvent event) {
        PlayerEnterLeaveGraveyardEvent.Action action = event.getAction();
        if(action == PlayerEnterLeaveGraveyardEvent.Action.ENTER) {
            event.getGraveyard().applyPotionEffects(event.getPlayer());
        } else {
            event.getGraveyard().removePotionEffects(event.getPlayer());
        }
    }

    public static void update(Block block) {
        if(!GraveyardManager.hasGraveyard(block.getWorld())) return;
        for(Graveyard graveyard : GraveyardManager.getGraveyards(block.getWorld())) {
            if(main.debug) {
                main.debug("Block changed within Graveyard " + graveyard.getName() + ", updating cached grave locations...");
            }
            if(graveyard.getWorldBoundingBox().contains(block)) {
                for(int x = -CHECK_RADIUS; x <= CHECK_RADIUS; x++) {
                    for(int y = -CHECK_RADIUS; y <= CHECK_RADIUS; y++) {
                        for(int z = -CHECK_RADIUS; z <= CHECK_RADIUS; z++) {
                            Block candidate = block.getRelative(x,y,z);
                            if(!graveyard.getWorldBoundingBox().contains(block)) continue;
                            if(graveyard.isValidSpawnOn(candidate)) {
                                graveyard.getCachedValidGraveLocations().add(candidate);
                            } else {
                                graveyard.getCachedValidGraveLocations().remove(candidate);
                            }
                        }
                    }
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        update(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        update(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        System.out.println(1);
        Player player = event.getPlayer();
        Graveyard graveyard = GraveyardManager.getLastGraveyard(player);
        if(graveyard == null) return;
        System.out.println(2);
        Location respawnLocation = graveyard.getSpawn();
        if(respawnLocation == null) return;
        System.out.println(3);
        event.setRespawnLocation(respawnLocation);
        GraveyardManager.setLastGraveyard(player, null);

        if(graveyard.hasCustomTotemAnimation()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> NMSHandler.playTotemAnimation(player, graveyard.getCustomTotemModelData()), 1L);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDie(PlayerDeathEvent event) {
        System.out.println("[GRAVEYARDS] PlayerDeathEvent");
        Graveyard graveyard = GraveyardManager.getLastGraveyard(event.getEntity());
        if(graveyard == null) {
            System.out.println("[GRAVEYARDS] No graveyard associated");
            return;
        }
        if(!graveyard.isInstantRespawn()) {
            System.out.println("[GRAVEYARDS] Instant Respawn disabled for this graveyard");
            return;
        }
        System.out.println("[GRAVEYARDS] Respawning");
        Bukkit.getScheduler().runTask(main,() -> event.getEntity().spigot().respawn());
    }

}
