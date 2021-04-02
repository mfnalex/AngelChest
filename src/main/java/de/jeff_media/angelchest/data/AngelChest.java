package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.ChestYaml;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.enums.EconomyStatus;
import de.jeff_media.angelchest.enums.Features;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents an AngelChest including its content and all other relevant information
 */
public final class AngelChest implements de.jeff_media.angelchest.AngelChest {

    public ItemStack[] armorInv;
    public ItemStack[] storageInv;
    public ItemStack[] extraInv;
    public Inventory overflowInv;
    public boolean success = true;
    public Block block;
    public UUID worldid;
    public UUID owner;
    public Hologram hologram;
    public boolean isProtected;
    public int secondsLeft;
    public int unlockIn;
    public int experience = 0;
    public int levels = 0;
    public boolean infinite = false;
    public Main main;
    public String logfile;
    public List<String> openedBy;
    public DeathCause deathCause;
    public List<ItemStack> blacklistedItems;
    public Set<ItemStack> randomlyLostItems = null;
    public long created;

    double price = 0;

    public void scheduleBlockChange() {
        scheduleBlockChange(true);
    }

    public void scheduleBlockChange(boolean firstTry) {
        if(firstTry) {
            main.debug("scheduleBlockChange: "+block.toString());
        }
        if(main.chestMaterial == main.chestMaterialUnlocked) {
            main.debug("scheduleBlockChange abort: matching materials");
            return;
        }
        int x = block.getX();
        int z = block.getZ();

        if(!block.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
            if(firstTry) {
                main.debug("Tried to change block for chest in unloaded chunk because of unlocking, will do so once chunk is loaded.");
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> scheduleBlockChange(false), 1L);
        } else {
            main.debug("Changed block for chest because of unlocking.");
            createChest(block,owner,false);
        }

    }

    /**
     * Loads an AngelChest from a YAML file
     * @param file File containing the AngelChest data
     */
    public @Nullable AngelChest(File file) {
        main = Main.getInstance();
        main.debug("Creating AngelChest from file " + file.getName());
        YamlConfiguration yaml;
        try {
            yaml = new YamlConfiguration();
            yaml.load(file);
        } catch (Throwable t) {
            main.getLogger().warning("Could not load AngelChest file " + file.getName());
            success = false;
            if(main.debug) {
                t.printStackTrace();
            }
            return;
        }

        /*if(!yaml.contains(ChestYaml.OWNER_UUID)) {
            main.getLogger().warning("Could not load AngelChest file " + file.getName());
            success = false;
            return;
        }*/

        this.main = Main.getInstance();
        this.owner = UUID.fromString(Objects.requireNonNull(yaml.getString(ChestYaml.OWNER_UUID)));
        this.levels = yaml.getInt("levels", 0);
        this.isProtected = yaml.getBoolean("isProtected");
        this.secondsLeft = yaml.getInt("secondsLeft");
        this.infinite = yaml.getBoolean("infinite",false);
        this.unlockIn = yaml.getInt("unlockIn",-1);
        this.price = yaml.getDouble("price", main.getConfig().getDouble(Config.PRICE));
        this.logfile = yaml.getString("logfile",null);
        this.created = yaml.getLong("created",0);

        if(yaml.isSet("deathCause")) {
            this.deathCause = yaml.getSerializable("deathCause", DeathCause.class);
        } else {
            this.deathCause = new DeathCause(EntityDamageEvent.DamageCause.CUSTOM,"UNKNOWN");
        }

        if(yaml.contains("opened-by")) {
            this.openedBy = yaml.getStringList("opened-by");
        } else {
            openedBy = new ArrayList<>();
        }


        // Check if this is the current save format
        int saveVersion = yaml.getInt("angelchest-saveversion", 1);
        if (saveVersion == 1) {
            try {
                this.block = Objects.requireNonNull(yaml.getLocation(ChestYaml.LEGACY_BLOCK)).getBlock();
                this.worldid = block.getWorld().getUID();
            } catch (Exception exception) {
                success = false;

                main.getLogger().warning("Failed to create AngelChest from file");
                exception.printStackTrace();

            }
            if (!success) return;
        } else {
            this.worldid = UUID.fromString(Objects.requireNonNull(yaml.getString("worldid")));
            if (main.getServer().getWorld(worldid) == null) {
                success = false;
                main.getLogger().warning("Failed to create AngelChest because no world with this id could be found");
                return;
            }
            this.block = Objects.requireNonNull(main.getServer().getWorld(worldid)).getBlockAt(yaml.getInt("x"), yaml.getInt("y"), yaml.getInt("z"));
        }

        //String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
        String inventoryName = main.messages.ANGELCHEST_INVENTORY_NAME.replaceAll("\\{player}", Objects.requireNonNull(main.getServer().getOfflinePlayer(owner).getName()));

        if(!block.getWorld().isChunkLoaded(block.getX() >> 4,block.getZ() >> 4)) {
            main.debug("Chunk is not loaded, trying to load chunk async...");
            PaperLib.getChunkAtAsync(block.getLocation());
            if(!block.getWorld().isChunkLoaded(block.getX() >> 4,block.getZ() >> 4)) {
                main.debug("The chunk is still unloaded... Trying to load chunk synced...");
                block.getChunk().load();
                if(!block.getWorld().isChunkLoaded(block.getX() >> 4,block.getZ() >> 4)) {
                    main.debug("The chunk is still unloaded... creating the chest will probably fail.");
                }
            }
        }

        createChest(block,owner);

        // Load OverflowInv
        AngelChestHolder holder = new AngelChestHolder();
        overflowInv = Bukkit.createInventory(holder, 54, inventoryName);
        holder.setInventory(overflowInv);
        int iOverflow = 0;
        //noinspection SuspiciousToArrayCall
        for (ItemStack is : Objects.requireNonNull(yaml.getList("overflowInv")).toArray(new ItemStack[54])) {
            if (is != null) overflowInv.setItem(iOverflow, is);
            iOverflow++;
        }

        // Load ArmorInv
        armorInv = new ItemStack[4];
        int iArmor = 0;
        //noinspection SuspiciousToArrayCall
        for (ItemStack is : Objects.requireNonNull(yaml.getList("armorInv")).toArray(new ItemStack[4])) {
            if (is != null) armorInv[iArmor] = is;
            iArmor++;
        }

        // Load StorageInv
        storageInv = new ItemStack[36];
        int iStorage = 0;
        //noinspection SuspiciousToArrayCall
        for (ItemStack is : Objects.requireNonNull(yaml.getList("storageInv")).toArray(new ItemStack[36])) {
            if (is != null) storageInv[iStorage] = is;
            iStorage++;
        }

        // Load ExtraInv
        extraInv = new ItemStack[1];
        int iExtra = 0;
        //noinspection SuspiciousToArrayCall
        for (ItemStack is : Objects.requireNonNull(yaml.getList("extraInv")).toArray(new ItemStack[1])) {
            if (is != null) extraInv[iExtra] = is;
            iExtra++;
        }

        if(!file.delete()) {
            main.getLogger().severe("Could not remove AngelChest file "+file.getAbsolutePath());
        }
    }

    @Override
    public ItemStack[] getArmorInv() {
        return armorInv;
    }

    @Override
    public void setArmorInv(ItemStack[] armorInv) {
        if(armorInv.length!=4) {
            throw new IllegalArgumentException("Armor inventory must be an array of exactly 4 ItemStacks.");
        }
        this.armorInv = armorInv;
    }

    @Override
    public ItemStack[] getStorageInv() {
        return storageInv;
    }

    @Override
    public void setStorageInv(ItemStack[] storageInv) {
        if(storageInv.length!=36) {
            throw new IllegalArgumentException("Storage inventory must be an array of exactly 36 ItemStacks.");
        }
        this.storageInv = storageInv;
    }

    @Override
    public ItemStack getOffhandItem() {
        return extraInv[0];
    }

    @Override
    public void setOffhandItem(ItemStack extraInv) {
        this.extraInv[0] = extraInv;
    }

    @Override
    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }

    @Override
    public int getSecondsLeft() {
        return secondsLeft;
    }

    @Override
    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    @Override
    public int getUnlockIn() {
        return unlockIn;
    }

    @Override
    public void setUnlockIn(int unlockIn) {
        this.unlockIn = unlockIn;
    }

    @Override
    public int getExperience() {
        return experience;
    }

    @Override
    public void setExperience(int experience) {
        this.experience = experience;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean isInfinite() {
        return infinite;
    }

    @Override
    public void setInfinite(boolean isInfinite) {
        this.infinite = isInfinite;
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld(worldid);
    }

    @Override
    public List<OfflinePlayer> getOpenedBy() {
        List<OfflinePlayer> players = new ArrayList<>();
        for(String uuid : openedBy) {
            players.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        }
        return players;
    }

    @Override
    public long getCreated() {
        return created;
    }

    /**
     * Creates a new AngelChest
     * @param player Player that this AngelChest belongs to
     * @param block Block where the AngelCcest should be created
     * @param logfile Name of the logfile for this AngelChest
     */
    public AngelChest(Player player, Block block, String logfile, DeathCause deathCause) {

        main = Main.getInstance();
        main.debug("Creating AngelChest natively for player "+player.getName());

        this.main = Main.getInstance();
        this.owner = player.getUniqueId();
        this.block = block;
        this.logfile = logfile;
        this.openedBy = new ArrayList<>();
        this.price = main.groupUtils.getSpawnPricePerPlayer(player);
        this.isProtected = Objects.requireNonNull(main.getServer().getPlayer(owner)).hasPermission("angelchest.protect");
        this.secondsLeft = main.groupUtils.getDurationPerPlayer(main.getServer().getPlayer(owner));
        this.unlockIn = main.groupUtils.getUnlockDurationPerPlayer(main.getServer().getPlayer(owner));
        this.deathCause = deathCause;
        this.blacklistedItems = new ArrayList<>();
        this.created = System.currentTimeMillis();
        if(secondsLeft<=0) infinite = true;

        String inventoryName = main.messages.ANGELCHEST_INVENTORY_NAME.replaceAll("\\{player}", player.getName());
        overflowInv = Bukkit.createInventory(null, 54, inventoryName);
        // TODO: We are doing this in the PlayerListener instead
        //createChest(block,player.getUniqueId());

        PlayerInventory playerInventory = player.getInventory();

        // Remove curse of vanishing equipment and Minepacks backpacks
        main.debug("===== PLAYER INVENTORY CONTENTS =====");
        for (int i = 0; i<playerInventory.getSize();i++) {
            if (Utils.isEmpty(playerInventory.getItem(i))) {
                continue;
            }
            String isBlacklisted = main.isItemBlacklisted(playerInventory.getItem(i));
            if(isBlacklisted!=null) {
                main.debug("Slot " + i + ": [BLACKLISTED: \""+isBlacklisted+"\"] " + playerInventory.getItem(i));
                blacklistedItems.add(playerInventory.getItem(i));
                playerInventory.clear(i);
            }
            else {
                main.debug("Slot " + i + ": " + playerInventory.getItem(i));
                if (toBeRemoved(playerInventory.getItem(i))) playerInventory.setItem(i, null);
            }
        }
        main.debug("===== PLAYER INVENTORY CONTENTS END =====");

        int randomItemLoss = main.groupUtils.getItemLossPerPlayer(player);
        if(randomItemLoss > 0) {
            if(Daddy.allows(Features.RANDOM_ITEM_LOSS)) {
                main.debug("===== RANDOM ITEM LOSS START =====");
                main.debug("Removed " + randomItemLoss + " item stacks randomly:");
                randomlyLostItems = InventoryUtils.removeRandomItemsFromInventory(playerInventory, randomItemLoss);
                for (ItemStack lostItem : randomlyLostItems) {
                    main.debug(lostItem.toString());
                }
                main.debug("===== RANDOM ITEM LOSS END =====");
            } else {
                main.getLogger().warning("You are using random-item-loss, which is only available in AngelChestPlus. See here: " + Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
            }
        }

        armorInv = playerInventory.getArmorContents();
        storageInv = playerInventory.getStorageContents();
        extraInv = playerInventory.getExtraContents();

        removeKeptItems();
    }

    /**
     * Returns the filename where this AngelChest is saved
     * @return filename for this AngelChest
     */
    public String getFileName() {
        return main.getConfig().getString(Config.CHEST_FILENAME)
                .replaceAll("\\{world}",block.getWorld().getName())
                .replaceAll("\\{uuid}",owner.toString())
                .replaceAll("\\{player}", Objects.requireNonNull(Bukkit.getOfflinePlayer(owner).getName()))
                .replaceAll("\\{x}", String.valueOf(block.getX()))
                .replaceAll("\\{y}", String.valueOf(block.getY()))
                .replaceAll("\\{z}", String.valueOf(block.getZ()))
        ;
    }

    /**
     * Checks whether this AngelChest has been completely looted
     * @return true when AngelChest is empty, otherwise false
     */
    @Override
    public boolean isEmpty() {
        for(ItemStack item : storageInv) {
            if(!Utils.isEmpty(item)) return false;
        }
        for(ItemStack item : armorInv) {
            if(!Utils.isEmpty(item)) return false;
        }
        for(ItemStack item : extraInv) {
            if(!Utils.isEmpty(item)) return false;
        }
        for(ItemStack item : overflowInv) {
            if(!Utils.isEmpty(item)) return false;
        }
        return experience <= 0;
    }

    /**
     * Removes all items that the player kept on death, and all other items that should not
     * appear in the chest (e.g. curse of vanishing, soulbound items, ...)
     */
    private void removeKeptItems() {

        for(int i = 0; i <armorInv.length;i++) {
            if(main.hookUtils.keepOnDeath(armorInv[i])
                    || main.hookUtils.removeOnDeath(armorInv[i])) {
                armorInv[i]=null;
            }
        }
        for(int i = 0; i <storageInv.length;i++) {
            if(main.hookUtils.keepOnDeath(storageInv[i])
                    || main.hookUtils.removeOnDeath(storageInv[i])) {
                storageInv[i]=null;
            }
        }for(int i = 0; i <extraInv.length;i++) {
            if(main.hookUtils.keepOnDeath(extraInv[i])
                    || main.hookUtils.removeOnDeath(extraInv[i])) {
                extraInv[i]=null;
            }
        }
    }

    /**
     * Checks whether this item should be removed from the inventory
     * @param item The item to check
     * @return true when this item should be removed, otherwise false
     */
    private boolean toBeRemoved(ItemStack item) {
        if(item==null) return false;
        if(main.getConfig().getBoolean(Config.REMOVE_CURSE_OF_VANISHING)
                && item.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
            return true;
        }
        if(main.getConfig().getBoolean(Config.REMOVE_CURSE_OF_BINDING)
                && item.getEnchantments().containsKey(Enchantment.BINDING_CURSE)) {
            return true;
        }
        if (main.minepacksHook.isMinepacksBackpack(item)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "AngelChest{block=" + block.toString() + ",owner=" + owner.toString() + "}";
    }

    public void createChest(Block block, UUID uuid) {
        createChest(block,uuid,true);
    }
    /**
     * Sets the block at the location to the correct material
     * @param block The block where the AngelChest is spawned
     * @param uuid The owner's UUID (to correctly set player heads)
     */
    public void createChest(Block block, UUID uuid, boolean createHologram) {
        main.debug("Attempting to create chest with material " + main.getChestMaterial(this).name() + " at "+block.getLocation().toString());
        block.setType(main.getChestMaterial(this));

        // Material is PLAYER_HEAD, so either use the custom texture, or the player skin's texture
        if(main.getChestMaterial(this) == Material.PLAYER_HEAD) {
            /*if(Material.getMaterial(Values.PLAYER_HEAD) == null) {
                main.getLogger().warning("Using a custom PLAYER_HEAD as chest material is NOT SUPPORTED in versions < 1.13. Consider using another chest material.");
            } else {*/
                HeadCreator.createHeadInWorld(block, uuid);
            //}
        }
        if(createHologram) {
            createHologram(block, uuid);
        }
    }

    /**
     * Removes the chest block from the world and displays explosion particles.
     * @param block Block where the AngelChest was spawned
     */
    public void destroyChest(Block block) {
        main.debug("Destroying chest at "+block.getLocation()+toString());
        block.setType(Material.AIR);
        Objects.requireNonNull(block.getLocation().getWorld()).spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1);
        hologram.destroy();
    }

    /**
     * Unlocks this AngelChest for ALL players.
     */
    public void unlock() {
        this.isProtected = false;
    }

    /**
     * Saves the AngelChest to a yaml file.
     * @param removeChest Whether to also remove the AngelChest from the world
     * @return File where the AngelChest has been saved to
     */
    public File saveToFile(boolean removeChest) {
        File yamlFile = new File(main.getDataFolder() + File.separator + "angelchests",
                this.getFileName());
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
        yaml.set("angelchest-saveversion", 2);

        // We are not using block objects to avoid problems with unloaded or removed worlds
        yaml.set("worldid", Objects.requireNonNull(block.getLocation().getWorld()).getUID().toString());
        yaml.set("x", block.getX());
        yaml.set("y", block.getY());
        yaml.set("z", block.getZ());

        yaml.set("infinite",infinite);
        yaml.set(ChestYaml.OWNER_UUID, owner.toString());
        yaml.set("isProtected", isProtected);
        yaml.set("secondsLeft", secondsLeft);
        yaml.set("unlockIn", unlockIn);
        yaml.set("created",created);
        yaml.set("experience", experience);
        yaml.set("levels", levels);
        yaml.set("price",price);
        yaml.set("deathCause",deathCause);
        yaml.set("opened-by",openedBy);
        yaml.set("logfile",logfile);
        yaml.set("storageInv", storageInv);
        yaml.set("armorInv", armorInv);
        yaml.set("extraInv", extraInv);
        yaml.set("overflowInv", overflowInv.getContents());


        if(removeChest) {
            // Duplicate Start
            block.setType(Material.AIR);
            if (hologram != null) hologram.destroy();
        }
        // Duplicate End
        try {
            yaml.save(yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return yamlFile;
    }

    /**
     * Attempts to charge the player for opening the chest. Each player will only be charged once per chest.
     * @param player The player that attempted to open the chest
     * @return true if the player successfully paid now or has already paid, otherwise false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasPaidForOpening(Player player) {
        main.debug("Checking whether "+player+" already paid to open this chest...");
        if(openedBy.contains(player.getUniqueId().toString())) {
            main.debug("Yes, they did!");
            return true;
        }
        double price = main.groupUtils.getOpenPricePerPlayer(player);
        main.debug("No, they didn't... It will cost "+price);
        main.logger.logPaidForChest(player,price,main.logger.getLogFile(logfile));
        if(CommandUtils.hasEnoughMoney(player,price,main.messages.MSG_NOT_ENOUGH_MONEY,"AngelChest opened")) {
            openedBy.add(player.getUniqueId().toString());
            if(main.economyStatus == EconomyStatus.ACTIVE) {
                if(price>0) {
                    player.sendMessage(main.messages.MSG_PAID_OPEN
                            .replaceAll("\\{price}", String.valueOf(price))
                            .replaceAll("\\{currency}", CommandUtils.getCurrency(price))
                    );
                }
            }
            return true;
        }
        player.sendMessage(main.messages.MSG_NOT_ENOUGH_MONEY);
        return false;
    }

    /**
     * Removes the Block from the world. Attempts to load the chunk first.
     * @param refund Whether the owner should get the price back they paid to have the chest spawned.
     */
    public void destroy(boolean refund) {
        main.debug("Destroying AngelChest");

        if(!block.getWorld().isChunkLoaded(block.getX() >> 4,block.getZ() >> 4)) {
            main.debug("Chunk is not loaded, trying to load chunk async...");
            PaperLib.getChunkAtAsync(block.getLocation());
            if(!block.getWorld().isChunkLoaded(block.getX() >> 4,block.getZ() >> 4)) {
                main.debug("The chunk is still unloaded... Trying to load chunk synced...");
                block.getChunk().load();
                if(!block.getWorld().isChunkLoaded(block.getX() >> 4,block.getZ() >> 4)) {
                    main.debug("The chunk is still unloaded... destroying the chest will probably fail.");
                }
            }
        }


        if (!main.isAngelChest(block))
            return;

        // remove the physical chest
        destroyChest(block);
        hologram.destroy();

        // drop contents
        Utils.dropItems(block, armorInv);
        Utils.dropItems(block, storageInv);
        Utils.dropItems(block, extraInv);
        //Utils.dropItems(block, overflowInv);

        if (experience > 0) {
            Utils.dropExp(block, experience);
        }

        if(refund
                && main.getConfig().getBoolean(Config.REFUND_EXPIRED_CHESTS)
                && price > 0) {
            CommandUtils.payMoney(Bukkit.getOfflinePlayer(owner),price, "AngelChest expired");
        }

        int currentChestId = AngelChestUtils.getAllAngelChestsFromPlayer(Bukkit.getOfflinePlayer(owner)).indexOf(this)+1;
        Player player = Bukkit.getPlayer(owner);
        if(player != null && player.isOnline()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> main.guiManager.updateGUI(player, currentChestId), 1L);
        }

    }

    /**
     * Removes this AngelChest from the memory
     */
    public void remove() {
        main.debug("Removing AngelChest");
        main.angelChests.remove(block);
    }

    /**
     * Creates the hologram above the AngelChest
     * @param block Block where the hologram should be spawned
     * @param uuid Owner of this AngelChest
     */
	public void createHologram(Block block, UUID uuid) {
        String hologramText = main.messages.HOLOGRAM_TEXT
                .replaceAll("\\{player}", Objects.requireNonNull(main.getServer().getOfflinePlayer(uuid).getName()))
                .replaceAll("\\{deathcause}",deathCause.getText());
		hologram = new Hologram(block, hologramText, this);
	}

}