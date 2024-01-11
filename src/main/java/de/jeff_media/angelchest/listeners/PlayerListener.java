package de.jeff_media.angelchest.listeners;

import com.jeff_media.jefflib.*;
import com.jeff_media.jefflib.data.Cooldown;
import com.jeff_media.jefflib.data.McVersion;
import com.jeff_media.jefflib.pluginhooks.McMMOUtils;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.DeathCause;
import de.jeff_media.angelchest.data.Graveyard;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.events.AngelChestOpenEvent;
import de.jeff_media.angelchest.events.AngelChestSpawnEvent;
import de.jeff_media.angelchest.events.AngelChestSpawnPrepareEvent;
import de.jeff_media.angelchest.gui.GUIHolder;
import de.jeff_media.angelchest.handlers.DeathMapManager;
import de.jeff_media.angelchest.handlers.GraveyardManager;
import de.jeff_media.angelchest.hooks.*;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.utils.CommandUtils;
import de.jeff_media.angelchest.utils.ProtectionUtils;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy_Stepsister;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Handles player related events
 */
public final class PlayerListener implements Listener {

    final static AngelChestMain main = AngelChestMain.getInstance();
    private static final byte TOTEM_MAGIC_VALUE = 35;
    private static final NamespacedKey TOTEM_ADVANCEMENT = NamespacedKey.minecraft("adventure/totem_of_undying");
    private final HashMap<UUID, BukkitTask> respawnTasks = new HashMap<>();
    private final Cooldown pvpCooldowns = new Cooldown(); // TODO: Replace with PvpTracker
    private final Cooldown cooldowns = new Cooldown();

    @SuppressWarnings("unused")
    @EventHandler
    public void onAngelChestClose(final InventoryCloseEvent event) {

        for (final AngelChest angelChest : main.angelChests) {
            if (!angelChest.overflowInv.equals(event.getInventory())) {
                continue;
            }

            //Inventory inv = event.getInventory();
            if (Utils.isEmpty(angelChest.overflowInv) && AngelChestUtils.isEmpty(angelChest.armorInv) && AngelChestUtils.isEmpty(angelChest.extraInv) && AngelChestUtils.isEmpty(angelChest.storageInv)) {
                // plugin.angelChests.remove(Utils.getKeyByValue(plugin.angelChests,
                // angelChest));
                angelChest.destroy(false, false);
                main.angelChests.remove(angelChest);

                if (main.debug) main.debug("Inventory empty, removing chest");
                // Messages.send(event.getPlayer(),"You have emptied an AngelChest. It is now
                // gone.");
            }

            return;
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAngelChestRightClick(final PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        final Block block = event.getClickedBlock();
        if (!main.isAngelChest(block)) {
            return;
        }

        event.setCancelled(true);

        final AngelChest angelChest = main.getAngelChest(block);

        if(angelChest == null) return;

        // Test here if player is allowed to open THIS angelchest
        if (!main.protectionUtils.playerMayOpenThisChest(event.getPlayer(), angelChest)) {
            Messages.send(event.getPlayer(), main.messages.MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS);
            return;
        }

        final boolean firstOpened = !angelChest.openedBy.contains(p.getUniqueId().toString());

        openGUIorFastLoot(p, angelChest, firstOpened);

    }

    private void openGUIorFastLoot(final Player player, final AngelChest angelChest, final boolean firstOpened) {
        if (main.debug) main.debug("Attempting to open AngelChest " + angelChest + " for player " + player);

        if (!angelChest.hasPaidForOpening(player)) {
            return;
        }

        boolean openGUI = false;
        if (Daddy_Stepsister.allows(PremiumFeatures.GUI)) {
            if (main.getConfig().getString(Config.ALLOW_FASTLOOTING).equalsIgnoreCase("force")) {
                if (main.debug) main.debug("Not oening GUI because allow-fastlooting is force");
                openGUI = false;
            } else if (main.getConfig().getString(Config.ALLOW_FASTLOOTING).equalsIgnoreCase("false")) {
                if (main.debug) main.debug("Opening GUI because allow-fastlooting is disabled");
                openGUI = true;
            } else if (player.isSneaking() && main.getConfig().getBoolean(Config.GUI_REQUIRES_SHIFT)) {
                if (main.debug) main.debug("Opening GUI because player is sneaking and GUI requires Shift");
                openGUI = true;
            } else if (!player.isSneaking() && !main.getConfig().getBoolean(Config.GUI_REQUIRES_SHIFT)) {
                if (main.debug) main.debug("Opening GUI because player is NOT sneaking and GUI does NOT require Shift");
                openGUI = true;
            }
        }

        DeathMapManager.removeDeathMap(angelChest);

        if (openGUI) {
            main.guiManager.showPreviewGUI(player, angelChest, false, firstOpened);
        } else {
            main.debug("Fastlooting chest");
            fastLoot(player, angelChest, firstOpened);
        }

    }

    public static void fastLoot(final Player p, final AngelChest angelChest, boolean firstOpened) {

        AngelChestOpenEvent openEvent = new AngelChestOpenEvent(angelChest, p, AngelChestOpenEvent.Reason.FAST_LOOT);
        Bukkit.getPluginManager().callEvent(openEvent);
        if (openEvent.isCancelled()) {
            main.debug("AngelChestOpenEvent (Fast Loot) was cancelled.");
            return;
        }

        if (main.getConfig().getBoolean(Config.COMBATLOGX_PREVENT_FASTLOOTING)) {
            try {
                boolean isInCombat = PluginUtils.whenInstalled("CombatLogX", () -> CombatLogXHook.isInCombat(p), false);
                if (isInCombat) return;
            } catch (Exception | Error ignored) {

            }
        }


        if (p.getOpenInventory().getTopInventory() != null && p.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder) {
            //main.getLogger().warning("Player " + p.getName() + " attempted to fastloot an AngelChest while having an inventory open - possible duplication attempt using a hacked client, or just client lag.");
            return;
        }
        p.closeInventory();

        Utils.applyXp(p, angelChest);

        final boolean succesfullyStoredEverything;
        //boolean isOwnChest = angelChest.owner == p.getUniqueId();

        succesfullyStoredEverything = AngelChestUtils.tryToMergeInventories(main, angelChest, p.getInventory());
        if (succesfullyStoredEverything) {
            angelChest.isLooted = true;
            Messages.send(p, main.messages.MSG_YOU_GOT_YOUR_INVENTORY_BACK);

            // This is another player's chest
            if (Daddy_Stepsister.allows(PremiumFeatures.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_ANGELCHEST)) {
                if (!p.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_CHEST)) {
                    final Player tmpPlayer = Bukkit.getPlayer(angelChest.owner);
                    if (tmpPlayer != null) {
                        Messages.send(tmpPlayer, main.messages.MSG_EMPTIED.replace("{player}", p.getName()));
                    }
                }
            }

            angelChest.destroy(false, false);
            angelChest.remove();
            if (main.getConfig().getBoolean(Config.CONSOLE_MESSAGE_ON_OPEN)) {
                main.getLogger().info(p.getName() + " emptied the AngelChest of " + Bukkit.getOfflinePlayer(angelChest.owner).getName() + " at " + angelChest.block.getLocation());
            }
        } else {
            Messages.send(p, main.messages.MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK);

            // This is another player's chest
            if (Daddy_Stepsister.allows(PremiumFeatures.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_ANGELCHEST)) {
                if (!p.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_CHEST)) {
                    final Player tmpPlayer = Bukkit.getPlayer(angelChest.owner);
                    if (tmpPlayer != null) {
                        if (firstOpened) {
                            Messages.send(tmpPlayer, main.messages.MSG_OPENED.replace("{player}", p.getName()));
                            firstOpened = false;
                        }
                    }
                }
            }

            //p.openInventory(angelChest.overflowInv);
            main.guiManager.showPreviewGUI(p, angelChest, false, firstOpened);
            main.getLogger().info(p.getName() + " opened the AngelChest of " + Bukkit.getOfflinePlayer(angelChest.owner).getName() + " at " + angelChest.block.getLocation());
        }

        main.guiManager.updatePreviewInvs(null, angelChest);

    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onArmorStandRightClick(final PlayerInteractAtEntityEvent event) {

        if (main.getConfig().getBoolean(Config.DISABLE_HOLOGRAM_INTERACTION)) {
            return;
        }

        if (event.getRightClicked() == null) {
            return;
        }
        if (event.getRightClicked().getType() != EntityType.ARMOR_STAND) {
            return;
        }
        final AtomicReference<AngelChest> atomicAngelChest = new AtomicReference<>();
        if (main.isAngelChestHologram(event.getRightClicked())) {
            atomicAngelChest.set(main.getAngelChestByHologram((ArmorStand) event.getRightClicked()));
        }

        if (atomicAngelChest.get() == null) return;

        event.setCancelled(true);

        if (!main.protectionUtils.playerMayOpenThisChest(event.getPlayer(), atomicAngelChest.get())) {
            Messages.send(event.getPlayer(), main.messages.MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS);
            return;
        }
        final boolean firstOpened = !atomicAngelChest.get().openedBy.contains(event.getPlayer().getUniqueId().toString());

        openGUIorFastLoot(event.getPlayer(), atomicAngelChest.get(), firstOpened);
    }

    /**
     * Handles auto-respawning the player
     *
     * @param event PlayerDeathEvent
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void autoRespawn(final PlayerDeathEvent event) {
        if (!main.getConfig().getBoolean(Config.AUTO_RESPAWN)) return;
        final int delay = main.getConfig().getInt(Config.AUTO_RESPAWN_DELAY);

        respawnTasks.put(event.getEntity().getUniqueId(), Bukkit.getScheduler().runTaskLater(main, () -> {
            if (event.getEntity().isDead()) {
                event.getEntity().spigot().respawn();
            }
        }, 1L + Ticks.fromSeconds(delay)));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (respawnTasks.containsKey(event.getPlayer().getUniqueId())) {
            respawnTasks.get(event.getPlayer().getUniqueId()).cancel();
            respawnTasks.remove(event.getPlayer().getUniqueId());
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeathBecauseTotemNotEquipped(final EntityResurrectEvent e) {
        //if (main.debug) main.debug("EntityResurrectEvent");
        if (!(e.getEntity() instanceof Player)) return;

        if (!e.isCancelled()) {
            if (main.debug) main.debug("  R: Not cancelled");
            return;
        }

        if (!main.getConfig().getBoolean(Config.TOTEM_OF_UNDYING_WORKS_EVERYWHERE)) {
            if (main.debug) main.debug("  R: Config option disabled");
            return;
        }

        final Player p = (Player) e.getEntity();


        for (final ItemStack is : p.getInventory()) {
            if (is == null) continue;
            if (is.getType().name().equals("TOTEM_OF_UNDYING") || is.getType().name().equals("TOTEM")) {
                e.setCancelled(false);
                is.setAmount(is.getAmount() - 1);
                giveTotemAchievement(p);
                final ItemStack offHand = p.getInventory().getItemInOffHand();
                if (offHand != null && offHand.getAmount() != 0 && offHand.getType() != Material.AIR) {
                    final ItemStack finalOffHand = offHand.clone();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> p.getInventory().setItemInOffHand(finalOffHand), 1L);
                }
                return;
            }
        }

    }

    private void giveTotemAchievement(final @Nonnull Player player) {
        if(McVersion.current().isAtLeast(1,16)) {
            Advancement advancement = Bukkit.getAdvancement(TOTEM_ADVANCEMENT);
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancementProgress(advancement);
                for (String crit : progress.getRemainingCriteria()) {
                    progress.awardCriteria(crit);
                }
            }
        }
    }

    /**
     * Keeps track of the correlation between killed player and damaging entity
     *
     * @param event EntityDamageByEntityEvent
     */

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        final UUID player = event.getEntity().getUniqueId();
        final Entity killer = event.getDamager();
        main.killers.put(player, killer);
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> main.killers.remove(player), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent playerJoinEvent) {
        if (main.getConfig().getBoolean(Config.SHOW_LOCATION_ON_JOIN)) {
            final Player player = playerJoinEvent.getPlayer();
            if (player.hasPermission(Permissions.USE)) {
                if (!main.getAllAngelChestsFromPlayer(player).isEmpty()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                        Messages.send(player, main.messages.MSG_ANGELCHEST_LOCATION);
                        CommandUtils.sendListOfAngelChests(main, player, player);
                    }, 3L);
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent playerRespawnEvent) {
        if (main.debug) main.debug("Player Respawn: Show GUI to player?");
        if (!Daddy_Stepsister.allows(PremiumFeatures.GUI)) {
            if (main.debug) main.debug("  No: not using premium version");
            return;
        }
        final Player player = playerRespawnEvent.getPlayer();
        if (!player.hasPermission(Permissions.USE)) {
            if (main.debug) main.debug("  No: no angelchest.use permission");
            return;
        }
        final ArrayList<AngelChest> chests = AngelChestUtils.getAllAngelChestsFromPlayer(player);
        if (chests.isEmpty()) {
            if (main.debug) main.debug("  No: no AngelChests");
            return;
        }
        final String showGUIAfterDeath = main.getConfig().getString(Config.SHOW_GUI_AFTER_DEATH).toLowerCase();

        if (main.getConfig().getBoolean(Config.ONLY_SHOW_GUI_AFTER_DEATH_IF_PLAYER_CAN_TP_OR_FETCH)) {
            if (main.debug) main.debug(" Checking if player has fetch or tp permission...");
            if (!player.hasPermission(Permissions.FETCH) && !player.hasPermission(Permissions.TP)) {
                if (main.debug) main.debug("  No: Neither angelchest.fetch nor angelchest.tp permission");
                return;
            }
            if (main.debug) main.debug(" At least one of those permissions is given.");
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            switch (showGUIAfterDeath) {
                case "false":
                    if (main.debug) main.debug("  No: showGUIAfterDeath is false");
                    return;
                case "latest":
                    if (main.debug) main.debug("  Yes: show latest chest");
                    main.guiManager.showLatestChestGUI(player);
                    break;
                case "true":
                    if (main.debug) main.debug("  Yes: show all chests or latest if there is only one");
                    main.guiManager.showMainGUI(player);
                    break;
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobGrief(EntityChangeBlockEvent event) {
        if (main.isAngelChest(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlaceAngelChestItem(final BlockPlaceEvent event) {
        if (!main.getConfig().getBoolean(Config.PREVENT_PLACING_CUSTOM_ITEMS)) return;
        ItemStack item = event.getItemInHand();
        if (item == null || item.getAmount() == 0 || item.getItemMeta() == null) return;
        if (PDCUtils.has(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }

    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH)
    public void spawnAngelChestHigh(final PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.HIGH) {
            if (main.debug) main.debug("PlayerDeathEvent Priority HIGH");
            spawnAngelChest(event);
        }
    }

    /**
     * Attempts to spawn an AngelChest on player death
     *
     * @param event PlayerDeathEvent
     */
    private void spawnAngelChest(final PlayerDeathEvent event) {

        final Player player = event.getEntity();

        if (SentinelHook.isNpc(player)) {
            main.debug("Ignoring death from NPC \"player\": " + player);
            return;
        }

        if (main.debug) {
            TimeUtils.startTimings("AngelChest spawn");
        }

        if (main.debug) main.debug("\n");
        LogUtils.debugBanner(new String[]{"PlayerDeathEvent", "Player: " + player.getName(), "Location: " + player.getLocation()});

        if (main.disableDeathEvent) {
            if (main.debug)
                main.debug("PlayerDeathEvent: Doing nothing, AngelChest has been disabled for debug reasons!");
            return;
        }

        if (main.getConfig().getBoolean(Config.DISABLE_IN_CREATIVE) && player.getGameMode() == GameMode.CREATIVE) {
            if (main.debug) main.debug("Cancelled: Player is in Creative and disable-in-creative is true");
            return;
        }

        //final long startTime = System.nanoTime();

        final boolean isPvpDeath = player.getKiller() != null && player.getKiller() != player;

        // Print out all plugins/listeners that listen to the PlayerDeathEvent
        if (main.debug) {
            for (final RegisteredListener registeredListener : event.getHandlers().getRegisteredListeners()) {
                if (main.debug)
                    main.debug(registeredListener.getPlugin().getName() + ": " + registeredListener.getListener().getClass().getName() + " @ " + registeredListener.getPriority().name());
            }
        }

        if (main.debug) main.debug("PlayerListener -> spawnAngelChest");

        if (!player.hasPermission(Permissions.USE)) {
            if (main.debug) main.debug("Cancelled: no permission (angelchest.use)");
            return;
        }

        if (NBTAPI.hasNBT(player, NBTTags.HAS_ANGELCHEST_DISABLED)) {
            if (main.debug) main.debug("Cancelled: this player disabled AngelChest using /actoggle");
            return;
        }

        if (!Utils.isWorldEnabled(player.getLocation().getWorld())) {
            if (main.debug) main.debug("Cancelled: world disabled (" + player.getLocation().getWorld());
            return;
        }

        if (AngelChestMain.getWorldGuardWrapper().isBlacklisted(player.getLocation().getBlock())) {
            if (main.debug) main.debug("Cancelled: region disabled.");
            return;
        }

        if (!AngelChestMain.getWorldGuardWrapper().getAngelChestFlag(player)) {
            if (main.debug) main.debug("Cancelled: World Guard flag \"allow-angelchest\" is \"deny\"");
            return;
        }

        if (main.getConfig().getBoolean(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD) && !ProtectionUtils.playerMayBuildHere(player, player.getLocation())) {
            if (main.debug) main.debug("Cancelled: BlockPlaceEvent cancelled");
            return;
        }

        if (Daddy_Stepsister.allows(PremiumFeatures.GENERIC) && main.getConfig().getBoolean(Config.DONT_PROTECT_WHEN_AT_WAR) && Bukkit.getPluginManager().getPlugin("Lands") != null && LandsHook.isWarDeath(event)) {
            if (main.debug) main.debug("Cancelled: Player was in war with their killer (Lands plugin)");
            return;
        }

        if (Daddy_Stepsister.allows(PremiumFeatures.PROHIBIT_CHEST_IN_LAVA_OR_VOID)) {
            if (!main.getConfig().getBoolean(Config.ALLOW_CHEST_IN_LAVA) && player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA) {
                if (main.debug) main.debug("Cancelled: Lava, allow-chest-in-lava: false");
                return;
            }

            if (!main.getConfig().getBoolean(Config.ALLOW_CHEST_IN_VOID) && player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (main.debug) main.debug("Cancelled: Void, allow-chest-in-void: false");
                return;
            }
        }

        if (!main.getConfig().getBoolean(Config.ALLOW_ANGELCHEST_IN_PVP)) {
            if (isPvpDeath) {
                if (main.debug)
                    main.debug("Cancelled: allow-angelchest-in-pvp is false and this seemed to be a pvp death");
                if (main.getConfig().getBoolean(Config.DROP_HEADS)) {
                    if (Daddy_Stepsister.allows(PremiumFeatures.DROP_HEADS)) {
                        dropPlayerHead(player);
                    }
                }

                Utils.sendDelayedMessage(player, main.messages.MSG_NO_CHEST_IN_PVP, 1);
                return;
            }
        }

        if (isPvpDeath) {
            if (main.getConfig().getDouble(Config.PVP_COOLDOWN) > 0 && Daddy_Stepsister.allows(PremiumFeatures.PVP_COOLDOWN)) {
                int pvpCooldown = (int) main.getConfig().getDouble(Config.PVP_COOLDOWN);

                boolean isInCooldown = pvpCooldowns.hasCooldown(player);
                if (isInCooldown) {
                    if (main.debug) {
                        main.debug("Cancelled: player is still in pvp-cooldown");
                    }
                    Messages.send(player, main.messages.MSG_PVP_COOLDOWN);
                    return;
                } else {
                    pvpCooldowns.setCooldown(player, pvpCooldown, TimeUnit.SECONDS);
                }
            }
        } else {
            if (main.getConfig().getDouble(Config.COOLDOWN) > 0 && Daddy_Stepsister.allows(PremiumFeatures.COOLDOWN)) {
                int cooldown = (int) main.getConfig().getDouble(Config.COOLDOWN);

                boolean isInCooldown = cooldowns.hasCooldown(player);
                if (isInCooldown) {
                    if (main.debug) {
                        main.debug("Cancelled: player is still in no-pvp-cooldown");
                    }
                    Messages.send(player, main.messages.MSG_COOLDOWN);
                    return;
                } else {
                    cooldowns.setCooldown(player, cooldown, TimeUnit.SECONDS);
                }
            }
        }

        if (!AngelChestUtils.spawnChance(main.groupManager.getSpawnChancePerPlayer(player))) {
            if (main.debug) main.debug("Cancelled: unlucky, spawnChance returned false!");
            Utils.sendDelayedMessage(player, main.messages.MSG_SPAWN_CHANCE_UNSUCCESFULL, 1);
            return;
        }

        // EcoEnchants Telekinesis
        if (EcoEnchantsHook.dontSpawnChestBecausePlayerWasKilledByTelekinesis(event)) {
            if (main.debug) main.debug("Cancelled: Player was killed by telekinesis");
            Utils.sendDelayedMessage(player, main.messages.MSG_NO_CHEST_IN_PVP, 1);
            return;
        }

        if (event.getKeepInventory()) {
            if (!main.getConfig().getBoolean(Config.IGNORE_KEEP_INVENTORY)) {
                if (main.debug) main.debug("Cancelled: event#getKeepInventory() == true");
                if (main.debug) main.debug("Please check if your kept your inventory on death!");
                if (main.debug)
                    main.debug("This is probably because some other plugin tries to handle your inv on death.");
                if (main.debug) main.debug(player.getDisplayName() + " is OP: " + player.isOp());
                return;
            } else {
                if (main.debug)
                    main.debug("event#getKeepInventory() == true but we ignore it because of config settings");
                event.setKeepInventory(false);
            }
        }

        // Start fix for CommandPanels
        InventoryView view = player.getOpenInventory();
        if(view != null) {
            InventoryCloseEvent closeEvent = new InventoryCloseEvent(view);
            Bukkit.getServer().getPluginManager().callEvent(closeEvent);
        }
        // End fix for CommandPanels


        Block fixedPlayerPosition;
        List<Predicate<Block>> predicates = new ArrayList<>();

        // Player died below world
        if (player.getLocation().getBlockY() < main.getWorldMinHeight(player.getWorld())) {
            if (main.debug)
                main.debug("Fixing player position for " + player.getLocation() + " because Y < World#getMinHeight()");
            fixedPlayerPosition = null;
            // Void detection: use last known position
            if (main.getConfig().getBoolean(Config.VOID_DETECTION)) {
                if (main.lastPlayerPositions.containsKey(player.getUniqueId())) {
                    fixedPlayerPosition = main.lastPlayerPositions.get(player.getUniqueId());
                    if (main.debug) main.debug("Using last known player position " + fixedPlayerPosition.getLocation());
                }
            }
            // Void detection disabled or no last known position: set to Y=1
            if (fixedPlayerPosition == null) {
                final Location ltmp = player.getLocation();
                ltmp.setY(main.getWorldMinHeight(player.getWorld()) + 1);
                fixedPlayerPosition = ltmp.getBlock();
                if (main.debug)
                    main.debug("Void detection disabled or no last known player position, setting Y to minWorldHeight+1 " + fixedPlayerPosition.getLocation());
            }
        } else {
            fixedPlayerPosition = player.getLocation().getBlock();
            if (main.debug) main.debug("Void fixing not needed for " + fixedPlayerPosition.getLocation());
        }

        // Player died above build limit
        // Note: This has to be checked AFTER the "below world" check, because the lastPlayerPositions could return 256
        if (fixedPlayerPosition.getY() >= main.getWorldMaxHeight(player.getWorld())) {
            if (main.debug)
                main.debug("Fixing player position for " + player.getLocation() + " because Y >= World#getMaxHeight()");
            final Location ltmp = player.getLocation();
            ltmp.setY(main.getWorldMaxHeight(player.getWorld()) - 1);
            fixedPlayerPosition = ltmp.getBlock();
            if (main.debug) main.debug("Setting Y to World#getMaxHeight()-1 " + fixedPlayerPosition.getLocation());
        } else {
            //fixedPlayerPosition = p.getLocation().getBlock();
            if (main.debug) main.debug("MaxHeight fixing not needed for " + fixedPlayerPosition.getLocation());
        }

        // Player died in Lava
        boolean diedThroughLava = false;
        if (player.getLastDamageCause() != null && player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA) {
            diedThroughLava = true;
        }
        if (main.getConfig().getBoolean(Config.LAVA_DETECTION) && (fixedPlayerPosition.getType() == Material.LAVA || diedThroughLava)) {
            if (main.debug) main.debug("Fixing player position for " + player.getLocation() + " because there's lava");
            if (main.lastPlayerPositions.containsKey(player.getUniqueId())) {
                fixedPlayerPosition = main.lastPlayerPositions.get(player.getUniqueId());
                if (main.debug) main.debug("Using last known player position " + fixedPlayerPosition.getLocation());
            }
        }
        if (main.getConfig().getBoolean(Config.AVOID_LAVA_OCEANS) && fixedPlayerPosition.getType() == Material.LAVA) {
            if (main.debug) main.debug("Adding predicate \"avoid-lava-oceans\"");
            predicates.add(block -> !(block.getY() < player.getLocation().getY()));
        }

        // Prevent destroying itemframes
        predicates.add(block -> {
            Collection<Entity> nearby = block.getWorld().getNearbyEntities(BoundingBox.of(block));
            for (Entity entity : nearby) {
                if (entity instanceof Hanging) {
                    return false;
                }
            }
            return true;
        });

        if (main.debug) main.debug("FixedPlayerPosition: " + fixedPlayerPosition);
        Block finalFixedPlayerPosition = fixedPlayerPosition;
        Block angelChestBlock = AngelChestUtils.getChestLocation(fixedPlayerPosition, predicates.toArray(new Predicate[]{new Predicate<Block>() {
            @Override
            public boolean test(Block block) {
                return HookHandler.isInsideWorldBorder(finalFixedPlayerPosition.getLocation(), event.getEntity());
            }
        }}));

        Location graveyardBlock = null;

        // Graveyards start
        if (Daddy_Stepsister.allows(PremiumFeatures.GRAVEYARDS)) {
            //if(GraveyardManager.hasGraveyard(angelChestBlock.getWorld())) {
            boolean tryClosest = main.getConfig().getBoolean(Config.TRY_CLOSEST_GRAVEYARD);
            boolean tryGlobal = main.getConfig().getBoolean(Config.TRY_GLOBAL_GRAVEYARD);
            boolean fallbackToDeathLocation = main.getConfig().getBoolean(Config.FALLBACK_TO_DEATHLOCATION);
            Block grave = GraveyardManager.getGraveLocation(angelChestBlock.getLocation(), tryClosest, tryGlobal);
            if (grave == null) {
                if (fallbackToDeathLocation) {
                    main.debug("Could not find a matching grave for player " + player.getName() + ". Using normal death location.");
                } else {
                    main.debug("Could not find a matching grave for player " + player.getName() + ". Disabling AngelChest spawn.");
                    return;
                }
            } else {
                //System.out.println("Player will be sent to a graveyard");
                if (!main.getConfig().getBoolean(Config.GRAVEYARDS_ONLY_AS_RESPAWN_POINT)) {
                    angelChestBlock = grave;
                }
                graveyardBlock = grave.getLocation().add(0.5, 0.0, 0.5);
                Graveyard yard = GraveyardManager.fromLocation(graveyardBlock);
                if (yard != null && yard.getSpawn() != null) {
                    graveyardBlock = yard.getSpawn();
                }
            }
            //}
        }
        // Graveyards end

        // Calling Event
        EntityDamageEvent lastDamageEvent = player.getLastDamageCause();
        EntityDamageEvent.DamageCause lastDamageCause = lastDamageEvent == null ? null : lastDamageEvent.getCause();
        final AngelChestSpawnPrepareEvent angelChestSpawnPrepareEvent = new AngelChestSpawnPrepareEvent(player, angelChestBlock, lastDamageCause, event);
        Bukkit.getPluginManager().callEvent(angelChestSpawnPrepareEvent);
        if (angelChestSpawnPrepareEvent.isCancelled()) {
            if (main.debug) main.debug("AngelChestCreateEvent has been cancelled!");
            return;
        }
        angelChestBlock = angelChestSpawnPrepareEvent.getBlock();
        final Block finalAngelChestBlock = angelChestBlock;
        final ItemStack priceItem = main.getItemManager().getItem(main.getConfig().getString(Config.PRICE));

        if (!CommandUtils.hasEnoughMoney(player, main.groupManager.getSpawnPricePerPlayer(player), priceItem, main.messages.MSG_NOT_ENOUGH_MONEY_CHEST, main.messages.MSG_HAS_NO_ITEM2, "AngelChest spawned")) {
            return;
        }

        // Enable keep inventory to prevent drops (this is not preventing the drops at the moment due to spigot)
        // (unless DONT_CLEAR_INVENTORY is set, in that case don't do anything)
        // TODO: Move this below
        if(!main.getConfig().getBoolean(Config.DONT_CLEAR_INVENTORY)) {
            event.setKeepInventory(true);
        }

        // DETECT ALL DROPS, EVEN FRESHLY ADDED
        final ArrayList<ItemStack> freshDrops = new ArrayList<>();
        ItemStack[] drops = new ItemStack[0]; // = event.getDrops().toArray(new ItemStack[0]);
        List<ItemStack> inventoryAsList = new ArrayList<>(player.getInventory().getSize()); // = Arrays.asList(player.getInventory().getContents());

        try {
            drops = event.getDrops().toArray(new ItemStack[0]);
        } catch (Exception debug) {
            if(main.debug) {
                main.debug("Could not turn PlayerDeathEvent#getDrops() to array. This is probably due to a plugin interfering with the PlayerDeathEvent:");
                //main.debug("drops: " + event.getDrops());
                debug.printStackTrace();
            }
        }

        try {
            inventoryAsList = Arrays.asList(player.getInventory().getContents());
        } catch (Exception debug) {
            if(main.debug) {
                debug.printStackTrace();
            }
        }

        // TODO: Maybe rename this to keptItems in general?
        HashMap<Integer, ItemStack> keptAngelChestTokens = new HashMap<>();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack tmp = player.getInventory().getItem(i);
            if (tmp == null || tmp.getType().isAir() || tmp.getAmount() == 0) continue;
            if (PDCUtils.has(tmp, NBTTags.IS_TOKEN_ITEM_KEEP, PersistentDataType.BYTE)) {
                keptAngelChestTokens.put(i, tmp);
                player.getInventory().setItem(i, null);
            }
        }

        Bukkit.getScheduler().runTaskLater(main, () -> {
            for (Map.Entry<Integer, ItemStack> entry : keptAngelChestTokens.entrySet()) {
                ItemStack inInv = player.getInventory().getItem(entry.getKey());
                if (inInv == null || inInv.getType().isAir() || inInv.getAmount() == 0) {
                    player.getInventory().setItem(entry.getKey(), entry.getValue());
                } else {
                    player.getInventory().addItem(entry.getValue());
                }
            }
        }, 1);

        if(main.getConfig().getBoolean(Config.DETECT_ADDITIONAL_DROPS)) {
            LogUtils.debugBanner(new String[]{"ADDITIONAL DEATH DROP LIST"});
            if (main.debug) main.debug("The following items are in the drops list, but not in the inventory.");
            for (int i = 0; i < drops.length; i++) {
                if (inventoryAsList.contains(drops[i])) continue;
                if (main.debug) main.debug(String.format("Drop %d: %s", i, drops[i]));
                if (main.debug) main.debug(" ");
                freshDrops.add(drops[i]);
            }
            LogUtils.debugBanner(new String[]{"ADDITIONAL DEATH DROP LIST END"});
        }

        if (main.getConfig().getBoolean(Config.DROP_HEADS) && Daddy_Stepsister.allows(PremiumFeatures.DROP_HEADS)) {
            boolean dropHead = false;
            if (main.getConfig().getBoolean(Config.ONLY_DROP_HEADS_IN_PVP)) {
                if (isPvpDeath) {
                    dropHead = true;
                }
            } else {
                dropHead = true;
            }

            if (dropHead) {
                if (main.getConfig().getBoolean(Config.DONT_STORE_HEADS_IN_ANGELCHEST)) {
                    dropPlayerHead(player);
                } else {
                    freshDrops.add(getPlayerHead(player));
                }
            }
        }

        for (final ItemStack freshDrop : freshDrops) {

            // Moneyhunters start
            if(MoneyhuntersHook.isMoneyHuntersItem(freshDrop)) {
                player.getWorld().dropItemNaturally(player.getLocation(), freshDrop);
                continue;
            }
            // Moneyhunters end

            for (final ItemStack leftover : player.getInventory().addItem(freshDrop).values()) {
                if (leftover == null || leftover.getAmount() == 0 || leftover.getType() == Material.AIR) continue;
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                main.getLogger().info("Could not add item to already full AngelChest of player " + player.getName() + ": " + leftover + ", dropping it to world @ " + player.getLocation());
            }
        }
        // END DETECT ALL DROPS

        /*
        Creating the chest
         */
        final DeathCause deathCause = new DeathCause(player.getLastDamageCause());
        final AngelChest ac = new AngelChest(player, finalAngelChestBlock, main.logger.getLogFileName(event), deathCause, event);
        main.angelChests.add(ac);


        /*
        Experience
         */
        //noinspection StatementWithEmptyBody
        if (Daddy_Stepsister.allows(PremiumFeatures.DISALLOW_XP_COLLECTION) && main.getConfig().getString(Config.COLLECT_XP).equalsIgnoreCase("false")) {
            // Do nothing
        } else //noinspection StatementWithEmptyBody
            if (Daddy_Stepsister.allows(PremiumFeatures.DISALLOW_XP_COLLECTION_IN_PVP) && main.getConfig().getString(Config.COLLECT_XP).equalsIgnoreCase("nopvp") && (player.getKiller() != null && player.getKiller() != player)) {
                // Do nothing
            } else if (!event.getKeepLevel() && event.getDroppedExp() != 0) {
                final double xpPercentage = main.groupManager.getXPPercentagePerPlayer(player);
                if (main.debug) main.debug("Player has xpPercentage of " + xpPercentage);
                if (xpPercentage == -1 || !Daddy_Stepsister.allows(PremiumFeatures.PERCENTAL_XP_LOSS)) {
                    ac.experience = event.getDroppedExp();
                } else {
                    final float currentXP = XPUtils.getTotalXPRequiredForLevel(player.getLevel());
                    if (main.debug) main.debug("currentXP = " + currentXP + " (for this level)");
                    if (main.debug) main.debug("p.getEXP = " + player.getExp());
                    final double remainingXP = player.getExp() * XPUtils.getXPRequiredForNextLevel(player.getLevel());
                    if (main.debug) main.debug("Remaining XP = " + remainingXP);
                    final double totalXP = currentXP + remainingXP;
                    if (main.debug) main.debug("Total XP = " + totalXP);
                    final double adjustedXP = totalXP * xpPercentage;
                    if (main.debug) main.debug("adjustedXP = " + adjustedXP);
                    ac.experience = (int) adjustedXP;
                }
                event.setDroppedExp(0);
            }

        Graveyard graveyard = ac.graveyard;

        if(main.getConfig().getBoolean(Config.DISPOSE_DEATH_MAPS)) {
            DeathMapManager.removeDeathMapsFromChestContents(ac);
        }

        /*
        Clearing inventory
         */
        // TODO: DEBUG: This was previously BELOW the block "if (ac.isEmpty()) {"
        // Delete players inventory except excluded items
        if(!main.getConfig().getBoolean(Config.DONT_CLEAR_INVENTORY)) {
            clearInventory(player.getInventory());
        } else {
            clearDeathDrops(event.getDrops());
        }

        /*

        From here, there's no way to cancel the chest creation anymore-
        Todo: Don't even spawn the chest yet.
        Todo: Don't charge the player yet.

         */


        setRespawnLocationToGraveyardIfApplicable(player, graveyardBlock, graveyard);

        //ac.createChest(ac.block, ac.owner);

        main.logger.logDeath(event, ac);

        // Clear the drops except blacklisted items
        // DEBUG
        // TODO
        // Fix duplication of items blacklisted by slot
        // Original code: event.getDrops().removeIf(drop -> !ac.blacklistedItems.contains(drop));
        // Working solution start
        event.getDrops().removeIf(drop -> {
            boolean remove = !ac.blacklistedItems.contains(drop);
            if(!remove) {
                ac.blacklistedItems.remove(drop);
            }
            return remove;
        });
        // Working solution end
        //event.getDrops().removeIf(ac.blacklistedItems::remove);
        //event.getDrops().removeIf(drop -> !EqualUtils.contains(ac.blacklistedItems, drop));




                /*
        Check if player has any drops
         */
        if (ac.isEmpty()) {
            if (main.debug) main.debug("Cancelled: AngelChest would be empty.");
            if (main.debug) main.debug("Either your inventory and XP was empty, or another plugin set your");
            if (main.debug) main.debug("drops and XP to zero.");

            ac.remove();
            ac.destroy(true, false);
            main.angelChests.remove(ac);

            Utils.sendDelayedMessage(player, main.messages.MSG_INVENTORY_WAS_EMPTY, 1);

            if (main.getConfig().getBoolean(Config.GRAVEYARDS_ONLY_AS_RESPAWN_POINT)) {
                setRespawnLocationToGraveyardIfApplicable(player, graveyardBlock, graveyard);
            }

            return;
        }



        // send message after one twentieth second
        String playerDeathMessage = main.messages.MSG_ANGELCHEST_CREATED;
        if (graveyard != null) {
            playerDeathMessage = main.messages.MSG_BURIED_IN_GRAVEYARD.replace("{graveyard}", graveyard.getName());
        }
        Utils.sendDelayedMessage(player, playerDeathMessage, 1);

        if (main.getConfig().getBoolean(Config.SHOW_LOCATION)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> CommandUtils.sendListOfAngelChests(main, player, player), 2);
        }

        final int maxChests = main.groupManager.getChestsPerPlayer(player);
        final ArrayList<AngelChest> chests = AngelChestUtils.getAllAngelChestsFromPlayer(player);
        //System.out.println(chests.size()+" chests.size");
        if (chests.size() > maxChests) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder) {
                    GUIHolder holder = (GUIHolder) viewer.getOpenInventory().getTopInventory().getHolder();
                    if (holder.getAngelChest() == chests.get(0)) {
                        viewer.closeInventory();
                    }
                }
            }
            AngelChest toExplode = chests.get(0);
                    toExplode.destroy(true, false, main.getConfig().getString(Config.DROP_BEHAVIOUR_MAX_ANGELCHESTS, "drop").equalsIgnoreCase("delete"));
                    if(main.getConfig().getString(Config.DROP_BEHAVIOUR_MAX_ANGELCHESTS, "drop").equalsIgnoreCase("drop")) {
                        toExplode.dropContents();
                    }
                    main.angelChests.remove(toExplode);

            Bukkit.getScheduler().runTaskLater(main, () -> {
                //Messages.send(player, " ");
                Messages.send(player, main.messages.MSG_ANGELCHEST_EXPLODED);
            }, 3L);

        }

        if (Daddy_Stepsister.allows(PremiumFeatures.DONT_PROTECT_ANGELCHESTS_IN_PVP)) {
            if (main.getConfig().getBoolean(Config.DONT_PROTECT_CHEST_IF_PLAYER_DIED_IN_PVP)) {
                if (player.getKiller() != null && player.getKiller() != player) {
                    ac.isProtected = false;
                }
            }
        }

        //Utils.reloadAngelChest(ac,plugin);

        // mcMMO Super ability start
        for (ItemStack itemStack : ac.getStorageInv()) {
            McMMOUtils.removeSuperAbilityBoost(itemStack);
        }
        for (ItemStack itemStack : ac.getArmorInv()) {
            McMMOUtils.removeSuperAbilityBoost(itemStack);
        }
        McMMOUtils.removeSuperAbilityBoost(ac.getOffhandItem());
        // mcMMO Super ability end

        @SuppressWarnings("RedundantCast") final AngelChestSpawnEvent angelChestSpawnEvent = new AngelChestSpawnEvent(
                /* DO NOT REMOVE THE CAST!                      */
                /* It would result in a MethodNotFoundException */
                (de.jeff_media.angelchest.AngelChest) ac);
        Bukkit.getPluginManager().callEvent(angelChestSpawnEvent);

        /*long durationNano = System.nanoTime() - startTime;
        long durationMilli = TimeUtils.nanoSecondsToMilliSeconds(durationNano);
        double tickPercentage = TimeUtils.milliSecondsToTickPercentage(durationMilli);
        if(main.debug) main.debug("AngelChest creation took " +durationNano + " ns or " + durationMilli +" ms or "+tickPercentage+" % of tick.");*/

        if (main.debug) main.debug(" ");

        if (Daddy_Stepsister.allows(PremiumFeatures.PLAY_TOTEM_ANIMATION) && main.getConfig().getBoolean(Config.PLAY_TOTEM_ANIMATION)) {
            EntityUtils.playTotemAnimation(player, main.getConfig().getInt(Config.TOTEM_CUSTOM_MODEL_DATA));
        }

        ac.createChest();

        //System.out.println("New added chest: " + ac.block);

        if (main.getConfig().getBoolean(Config.DEATH_MAPS)) {
            if (Daddy_Stepsister.allows(PremiumFeatures.DEATH_MAP)) {
                ItemStack deathMap = DeathMapManager.getDeathMap(ac);
                Bukkit.getScheduler().runTaskLater(main, () -> player.getInventory().addItem(deathMap), 1);
            } else {
                Messages.sendPremiumOnly(Config.DEATH_MAPS);
            }
        }

        if (main.debug) {
            TimeUtils.endTimings("AngelChest spawn", main, true);
        }

        LogUtils.debugBanner(new String[]{"PlayerDeathEvent END"});

    }

    private static void dropPlayerHead(final Player player) {
        final ItemStack head = getPlayerHead(player);
        player.getLocation().getWorld().dropItemNaturally(player.getLocation(), head);
    }

    private static ItemStack getPlayerHead(final OfflinePlayer player) {
        return HeadCreator.getPlayerHead(player.getUniqueId());
    }

    private void setRespawnLocationToGraveyardIfApplicable(Player p, Location graveyardBlock, Graveyard graveyard) {
        if (graveyard != null) {
            //System.out.println("[GRAVEYARDS] Registering Graveyard death");
            GraveyardManager.setLastGraveyard(p, graveyard);
        } else if (graveyardBlock != null) {
            GraveyardManager.setLastRespawnLoc(p, graveyardBlock);
        }
    }

    /**
     * Remove all items from inventory that should not be kept on death
     *
     * @param inv inventory
     */
    private void clearInventory(final Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (main.genericHooks.keepOnDeath(inv.getItem(i))) {
                continue;
            }
            if (main.isItemBlacklisted(inv.getItem(i), i) != null) {
                continue;
            }
            inv.setItem(i, null);
        }
    }

    private void clearDeathDrops(final List<ItemStack> inv) {
        inv.removeIf(item -> !main.genericHooks.keepOnDeath(item) && main.isItemBlacklisted(item, -1) == null);
//        for (int i = 0; i < inv.size(); i++) {
//            if (main.genericHooks.keepOnDeath(inv.get(i))) {
//                continue;
//            }
//            if (main.isItemBlacklisted(inv.get(i), i) != null) {
//                continue;
//            }
//            inv.set(i, null);
//        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void spawnAngelChestHighest(final PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.HIGHEST) {
            if (main.debug) main.debug("PlayerDeathEvent Priority HIGHEST");
            spawnAngelChest(event);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOW)
    public void spawnAngelChestLow(final PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.LOW) {
            if (main.debug) main.debug("PlayerDeathEvent Priority LOW");
            spawnAngelChest(event);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    public void spawnAngelChestLowest(final PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.LOWEST) {
            if (main.debug) main.debug("PlayerDeathEvent Priority LOWEST");
            spawnAngelChest(event);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR)
    public void spawnAngelChestMonitor(final PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.MONITOR) {
            if (main.debug) main.debug("PlayerDeathEvent Priority MONITOR");
            spawnAngelChest(event);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void spawnAngelChestNormal(final PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.NORMAL) {
            if (main.debug) main.debug("PlayerDeathEvent Priority NORMAL");
            spawnAngelChest(event);
        }
    }

}
