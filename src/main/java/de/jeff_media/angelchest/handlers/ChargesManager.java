package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.daddy.Daddy_Stepsister;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChargesManager {

    private final AngelChestMain plugin;
    private final NamespacedKey remainingChargesKey;
    private final ConcurrentMap<UUID, Integer> offlineCharges = new ConcurrentHashMap<>();

    public ChargesManager(AngelChestMain plugin) {
        this.plugin = plugin;
        this.remainingChargesKey = new NamespacedKey(plugin, "remainingCharges");
    }

    public boolean isActive() {
        if(!plugin.getConfig().getBoolean(Config.ENABLE_CHARGES_SYSTEM)) return false;
        return Daddy_Stepsister.allows(PremiumFeatures.CHARGES_SYSTEM);
    }

    public boolean hasEnoughChargesAndReduceByOne(Player player) {
        if(!isActive()) return true;
        int remainingCharges = getRemainingCharges(player);
        if(remainingCharges <= 0) return false;
        setRemainingCharges(player, remainingCharges - 1);
        return true;
    }

    public CompletableFuture<Void> setRemainingCharges(UUID offlinePlayer, int remainingCharges) {
        Player player = Bukkit.getPlayer(offlinePlayer);
        if(player != null) {
            player.getPersistentDataContainer().set(remainingChargesKey, PersistentDataType.INTEGER, remainingCharges);
            clearOfflineCharges(offlinePlayer);
            return CompletableFuture.completedFuture(null);
        }
        offlineCharges.put(offlinePlayer, remainingCharges);
        plugin.getConfig().set(chargesPath(offlinePlayer), remainingCharges);
        plugin.saveConfig();
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Integer> getRemainingCharges(UUID offlinePlayer) {
        Player player = Bukkit.getPlayer(offlinePlayer);
        if(player != null) {
            final String path = chargesPath(offlinePlayer);
            if (plugin.getConfig().contains(path)) {
                final int remainingCharges = plugin.getConfig().getInt(path);
                player.getPersistentDataContainer().set(remainingChargesKey, PersistentDataType.INTEGER, remainingCharges);
                clearOfflineCharges(offlinePlayer);
                return CompletableFuture.completedFuture(remainingCharges);
            }
            return CompletableFuture.completedFuture(player.getPersistentDataContainer().getOrDefault(remainingChargesKey, PersistentDataType.INTEGER, 0));
        }
        return CompletableFuture.completedFuture(offlineCharges.computeIfAbsent(offlinePlayer,
                uuid -> plugin.getConfig().getInt(chargesPath(uuid), 0)));
    }

    public CompletableFuture<Integer> addCharges(UUID offlinePlayer, int chargesToAdd) {
        Player player = Bukkit.getPlayer(offlinePlayer);
        if(player != null) {
            int remainingCharges = player.getPersistentDataContainer().getOrDefault(remainingChargesKey, PersistentDataType.INTEGER, 0);
            setRemainingCharges(player, remainingCharges + chargesToAdd);
            return CompletableFuture.completedFuture(remainingCharges + chargesToAdd);
        }
        return getRemainingCharges(offlinePlayer)
                .thenCompose(remainingCharges -> setRemainingCharges(offlinePlayer, remainingCharges + chargesToAdd)
                        .thenApply(ignored -> remainingCharges + chargesToAdd));
    }

    public CompletableFuture<Integer> removeCharges(UUID offlinePlayer, int chargesToRemove) {
        Player player = Bukkit.getPlayer(offlinePlayer);
        if(player != null) {
            int remainingCharges = player.getPersistentDataContainer().getOrDefault(remainingChargesKey, PersistentDataType.INTEGER, 0);
            setRemainingCharges(player, Math.max(0, remainingCharges - chargesToRemove));
            return CompletableFuture.completedFuture(Math.max(0, remainingCharges - chargesToRemove));
        }
        return getRemainingCharges(offlinePlayer)
                .thenCompose(remainingCharges -> setRemainingCharges(offlinePlayer, Math.max(0, remainingCharges - chargesToRemove))
                        .thenApply(ignored -> Math.max(0, remainingCharges - chargesToRemove)));
    }

    public void setRemainingCharges(Player player, int remainingCharges) {
        player.getPersistentDataContainer().set(remainingChargesKey, PersistentDataType.INTEGER, remainingCharges);
        clearOfflineCharges(player.getUniqueId());
    }

    public int getRemainingCharges(Player player) {
        return player.getPersistentDataContainer().getOrDefault(remainingChargesKey, PersistentDataType.INTEGER, 0);
    }

    public int addCharges(Player player, int chargesToAdd) {
        int remainingCharges = getRemainingCharges(player);
        setRemainingCharges(player, remainingCharges + chargesToAdd);
        return remainingCharges + chargesToAdd;
    }

    public int removeCharges(Player player, int chargesToRemove) {
        int remainingCharges = getRemainingCharges(player);
        setRemainingCharges(player, Math.max(0, remainingCharges - chargesToRemove));
        return Math.max(0, remainingCharges - chargesToRemove);
    }

    private String chargesPath(final UUID uuid) {
        return "charges." + uuid;
    }

    private void clearOfflineCharges(final UUID uuid) {
        offlineCharges.remove(uuid);
        final String path = chargesPath(uuid);
        if (plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, null);
            plugin.saveConfig();
        }
    }
}
