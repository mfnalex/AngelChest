package de.jeff_media.angelchest.listeners;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.data.AngelChest;
import de.jeff_media.angelchest.data.AngelChestHolder;
import de.jeff_media.angelchest.data.DeathCause;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.events.AngelChestSpawnEvent;
import de.jeff_media.angelchest.events.AngelChestSpawnPrepareEvent;
import de.jeff_media.angelchest.gui.GUIHolder;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.angelchest.nms.NMSHandler;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.NBTAPI;
import de.jeff_media.jefflib.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles player related events
 */
public final class PlayerListener implements Listener {

    private static final byte TOTEM_MAGIC_VALUE = 35;
    final static Main main = Main.getInstance();

    private static void dropPlayerHead(final Player player) {
        final ItemStack head = getPlayerHead(player);
        player.getLocation().getWorld().dropItemNaturally(player.getLocation(), head);
    }

    private static ItemStack getPlayerHead(final OfflinePlayer player) {
        return HeadCreator.getPlayerHead(player.getUniqueId());
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
            if (main.isItemBlacklisted(inv.getItem(i)) != null) {
                continue;
            }
            inv.setItem(i, null);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onAngelChestClose(final InventoryCloseEvent event) {

        for (final AngelChest angelChest : main.angelChests.values()) {
            if (!angelChest.overflowInv.equals(event.getInventory())) {
                continue;
            }

            //Inventory inv = event.getInventory();
            if (Utils.isEmpty(angelChest.overflowInv) && AngelChestUtils.isEmpty(angelChest.armorInv) && AngelChestUtils.isEmpty(angelChest.extraInv) && AngelChestUtils.isEmpty(angelChest.storageInv)) {
                // plugin.angelChests.remove(Utils.getKeyByValue(plugin.angelChests,
                // angelChest));
                angelChest.destroy(false);

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

        final AngelChest angelChest = main.angelChests.get(block);

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
        if (Daddy.allows(PremiumFeatures.GUI)) {
            if (!main.getConfig().getBoolean(Config.ALLOW_FASTLOOTING)) {
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

        if (openGUI) {
            main.guiManager.showPreviewGUI(player, angelChest, false, firstOpened);
        } else {
            main.debug("Fastlooting chest");
            fastLoot(player, angelChest, firstOpened);
        }

    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onArmorStandRightClick(final PlayerInteractAtEntityEvent event) {
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
    public void onDeath(final PlayerDeathEvent event) {
        if (!main.getConfig().getBoolean(Config.AUTO_RESPAWN)) return;
        final int delay = main.getConfig().getInt(Config.AUTO_RESPAWN_DELAY);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (event.getEntity().isDead()) {
                event.getEntity().spigot().respawn();
            }
        }, 1L + Ticks.fromSeconds(delay));
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
                final ItemStack offHand = p.getInventory().getItemInOffHand();
                if (offHand != null && offHand.getAmount() != 0 && offHand.getType() != Material.AIR) {
                    final ItemStack finalOffHand = offHand.clone();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> p.getInventory().setItemInOffHand(finalOffHand), 1L);
                }
                return;
            }
        }

    }

    /**
     * Keeps track of the correlation between killed player and damaging entity
     *
     * @param event EntityDamageByEntityEvent
     */
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent playerRespawnEvent) {
        if (main.debug) main.debug("Player Respawn: Show GUI to player?");
        if (!Daddy.allows(PremiumFeatures.GUI)) {
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

    public static void fastLoot(final Player p, final AngelChest angelChest, boolean firstOpened) {
        if(p.getOpenInventory().getTopInventory() != null && p.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder) {
            main.getLogger().warning("Player " + p.getName() + " attempted to fastloot an AngelChest while having an inventory open - possible duplication attempt using a hacked client, or just client lag.");
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
            if (Daddy.allows(PremiumFeatures.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_ANGELCHEST)) {
                if (!p.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_EMPTIES_CHEST)) {
                    final Player tmpPlayer = Bukkit.getPlayer(angelChest.owner);
                    if (tmpPlayer != null) {
                        Messages.send(tmpPlayer, main.messages.MSG_EMPTIED.replaceAll("\\{player}", p.getName()));
                    }
                }
            }

            angelChest.destroy(false);
            angelChest.remove();
            if (main.getConfig().getBoolean(Config.CONSOLE_MESSAGE_ON_OPEN)) {
                main.getLogger().info(p.getName() + " emptied the AngelChest of " + Bukkit.getOfflinePlayer(angelChest.owner).getName() + " at " + angelChest.block.getLocation());
            }
        } else {
            Messages.send(p, main.messages.MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK);

            // This is another player's chest
            if (Daddy.allows(PremiumFeatures.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_ANGELCHEST)) {
                if (!p.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_CHEST)) {
                    final Player tmpPlayer = Bukkit.getPlayer(angelChest.owner);
                    if (tmpPlayer != null) {
                        if (firstOpened) {
                            Messages.send(tmpPlayer, main.messages.MSG_OPENED.replaceAll("\\{player}", p.getName()));
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

    /**
     * Attempts to spawn an AngelChest on player death
     *
     * @param event PlayerDeathEvent
     */
    private void spawnAngelChest(final PlayerDeathEvent event) {

        final Player p = event.getEntity();

        if (main.debug) main.debug("\n");
        LogUtils.debugBanner(new String[]{"PlayerDeathEvent", "Player: " + p.getName(), "Location: " + p.getLocation()});

        if (main.disableDeathEvent) {
            if (main.debug)
                main.debug("PlayerDeathEvent: Doing nothing, AngelChest has been disabled for debug reasons!");
            return;
        }

        //final long startTime = System.nanoTime();

        final boolean isPvpDeath = p.getKiller() != null && p.getKiller() != p;

        // Print out all plugins/listeners that listen to the PlayerDeathEvent
        if (main.debug) {
            for (final RegisteredListener registeredListener : event.getHandlers().getRegisteredListeners()) {
                if (main.debug)
                    main.debug(registeredListener.getPlugin().getName() + ": " + registeredListener.getListener().getClass().getName() + " @ " + registeredListener.getPriority().name());
            }
        }

        if (main.debug) main.debug("PlayerListener -> spawnAngelChest");

        if (!p.hasPermission(Permissions.USE)) {
            if (main.debug) main.debug("Cancelled: no permission (angelchest.use)");
            return;
        }

        if (NBTAPI.hasNBT(p, NBTTags.HAS_ANGELCHEST_DISABLED)) {
            if (main.debug) main.debug("Cancelled: this player disabled AngelChest using /actoggle");
            return;
        }

        if (event.getKeepInventory()) {
            if (!main.getConfig().getBoolean(Config.IGNORE_KEEP_INVENTORY)) {
                if (main.debug) main.debug("Cancelled: event#getKeepInventory() == true");
                if (main.debug) main.debug("Please check if your kept your inventory on death!");
                if (main.debug)
                    main.debug("This is probably because some other plugin tries to handle your inv on death.");
                if (main.debug) main.debug(p.getDisplayName() + " is OP: " + p.isOp());
                return;
            } else {
                if (main.debug)
                    main.debug("event#getKeepInventory() == true but we ignore it because of config settings");
                event.setKeepInventory(false);
            }
        }

        if (!Utils.isWorldEnabled(p.getLocation().getWorld())) {
            if (main.debug) main.debug("Cancelled: world disabled (" + p.getLocation().getWorld());
            return;
        }

        if (Main.getWorldGuardWrapper().isBlacklisted(p.getLocation().getBlock())) {
            if (main.debug) main.debug("Cancelled: region disabled.");
            return;
        }

        if (!Main.getWorldGuardWrapper().getAngelChestFlag(p)) {
            if (main.debug) main.debug("Cancelled: World Guard flag \"allow-angelchest\" is \"deny\"");
            return;
        }

        if (main.getConfig().getBoolean(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD) && !ProtectionUtils.playerMayBuildHere(p, p.getLocation())) {
            if (main.debug) main.debug("Cancelled: BlockPlaceEvent cancelled");
            return;
        }

        if (Daddy.allows(PremiumFeatures.PROHIBIT_CHEST_IN_LAVA_OR_VOID)) {
            if (!main.getConfig().getBoolean(Config.ALLOW_CHEST_IN_LAVA) && p.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA) {
                if (main.debug) main.debug("Cancelled: Lava, allow-chest-in-lava: false");
                return;
            }

            if (!main.getConfig().getBoolean(Config.ALLOW_CHEST_IN_VOID) && p.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (main.debug) main.debug("Cancelled: Void, allow-chest-in-void: false");
                return;
            }
        }

        if (!main.getConfig().getBoolean(Config.ALLOW_ANGELCHEST_IN_PVP)) {
            if (isPvpDeath) {
                if (main.debug)
                    main.debug("Cancelled: allow-angelchest-in-pvp is false and this seemed to be a pvp death");
                if (main.getConfig().getBoolean(Config.DROP_HEADS)) {
                    if (Daddy.allows(PremiumFeatures.DROP_HEADS)) {
                        dropPlayerHead(p);
                    }
                }

                Utils.sendDelayedMessage(p, main.messages.MSG_NO_CHEST_IN_PVP, 1);
                return;
            }
        }

        if (!AngelChestUtils.spawnChance(main.groupUtils.getSpawnChancePerPlayer(p))) {
            if (main.debug) main.debug("Cancelled: unlucky, spawnChance returned false!");
            Utils.sendDelayedMessage(p, main.messages.MSG_SPAWN_CHANCE_UNSUCCESFULL, 1);
            return;
        }


        Block fixedPlayerPosition;

        // Player died below world
        if (p.getLocation().getBlockY() < 1) {
            if (main.debug) main.debug("Fixing player position for " + p.getLocation() + " because Y < 1");
            fixedPlayerPosition = null;
            // Void detection: use last known position
            if (main.getConfig().getBoolean(Config.VOID_DETECTION)) {
                if (main.lastPlayerPositions.containsKey(p.getUniqueId())) {
                    fixedPlayerPosition = main.lastPlayerPositions.get(p.getUniqueId());
                    if (main.debug)
                        main.debug("Using last known player position " + fixedPlayerPosition.getLocation());
                }
            }
            // Void detection disabled or no last known position: set to Y=1
            if (fixedPlayerPosition == null) {
                final Location ltmp = p.getLocation();
                ltmp.setY(1);
                fixedPlayerPosition = ltmp.getBlock();
                if (main.debug)
                    main.debug("Void detection disabled or no last known player position, setting Y to 1 " + fixedPlayerPosition.getLocation());
            }
        } else {
            fixedPlayerPosition = p.getLocation().getBlock();
            if (main.debug) main.debug("Void fixing not needed for " + fixedPlayerPosition.getLocation());
        }

        // Player died above build limit
        // Note: This has to be checked AFTER the "below world" check, because the lastPlayerPositions could return 256
        if (fixedPlayerPosition.getY() >= p.getWorld().getMaxHeight()) {
            if (main.debug)
                main.debug("Fixing player position for " + p.getLocation() + " because Y >= World#getMaxHeight()");
            final Location ltmp = p.getLocation();
            ltmp.setY(p.getWorld().getMaxHeight() - 1);
            fixedPlayerPosition = ltmp.getBlock();
            if (main.debug)
                main.debug("Setting Y to World#getMaxHeight()-1 " + fixedPlayerPosition.getLocation());
        } else {
            //fixedPlayerPosition = p.getLocation().getBlock();
            if (main.debug)
                main.debug("MaxHeight fixing not needed for " + fixedPlayerPosition.getLocation());
        }

        // Player died in Lava
        if (main.getConfig().getBoolean(Config.LAVA_DETECTION) && fixedPlayerPosition.getType() == Material.LAVA) {
            if (main.debug) main.debug("Fixing player position for " + p.getLocation() + " because there's lava");
            if (main.lastPlayerPositions.containsKey(p.getUniqueId())) {
                fixedPlayerPosition = main.lastPlayerPositions.get(p.getUniqueId());
                if (main.debug)
                    main.debug("Using last known player position " + fixedPlayerPosition.getLocation());
            }
        }

        if (main.debug) main.debug("FixedPlayerPosition: " + fixedPlayerPosition);
        Block angelChestBlock = AngelChestUtils.getChestLocation(fixedPlayerPosition);

        // Calling Event
        final AngelChestSpawnPrepareEvent angelChestSpawnPrepareEvent = new AngelChestSpawnPrepareEvent(p, angelChestBlock, p.getLastDamageCause().getCause(), event);
        Bukkit.getPluginManager().callEvent(angelChestSpawnPrepareEvent);
        if (angelChestSpawnPrepareEvent.isCancelled()) {
            if (main.debug) main.debug("AngelChestCreateEvent has been cancelled!");
            return;
        }
        angelChestBlock = angelChestSpawnPrepareEvent.getBlock();

        if (!CommandUtils.hasEnoughMoney(p, main.groupUtils.getSpawnPricePerPlayer(p), main.messages.MSG_NOT_ENOUGH_MONEY_CHEST, "AngelChest spawned")) {
            return;
        }

        // Enable keep inventory to prevent drops (this is not preventing the drops at the moment due to spigot)
        event.setKeepInventory(true);

        // DETECT ALL DROPS, EVEN FRESHLY ADDED
        final ArrayList<ItemStack> freshDrops = new ArrayList<>();
        final ItemStack[] drops = event.getDrops().toArray(new ItemStack[0]);
        final List<ItemStack> inventoryAsList = Arrays.asList(p.getInventory().getContents());

        LogUtils.debugBanner(new String[]{"ADDITIONAL DEATH DROP LIST"});
        if (main.debug) main.debug("The following items are in the drops list, but not in the inventory.");
        for (int i = 0; i < drops.length; i++) {
            if (inventoryAsList.contains(drops[i])) continue;
            if (main.debug) main.debug(String.format("Drop %d: %s", i, drops[i]));
            if (main.debug) main.debug(" ");
            freshDrops.add(drops[i]);
        }
        LogUtils.debugBanner(new String[]{"ADDITIONAL DEATH DROP LIST END"});

        if (main.getConfig().getBoolean(Config.DROP_HEADS) && Daddy.allows(PremiumFeatures.DROP_HEADS)) {
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
                    dropPlayerHead(p);
                } else {
                    freshDrops.add(getPlayerHead(p));
                }
            }
        }

        for (final ItemStack freshDrop : freshDrops) {
            for (final ItemStack leftover : p.getInventory().addItem(freshDrop).values()) {
                if (leftover == null || leftover.getAmount() == 0 || leftover.getType() == Material.AIR) continue;
                p.getWorld().dropItemNaturally(p.getLocation(), leftover);
                main.getLogger().info("Could not add item to already full AngelChest of player " + p.getName() + ": " + leftover + ", dropping it to world @ " + p.getLocation());
            }
        }
        // END DETECT ALL DROPS

        /*
        Creating the chest
         */
        final DeathCause deathCause = new DeathCause(p.getLastDamageCause());
        final AngelChest ac = new AngelChest(p, angelChestBlock, main.logger.getLogFileName(event), deathCause);
        main.angelChests.put(angelChestBlock, ac);


        /*
        Experience
         */
        //noinspection StatementWithEmptyBody
        if (Daddy.allows(PremiumFeatures.DISALLOW_XP_COLLECTION) && main.getConfig().getString(Config.COLLECT_XP).equalsIgnoreCase("false")) {
            // Do nothing
        } else //noinspection StatementWithEmptyBody
            if (Daddy.allows(PremiumFeatures.DISALLOW_XP_COLLECTION_IN_PVP) && main.getConfig().getString(Config.COLLECT_XP).equalsIgnoreCase("nopvp") && (p.getKiller() != null && p.getKiller() != p)) {
                // Do nothing
            } else if (!event.getKeepLevel() && event.getDroppedExp() != 0) {
                final double xpPercentage = main.groupUtils.getXPPercentagePerPlayer(p);
                if (main.debug) main.debug("Player has xpPercentage of " + xpPercentage);
                if (xpPercentage == -1 || !Daddy.allows(PremiumFeatures.PERCENTAL_XP_LOSS)) {
                    ac.experience = event.getDroppedExp();
                } else {
                    final float currentXP = XPUtils.getTotalXPRequiredForLevel(p.getLevel());
                    if (main.debug) main.debug("currentXP = " + currentXP + " (for this level)");
                    if (main.debug) main.debug("p.getEXP = " + p.getExp());
                    final double remainingXP = p.getExp() * XPUtils.getXPRequiredForNextLevel(p.getLevel());
                    if (main.debug) main.debug("Remaining XP = " + remainingXP);
                    final double totalXP = currentXP + remainingXP;
                    if (main.debug) main.debug("Total XP = " + totalXP);
                    final double adjustedXP = totalXP * xpPercentage;
                    if (main.debug) main.debug("adjustedXP = " + adjustedXP);
                    ac.experience = (int) adjustedXP;
                }
                event.setDroppedExp(0);
            }

        /*
        Check if player has any drops
         */
        if (ac.isEmpty()) {
            if (main.debug) main.debug("Cancelled: AngelChest would be empty.");
            if (main.debug) main.debug("Either your inventory and XP was empty, or another plugin set your");
            if (main.debug) main.debug("drops and XP to zero.");

            ac.remove();
            ac.destroy(true);
            main.angelChests.remove(angelChestBlock);

            Utils.sendDelayedMessage(p, main.messages.MSG_INVENTORY_WAS_EMPTY, 1);
            return;
        }

        //ac.createChest(ac.block, ac.owner);

        main.logger.logDeath(event, ac);

        /*
        Clearing inventory
         */
        // Delete players inventory except excluded items
        clearInventory(p.getInventory());

        // Clear the drops except blacklisted items
        event.getDrops().removeIf(drop -> !ac.blacklistedItems.contains(drop));

        // send message after one twentieth second
        Utils.sendDelayedMessage(p, main.messages.MSG_ANGELCHEST_CREATED, 1);


        if (main.getConfig().getBoolean(Config.SHOW_LOCATION)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> CommandUtils.sendListOfAngelChests(main, p, p), 2);
        }

        final int maxChests = main.groupUtils.getChestsPerPlayer(p);
        final ArrayList<AngelChest> chests = AngelChestUtils.getAllAngelChestsFromPlayer(p);
        //System.out.println(chests.size()+" chests.size");
        if (chests.size() > maxChests) {
            chests.get(0).destroy(true);
            chests.get(0).remove();
            Bukkit.getScheduler().runTaskLater(main, () -> {
                Messages.send(p, " ");
                Messages.send(p, main.messages.MSG_ANGELCHEST_EXPLODED);
            }, 3L);

        }

        if (Daddy.allows(PremiumFeatures.DONT_PROTECT_ANGELCHESTS_IN_PVP)) {
            if (main.getConfig().getBoolean(Config.DONT_PROTECT_CHEST_IF_PLAYER_DIED_IN_PVP)) {
                if (p.getKiller() != null && p.getKiller() != p) {
                    ac.isProtected = false;
                }
            }
        }

        //Utils.reloadAngelChest(ac,plugin);

        @SuppressWarnings("RedundantCast") final AngelChestSpawnEvent angelChestSpawnEvent = new AngelChestSpawnEvent(
                /* DO NOT REMOVE THE CAST!                      */
                /* It would result in a MethodNotFoundException */
                (de.jeff_media.angelchest.AngelChest) ac);
        Bukkit.getPluginManager().callEvent(angelChestSpawnEvent);

        /*long durationNano = System.nanoTime() - startTime;
        long durationMilli = TimeUtils.nanoSecondsToMilliSeconds(durationNano);
        double tickPercentage = TimeUtils.milliSecondsToTickPercentage(durationMilli);
        if(main.debug) main.debug("AngelChest creation took " +durationNano + " ns or " + durationMilli +" ms or "+tickPercentage+" % of tick.");*/

        LogUtils.debugBanner(new String[]{"PlayerDeathEvent END"});
        if (main.debug) main.debug(" ");

        if (Daddy.allows(PremiumFeatures.PLAY_TOTEM_ANIMATION) && main.getConfig().getBoolean(Config.PLAY_TOTEM_ANIMATION)) {
            NMSHandler.playTotemAnimation(p, main.getConfig().getInt(Config.TOTEM_CUSTOM_MODEL_DATA));
        }

        ac.createChest(ac.block, ac.owner);
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGH)
    public void spawnAngelChestHigh(final PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.HIGH) {
            if (main.debug) main.debug("PlayerDeathEvent Priority HIGH");
            spawnAngelChest(event);
        }
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
