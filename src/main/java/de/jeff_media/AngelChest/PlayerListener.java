package de.jeff_media.AngelChest;

import de.jeff_media.AngelChest.hooks.PlayerHeadDropsHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerListener implements Listener {

	final Main main;

	PlayerListener(Main main) {
		this.main = main;

		main.debug("PlayerListener created");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		main.registerPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		main.unregisterPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void spawnAngelChestMonitor(PlayerDeathEvent event) {
		if(Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.MONITOR) {
			main.debug("PlayerDeathEvent Priority MONITOR");
			spawnAngelChest(event);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void spawnAngelChestHighest(PlayerDeathEvent event) {
		if(Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.HIGHEST) {
			main.debug("PlayerDeathEvent Priority HIGHEST");
			spawnAngelChest(event);
		}
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void spawnAngelChestHigh(PlayerDeathEvent event) {
		if(Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.HIGH) {
			main.debug("PlayerDeathEvent Priority HIGH");
			spawnAngelChest(event);
		}
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void spawnAngelChestNormal(PlayerDeathEvent event) {
		if(Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.NORMAL) {
			main.debug("PlayerDeathEvent Priority NORMAL");
			spawnAngelChest(event);
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void spawnAngelChestLow(PlayerDeathEvent event) {
		if(Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.LOW) {
			main.debug("PlayerDeathEvent Priority LOW");
			spawnAngelChest(event);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void spawnAngelChestLowest(PlayerDeathEvent event) {
		if(Utils.getEventPriority(main.getConfig().getString(Config.EVENT_PRIORITY)) == EventPriority.LOWEST) {
			main.debug("PlayerDeathEvent Priority LOWEST");
			spawnAngelChest(event);
		}
	}



	private void spawnAngelChest(PlayerDeathEvent event) {
		if(main.debug) {
			for (RegisteredListener registeredListener : event.getHandlers().getRegisteredListeners()) {
				main.debug(registeredListener.getPlugin().getName()+": "+registeredListener.getListener().getClass().getName() + " @ "+registeredListener.getPriority().name());
			}
		}


		Objects.requireNonNull(main.chestMaterial,"Chest Material is null!");

		/*System.out.println("test");
		if(plugin.chestMaterial==null) {
			System.out.println("chestmat is null");
			return;
		}

		plugin.debug(plugin.chestMaterial.name());*/

		main.debug("PlayerListener -> spawnAngelChest");
		Player p = event.getEntity();
		if (!p.hasPermission("angelchest.use")) {
			main.debug("Cancelled: no permission (angelchest.use)");
			return;
		}

		if (event.getKeepInventory()) {
			if(!main.getConfig().getBoolean("ignore-keep-inventory", false)) {
				main.debug("Cancelled: event#getKeepInventory() == true");
				main.debug("Please check if your kept your inventory on death!");
				main.debug("This is probably because some other plugin tries to handle your inv on death.");
				main.debug(event.getEntity().getDisplayName()+" is OP: "+event.getEntity().isOp());
				return;
			} else {
				main.debug("event#getKeepInventory() == true but we ignore it because of config settings");
				event.setKeepInventory(false);
			}
		}

		if(!Utils.isWorldEnabled(p.getLocation().getWorld(), main)) {
			main.debug("Cancelled: world disabled ("+p.getLocation().getWorld());
			return;
		}

		if(main.worldGuardHandler.isBlacklisted(p.getLocation().getBlock())) {
			main.debug("Cancelled: region disabled.");
			return;
		}

		if(main.getConfig().getBoolean(Config.ONLY_SPAWN_CHESTS_IF_PLAYER_MAY_BUILD)
				&& !ProtectionUtils.playerMayBuildHere(p,p.getLocation(), main)) {
			main.debug("Cancelled: BlockPlaceEvent cancelled");
			return;
		}

		// Don't do anything if player's inventory is empty anyway
		if (event.getDrops() == null || event.getDrops().size() == 0) {
			main.debug("Cancelled: event#getDrops == null || event#getDrops#size == 0");
			main.debug("Either your inventory was empty, or another plugin set your");
			main.debug("drops to zero.");
			Utils.sendDelayedMessage(p, main.messages.MSG_INVENTORY_WAS_EMPTY, 1, main);
			return;
		}

		if(!main.getConfig().getBoolean(Config.ALLOW_ANGELCHEST_IN_PVP)) {
			if(event.getEntity().getKiller()!=null && event.getEntity().getKiller() != event.getEntity()) {
				main.debug("Cancelled: allow-angelchest-in-pvp is false and this seemed to be a pvp death");

				Utils.sendDelayedMessage(p, main.messages.MSG_NO_CHEST_IN_PVP,1, main);
				return;
			}
		}

		if(!AngelChestCommandUtils.hasEnoughMoney(event.getEntity(), main.getConfig().getDouble(Config.PRICE), main, main.messages.MSG_NOT_ENOUGH_MONEY_CHEST,"AngelChest spawned")) {
			return;
		}

		// Enable keep inventory to prevent drops (this is not preventing the drops at the moment due to spigot)
		event.setKeepInventory(true);

		Block tmpPosition;

		main.debug("Debug 1");

		if(p.getLocation().getBlockY() < 1) {
			tmpPosition = null;
			if(main.getConfig().getBoolean(Config.VOID_DETECTION)) {
				if(main.lastPlayerPositions.containsKey(p.getUniqueId())) {
					tmpPosition = main.lastPlayerPositions.get(p.getUniqueId());
				}
			}
			if(tmpPosition == null) {
				Location ltmp = p.getLocation();
				ltmp.setY(1);
				tmpPosition = ltmp.getBlock();
			}
		} else {
			tmpPosition = p.getLocation().getBlock();
		}

		Block angelChestBlock = Utils.findSafeBlock(tmpPosition, main);
		main.debug("Debug 2");

		/*if(plugin.debug) {
			for(ItemStack item : event.getDrops()) {
				if(item==null) continue;
				plugin.debug("Found drop "+item.toString());
			}
		}*/

		if(main.getConfig().getBoolean(Config.DETECT_PLAYER_HEAD_DROPS)) {
			PlayerHeadDropsHook.applyPlayerHeadDrops(p.getInventory(), event.getDrops(), main);
		}

		AngelChest ac =new AngelChest(p,p.getUniqueId(), angelChestBlock, p.getInventory(), main);
		main.angelChests.put(angelChestBlock,ac);
		main.debug("Debug 3");

		if(!event.getKeepLevel() && event.getDroppedExp()!=0 && p.hasPermission("angelchest.xp")) {
			if(p.hasPermission("angelchest.xp.levels")) {
				ac.levels = p.getLevel();
			}
			ac.experience=event.getDroppedExp();
			event.setDroppedExp(0);
		}

		// Delete players inventory except excluded items
		clearInventory(p.getInventory());

		// Clear the drops
		event.getDrops().clear();

		// send message after one twentieth second
		Utils.sendDelayedMessage(p, main.messages.MSG_ANGELCHEST_CREATED, 1, main);


		if(main.getConfig().getBoolean(Config.SHOW_LOCATION)) {
			//Utils.sendDelayedMessage(p, String.format(plugin.messages.MSG_ANGELCHEST_LOCATION , Utils.locationToString(fixedAngelChestBlock) ), 2, plugin);
			/*final int x = fixedAngelChestBlock.getX();
			final int y = fixedAngelChestBlock.getY();
			final int z = fixedAngelChestBlock.getZ();
			final String world = fixedAngelChestBlock.getWorld().getName();
			String locString = Utils.locationToString(fixedAngelChestBlock);*/
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
				//TpLinkUtil.sendLink(p, String.format(plugin.messages.MSG_ANGELCHEST_LOCATION , locString )+" ", "/acinfo tp "+x+" "+y+" "+z+" "+world);
				try {
					AngelChestCommandUtils.sendListOfAngelChests(main, p, p);
				} catch(Throwable throwable) {
					//e.printStackTrace();
				}
			},2);
		}

		int maxChests = main.groupUtils.getChestsPerPlayer(p);
		ArrayList<AngelChest> chests = Utils.getAllAngelChestsFromPlayer(p, main);
		//System.out.println(chests.size()+" chests.size");
		if(chests.size()>maxChests) {
			chests.get(0).destroy(true);
			chests.get(0).remove();
			Bukkit.getScheduler().runTaskLater(main,() -> {
				p.sendMessage(" ");
				p.sendMessage(main.messages.MSG_ANGELCHEST_EXPLODED);
			},3L);

		}

		//Utils.reloadAngelChest(ac,plugin);
	}

	private void clearInventory(Inventory inv) {
		for(int i = 0; i < inv.getSize(); i++) {
			if(main.hookUtils.keepOnDeath(inv.getItem(i))) {
				continue;
			}
			inv.setItem(i,null);
		}

	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(!main.getConfig().getBoolean(Config.AUTO_RESPAWN)) return;
		int delay = main.getConfig().getInt(Config.AUTO_RESPAWN_DELAY);

		Bukkit.getScheduler().runTaskLater(main,() -> {
			if(e.getEntity().isDead()) {
				e.getEntity().spigot().respawn();
			}
		},1L+(delay*20));
	}

	@EventHandler
	public void onDeathBecauseTotemNotEquipped(EntityResurrectEvent e) {
		if(!(e.getEntity() instanceof Player)) return;

		if(!main.getConfig().getBoolean(Config.TOTEM_OF_UNDYING_WORKS_EVERYWHERE)) return;

		Player p = (Player) e.getEntity();


		for(ItemStack is : p.getInventory()) {
			if(is==null) continue;
			if(is.getType().name().equals("TOTEM_OF_UNDYING") || is.getType().name().equals("TOTEM")) {
				e.setCancelled(false);
				is.setAmount(is.getAmount()-1);
				return;
			}
		}

	}

	/* Debug
	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent e) {

		//Player p = e.getPlayer();
		//System.out.println("Respawn");
		for(ItemStack itemStack : p.getInventory()) {
			if(itemStack==null) continue;
			System.out.println(itemStack.getType().name());
		}

	}*/

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
		// p.openInventory(angelChest.inv);
		openAngelChest(p, block, angelChest);

		event.setCancelled(true);
	}

	void openAngelChest(Player p, Block block, AngelChest angelChest) {

		if(p.hasPermission("angelchest.xp.levels") && angelChest.levels!=0 && angelChest.levels> p.getLevel()) {
			p.setExp(0);
			p.setLevel(angelChest.levels);
			angelChest.levels = 0;
			angelChest.experience = 0;
		}
		else if((p.hasPermission("angelchest.xp") || p.hasPermission("angelchest.xp.levels")) && angelChest.experience!=0) {
			p.giveExp(angelChest.experience);
			angelChest.levels = 0;
			angelChest.experience=0;
		}




		boolean succesfullyStoredEverything;
		boolean isOwnChest = angelChest.owner == p.getUniqueId();

		succesfullyStoredEverything = Utils.tryToMergeInventories(angelChest, p.getInventory());
		if (succesfullyStoredEverything) {
			p.sendMessage(main.messages.MSG_YOU_GOT_YOUR_INVENTORY_BACK);
			angelChest.destroy(false);
			angelChest.remove();
			if(main.getConfig().getBoolean(Config.CONSOLE_MESSAGE_ON_OPEN)) {
				main.getLogger().info(p.getName()+" emptied the AngelChest of "+Bukkit.getOfflinePlayer(angelChest.owner).getName()+" at "+angelChest.block.getLocation());
			}
		} else {
			p.sendMessage(main.messages.MSG_YOU_GOT_PART_OF_YOUR_INVENTORY_BACK);
			p.openInventory(angelChest.overflowInv);
			main.getLogger().info(p.getName()+" opened the AngelChest of "+Bukkit.getOfflinePlayer(angelChest.owner).getName()+" at "+angelChest.block.getLocation());
		}
	}

	@EventHandler
	public void onAngelChestClose(InventoryCloseEvent event) {

		for (AngelChest angelChest : main.angelChests.values()) {
			if (!angelChest.overflowInv.equals(event.getInventory())) {
				continue;
			}

			Inventory inv = event.getInventory();
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
    public void onArmorStandRightClick(PlayerInteractAtEntityEvent event)
    {
    	if(event.getRightClicked()==null) {
    		return;
    	}
        if (!event.getRightClicked().getType().equals(EntityType.ARMOR_STAND))
        {

            return;
        }
        AtomicReference<AngelChest> as  = new AtomicReference<>();
        if(main.isAngelChestHologram(event.getRightClicked())) {
			as.set(main.getAngelChestByHologram((ArmorStand) event.getRightClicked()));
			//System.out.println("GETBYHOLOGRAM1");
        }

        else {
        	main.blockArmorStandCombinations.forEach((combination -> {
        		if(event.getRightClicked().getUniqueId().equals(combination.armorStand.getUniqueId())) {
        			as.set(main.getAngelChest(combination.block));
					//System.out.println("GETBYHOLOGRAM2");
				}
			}));

		}

        if(as.get()==null) return;

		if (!as.get().owner.equals(event.getPlayer().getUniqueId())
				&& !event.getPlayer().hasPermission("angelchest.protect.ignore") && as.get().isProtected) {
			event.getPlayer().sendMessage(main.messages.MSG_NOT_ALLOWED_TO_BREAK_OTHER_ANGELCHESTS);
			event.setCancelled(true);
			return;
		}
		openAngelChest(event.getPlayer(), as.get().block, as.get());
    }

}
