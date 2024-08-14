package de.jeff_media.angelchest.listeners;

import com.jeff_media.jefflib.EntityUtils;
import com.jeff_media.jefflib.TextUtils;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.angelchest.events.PlayerEnterLeaveGraveyardEvent;
import de.jeff_media.angelchest.handlers.GraveyardManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;

public class GraveyardListener implements Listener {

    private static final int CHECK_RADIUS = 3;
    private static final AngelChestMain main = AngelChestMain.getInstance();
    private static final HashMap<Player, Graveyard> playerGraveyardMap = new HashMap<>();
    @Getter private static final HashMap<Player, Collection<PotionEffect>> activePotionEffects = new HashMap<>();

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
                    playerGraveyardMap.put(player, currentYard);
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
        if(yard == null) return;
        if(yard != GraveyardManager.fromLocation(player.getLocation())) return;
        Bukkit.getPluginManager().callEvent(new PlayerEnterLeaveGraveyardEvent(player, GraveyardManager.fromLocation(player.getLocation()), PlayerEnterLeaveGraveyardEvent.Action.ENTER));
    }

    @EventHandler
    public void onEnterLeaveYard(PlayerEnterLeaveGraveyardEvent event) {
        PlayerEnterLeaveGraveyardEvent.Action action = event.getAction();
        Player player = event.getPlayer();
        Graveyard graveyard = event.getGraveyard();
        if(action == PlayerEnterLeaveGraveyardEvent.Action.ENTER) {
            onEnterGraveyard(player, graveyard);
        } else {
            onLeaveGraveyard(player, graveyard);
        }
    }

    private void onLeaveGraveyard(Player player, Graveyard graveyard) {
        //Bukkit.broadcastMessage("§cPlayer " + event.getPlayer().getName() + " left graveyard " + event.getGraveyard().getName());
        graveyard.removePotionEffects(player);
        if(graveyard.hasCustomTime()) {
            player.resetPlayerTime();
        }
        if(graveyard.hasCustomWeather()) {
            player.resetPlayerWeather();
        }
    }

    private void onEnterGraveyard(Player player, Graveyard graveyard) {
        //Bukkit.broadcastMessage("§aPlayer " + event.getPlayer().getName() + " entered graveyard " + event.getGraveyard().getName());
        graveyard.applyPotionEffects(player);
        if(graveyard.hasCustomTime()) {
            player.setPlayerTime(graveyard.getCustomTime(), false);
        }
        if(graveyard.hasCustomWeather()) {
            player.setPlayerWeather(graveyard.getCustomWeather());
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
        //System.out.println(1);
        Player player = event.getPlayer();
        Graveyard graveyard = GraveyardManager.getLastGraveyard(player);
        Location graveyardRespawnLoc = GraveyardManager.getLastRespawnLoc(player);
        if(graveyard == null && graveyardRespawnLoc == null) return;
        //System.out.println(2);
        Location respawnLocation = graveyardRespawnLoc != null ? graveyardRespawnLoc : graveyard.getSpawn();
        if(respawnLocation == null) return;
        //System.out.println(3);
        event.setRespawnLocation(respawnLocation);
        GraveyardManager.setLastGraveyard(player, null);
        GraveyardManager.setLastRespawnLoc(player, null);

        final Graveyard respawnGraveyard = graveyard != null ? graveyard : GraveyardManager.fromLocation(respawnLocation);

        if(respawnGraveyard != null && respawnGraveyard.hasCustomTotemAnimation()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> EntityUtils.playTotemAnimation(player, respawnGraveyard.getCustomTotemModelData()), 1L);
        }

        if(respawnGraveyard != null) {
            String title = respawnGraveyard.getTitle();
            String subtitle = respawnGraveyard.getSubtitle();
            if(title == null) title = "";
            if(subtitle == null) subtitle = "";
            final String ftitle = title;
            final String fsubtitle = subtitle;
            if(!title.isEmpty() || !subtitle.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    event.getPlayer().sendTitle(TextUtils.format(ftitle, event.getPlayer()), TextUtils.format(fsubtitle, event.getPlayer()), respawnGraveyard.getTitleFadein(), respawnGraveyard.getTitleStay(), respawnGraveyard.getTitleFadeout());
                }, 2L);
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDie(PlayerDeathEvent event) {
        //System.out.println("[GRAVEYARDS] PlayerDeathEvent");
        Graveyard graveyard = GraveyardManager.getLastGraveyard(event.getEntity());
        if(graveyard == null) {
            //System.out.println("[GRAVEYARDS] No graveyard associated");
            return;
        }
        if(!graveyard.isInstantRespawn()) {
            //System.out.println("[GRAVEYARDS] Instant Respawn disabled for this graveyard");
            return;
        }
        //System.out.println("[GRAVEYARDS] Respawning");
        Bukkit.getScheduler().runTask(main,() -> event.getEntity().spigot().respawn());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        GraveyardManager.init(false);
    }

}
