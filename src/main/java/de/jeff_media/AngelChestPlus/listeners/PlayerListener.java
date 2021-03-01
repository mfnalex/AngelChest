package de.jeff_media.AngelChestPlus.listeners;

import de.jeff_media.AngelChestPlus.*;
import de.jeff_media.AngelChestPlus.config.Config;
import de.jeff_media.AngelChestPlus.config.Permissions;
import de.jeff_media.AngelChestPlus.data.AngelChest;
import de.jeff_media.AngelChestPlus.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles player related events
 */
public class PlayerListener implements Listener {

    final Main main;

    public PlayerListener() {
        this.main = Main.getInstance();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void spawnAngelChestMonitor(PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.MONITOR) {
            main.debug("PlayerDeathEvent Priority MONITOR");
            spawnAngelChest(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void spawnAngelChestHighest(PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.HIGHEST) {
            main.debug("PlayerDeathEvent Priority HIGHEST");
            spawnAngelChest(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void spawnAngelChestHigh(PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.HIGH) {
            main.debug("PlayerDeathEvent Priority HIGH");
            spawnAngelChest(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void spawnAngelChestNormal(PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.NORMAL) {
            main.debug("PlayerDeathEvent Priority NORMAL");
            spawnAngelChest(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void spawnAngelChestLow(PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.LOW) {
            main.debug("PlayerDeathEvent Priority LOW");
            spawnAngelChest(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void spawnAngelChestLowest(PlayerDeathEvent event) {
        if (Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.LOWEST) {
            main.debug("PlayerDeathEvent Priority LOWEST");
            spawnAngelChest(event);
        }
    }

    /**
     * Attempts to spawn an AngelChest on player death
     * @param event PlayerDeathEvent
     */
    private void spawnAngelChest(PlayerDeathEvent event) {

        // Print out all plugins/listeners that listen to the PlayerDeathEvent
        if (main.debug) {
            for (RegisteredListener registeredListener : event.getHandlers().getRegisteredListeners()) {
                main.debug(registeredListener.getPlugin().getName() + ": " + registeredListener.getListener().getClass().getName() + " @ " + registeredListener.getPriority().name());
            }
        }

        main.debug("PlayerListener -> spawnAngelChest");
        Player p = event.getEntity();
        if (!p.hasPermission("angelchest.use")) {
            main.debug("Cancelled: no permission (angelchest.use)");
            return;
        }

        // TODO: Readd this to the config file maybe?
        if (event.getKeepInventory()) {
            if (!main.getConfig().getBoolean("ignore-keep-inventory", false)) {
                main.debug("Cancelled: event#getKeepInventory() == true");
                main.debug("Please check if your kept your inventory on death!");
                main.debug("This is probably because some other plugin tries to handle your inv on death.");
                main.debug(event.getEntity().getDisplayName() + " is OP: " + event.getEntity().isOp());
                return;
            } else {
                main.debug("event#getKeepInventory() == true but we ignore it because of config settings");
                event.setKeepInventory(false);
            }
        }

        if (!Utils.isWorldEnabled(p.getLocation().getWorld())) {
            main.debug("Cancelled: world disabled (" + p.getLocation().getWorld());
            return;
        }

        if (main.worldGuardHandler.isBlacklisted(p.getLocation().getBlock())) {
            main.debug("Cancelled: region disabled.");
            return;
        }

        if (main.getConfig().getBoolean(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD)
                && !ProtectionUtils.playerMayBuildHere(p, p.getLocation())) {
            main.debug("Cancelled: BlockPlaceEvent cancelled");
            return;
        }

        if(!main.getConfig().getBoolean(Config.ALLOW_CHEST_IN_LAVA)
                && p.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LAVA) {
            main.debug("Cancelled: Lava, allow-chest-in-lava: false");
            return;
        }

        if(!main.getConfig().getBoolean(Config.ALLOW_CHEST_IN_VOID)
                && p.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) {
            main.debug("Cancelled: Void, allow-chest-in-void: false");
            return;
        }

        // Don't do anything if player's inventory is empty anyway
        // TODO: Player could die while having no items, but XP
        if (event.getDrops() == null || event.getDrops().size() == 0) {
            main.debug("Cancelled: event#getDrops == null || event#getDrops#size == 0");
            main.debug("Either your inventory was empty, or another plugin set your");
            main.debug("drops to zero.");
            Utils.sendDelayedMessage(p, main.messages.MSG_INVENTORY_WAS_EMPTY, 1);
            return;
        }

        if (!main.getConfig().getBoolean(Config.ALLOW_ANGELCHEST_IN_PVP)) {
            if (event.getEntity().getKiller() != null && event.getEntity().getKiller() != event.getEntity()) {
                main.debug("Cancelled: allow-angelchest-in-pvp is false and this seemed to be a pvp death");

                Utils.sendDelayedMessage(p, main.messages.MSG_NO_CHEST_IN_PVP, 1);
                return;
            }
        }

        if (!CommandUtils.hasEnoughMoney(event.getEntity(), main.getConfig().getDouble(Config.PRICE), main.messages.MSG_NOT_ENOUGH_MONEY_CHEST, "AngelChest spawned")) {
            return;
        }

        // Enable keep inventory to prevent drops (this is not preventing the drops at the moment due to spigot)
        event.setKeepInventory(true);

        Block tmpPosition;

        if (p.getLocation().getBlockY() < 1) {
            tmpPosition = null;
            if (main.getConfig().getBoolean(Config.VOID_DETECTION)) {
                if (main.lastPlayerPositions.containsKey(p.getUniqueId())) {
                    tmpPosition = main.lastPlayerPositions.get(p.getUniqueId());
                }
            }
            if (tmpPosition == null) {
                Location ltmp = p.getLocation();
                ltmp.setY(1);
                tmpPosition = ltmp.getBlock();
            }
        } else {
            tmpPosition = p.getLocation().getBlock();
        }

        Block angelChestBlock = Utils.findSafeBlock(tmpPosition);

        // DETECT ALL DROPS, EVEN FRESHLY ADDED
        ArrayList<ItemStack> freshDrops = new ArrayList<>();
        ItemStack[] drops = event.getDrops().toArray(new ItemStack[0]);
        List<ItemStack> inventoryAsList = Arrays.asList(p.getInventory().getContents());
        main.debug("===== ADDITIONAL DEATH DROP LIST =====");
        main.debug("The following items are in the drops list, but not in the inventory.");
        for (int i = 0; i < drops.length; i++) {
            if (inventoryAsList.contains(drops[i])) continue;
            main.debug(String.format("Drop %d: %s", i, drops[i]));
            freshDrops.add(drops[i]);
        }
        main.debug("===== ADDITIONAL DEATH DROP LIST END =====");
        for (ItemStack freshDrop : freshDrops) {
            for (ItemStack leftover : p.getInventory().addItem(freshDrop).values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), leftover);
                main.debug("Could not add item to AngelChest of player " + p.getName() + ": " + leftover + ", dropping it to world @ " + p.getLocation().toString());
            }
        }
		// END DETECT ALL DROPS

        AngelChest ac = new AngelChest(p, p.getUniqueId(), angelChestBlock, p.getInventory(), main.logger.getLogFileName(event));
        main.angelChests.put(angelChestBlock, ac);

        if (!event.getKeepLevel() && event.getDroppedExp() != 0) {
            double xpPercentage = main.groupUtils.getXPPercentagePerPlayer(p);
            main.debug("Player has xpPercentage of " + xpPercentage);
            if (xpPercentage == -1) {
                ac.experience = event.getDroppedExp();
            } else {
                float currentXP = XPUtils.getTotalXPRequiredForLevel(p.getLevel());
                main.debug("currentXP = " + currentXP + " (for this level)");
                main.debug("p.getEXP = " + p.getExp());
                double remainingXP = p.getExp() * XPUtils.getXPRequiredForNextLevel(p.getLevel());
                main.debug("Remaining XP = " + remainingXP);
                double totalXP = currentXP + remainingXP;
                main.debug("Total XP = " + totalXP);
                double adjustedXP = totalXP * xpPercentage;
                main.debug("adjustedXP = " + adjustedXP);
                ac.experience = (int) adjustedXP;
            }
            event.setDroppedExp(0);
        }

        main.logger.logDeath(event,ac);

        // Delete players inventory except excluded items
        clearInventory(p.getInventory());

        // Clear the drops
        event.getDrops().clear();

        // send message after one twentieth second
        Utils.sendDelayedMessage(p, main.messages.MSG_ANGELCHEST_CREATED, 1);


        if (main.getConfig().getBoolean(Config.SHOW_LOCATION)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> CommandUtils.sendListOfAngelChests(main, p, p), 2);
        }

        int maxChests = main.groupUtils.getChestsPerPlayer(p);
        ArrayList<AngelChest> chests = Utils.getAllAngelChestsFromPlayer(p);
        //System.out.println(chests.size()+" chests.size");
        if (chests.size() > maxChests) {
            chests.get(0).destroy(true);
            chests.get(0).remove();
            Bukkit.getScheduler().runTaskLater(main, () -> {
                p.sendMessage(" ");
                p.sendMessage(main.messages.MSG_ANGELCHEST_EXPLODED);
            }, 3L);

        }

        if(main.getConfig().getBoolean(Config.DONT_PROTECT_CHEST_IF_PLAYER_DIED_IN_PVP)) {
            if (event.getEntity().getKiller() != null && event.getEntity().getKiller() != event.getEntity()) {
                ac.isProtected = false;
            }
        }

        //Utils.reloadAngelChest(ac,plugin);
    }

    /**
     * Remove all items from inventory that should not be kept on death
     * @param inv inventory
     */
    private void clearInventory(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (main.hookUtils.keepOnDeath(inv.getItem(i))) {
                continue;
            }
            inv.setItem(i, null);
        }

    }

    /**
     * Handles auto-respawning the player
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!main.getConfig().getBoolean(Config.AUTO_RESPAWN)) return;
        int delay = main.getConfig().getInt(Config.AUTO_RESPAWN_DELAY);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (event.getEntity().isDead()) {
                event.getEntity().spigot().respawn();
            }
        }, 1L + (delay * 20));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeathBecauseTotemNotEquipped(EntityResurrectEvent e) {
        main.debug("EntityResurrectEvent");
        if (!(e.getEntity() instanceof Player)) return;

        if(!e.isCancelled()) {
            main.debug("  R: Not cancelled");
            return;
        }

        if (!main.getConfig().getBoolean(Config.TOTEM_OF_UNDYING_WORKS_EVERYWHERE)) {
            main.debug("  R: Config option disabled");
            return;
        }

        Player p = (Player) e.getEntity();


        for (ItemStack is : p.getInventory()) {
            if (is == null) continue;
            if (is.getType().name().equals("TOTEM_OF_UNDYING") || is.getType().name().equals("TOTEM")) {
                e.setCancelled(false);
                is.setAmount(is.getAmount() - 1);
                ItemStack offHand = p.getInventory().getItemInOffHand();
                if(offHand != null && offHand.getAmount()!=0 && offHand.getType()!= Material.AIR) {
                    final ItemStack finalOffHand = offHand.clone();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
                        p.getInventory().setItemInOffHand(finalOffHand);
                    },1L);
                }
                return;
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
        main.debug("Player Respawn: Show GUI to player?");
        Player player = playerRespawnEvent.getPlayer();
        if (!player.hasPermission(Permissions.ALLOW_USE)) {
            main.debug("  No: no angelchest.use permission");
            return;
        }
        ArrayList<AngelChest> chests = Utils.getAllAngelChestsFromPlayer(player);
        if (chests.size() == 0) {
            main.debug("  No: no AngelChests");
            return;
        }
        String showGUIAfterDeath = main.getConfig().getString(Config.SHOW_GUI_AFTER_DEATH).toLowerCase();

        if (main.getConfig().getBoolean(Config.ONLY_SHOW_GUI_AFTER_DEATH_IF_PLAYER_CAN_TP_OR_FETCH)) {
            main.debug(" Checking if player has fetch or tp permission...");
            if (!player.hasPermission(Permissions.ALLOW_FETCH) && !player.hasPermission(Permissions.ALLOW_TELEPORT)) {
                main.debug("  No: Neither angelchest.fetch nor angelchest.tp permission");
                return;
            }
            main.debug(" At least one of those permissions is given.");
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            switch (showGUIAfterDeath) {
                case "false":
                    main.debug("  No: showGUIAfterDeath is false");
                    return;
                case "latest":
                    main.debug("  Yes: show latest chest");
                    main.guiManager.showLatestChestGUI(player);
                    break;
                case "true":
                    main.debug("  Yes: show all chests or latest if there is only one");
                    main.guiManager.showMainGUI(player);
                    break;
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAngelChestRightClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        if (event.getClickedBlock() == null)
            return;
        Block block = event.getClickedBlock();
        if (!main.isAngelChest(block))
            return;
        AngelChest angelChest = main.angelChests.get(block);
        // event.getPlayer().sendMessage("This is " + angelChest.owner.getName()+"'s
        // AngelChest.");
        // Test here if player is allowed to open THIS angelchest
        if (angelChest.isProtected && !event.getPlayer().getUniqueId().equals(angelChest.owner)
                && !event.getPlayer().hasPermission("angelchest.protect.ignore")) {
            event.getPlayer().sendMessage(main.messages.MSG_NOT_ALLOWED_TO_OPEN_OTHER_ANGELCHESTS);
            event.setCancelled(true);
            return;
        }

        if(!angelChest.hasPaidForOpening(p)) {
            return;
        }

        if (p.isSneaking()) {
            main.guiManager.showPreviewGUI(p, angelChest, false);
        } else {
            openAngelChest(p, block, angelChest);
        }

        event.setCancelled(true);
    }

    void openAngelChest(Player p, Block block, AngelChest angelChest) {

        Utils.applyXp(p, angelChest);

        boolean succesfullyStoredEverything;
        //boolean isOwnChest = angelChest.owner == p.getUniqueId();

        succesfullyStoredEverything = Utils.tryToMergeInventories(main, angelChest, p.getInventory());
        if (succesfullyStoredEverything) {
            p.sendMessage(main.messages.MSG_YOU_GOT_YOUR_INVENTORY_BACK);
            angelChest.destroy(false);
            angelChest.remove();
            if (main.getConfig().getBoolean(Config.CONSOLE_MESSAGE_ON_OPEN)) {
                main.getLogger().info(p.getName() + " emptied the AngelChest of " + Bukkit.getOfflinePlayer(angelChest.owner).getName() + " at " + angelChest.block.getLocation());
            }
        } else {
            p.sendMessage(main.messages.MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK);
            //p.openInventory(angelChest.overflowInv);
            main.guiManager.showPreviewGUI(p, angelChest, false);
            main.getLogger().info(p.getName() + " opened the AngelChest of " + Bukkit.getOfflinePlayer(angelChest.owner).getName() + " at " + angelChest.block.getLocation());
        }
    }

    @EventHandler
    public void onAngelChestClose(InventoryCloseEvent event) {

        for (AngelChest angelChest : main.angelChests.values()) {
            if (!angelChest.overflowInv.equals(event.getInventory())) {
                continue;
            }

            //Inventory inv = event.getInventory();
            if (Utils.isEmpty(angelChest.overflowInv)
                    && Utils.isEmpty(angelChest.armorInv)
                    && Utils.isEmpty(angelChest.extraInv)
                    && Utils.isEmpty(angelChest.storageInv)) {
                // plugin.angelChests.remove(Utils.getKeyByValue(plugin.angelChests,
                // angelChest));
                angelChest.destroy(false);

                main.debug("Inventory empty, removing chest");
                // event.getPlayer().sendMessage("You have emptied an AngelChest. It is now
                // gone.");
            }

            return;
        }
    }

    @EventHandler
    public void onArmorStandRightClick(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() == null) {
            return;
        }
        if (!event.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {

            return;
        }
        AtomicReference<AngelChest> as = new AtomicReference<>();
        if (main.isAngelChestHologram(event.getRightClicked())) {
            as.set(main.getAngelChestByHologram((ArmorStand) event.getRightClicked()));
            //System.out.println("GETBYHOLOGRAM1");
        }

        if (as.get() == null) return;

        if (!as.get().owner.equals(event.getPlayer().getUniqueId())
                && !event.getPlayer().hasPermission("angelchest.protect.ignore") && as.get().isProtected) {
            event.getPlayer().sendMessage(main.messages.MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS);
            event.setCancelled(true);
            return;
        }
        if(event.getPlayer().isSneaking()) {
            main.guiManager.showPreviewGUI(event.getPlayer(), as.get(), false);
        } else {
            openAngelChest(event.getPlayer(), as.get().block, as.get());
        }
    }

}
