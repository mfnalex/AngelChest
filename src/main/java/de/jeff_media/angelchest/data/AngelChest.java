package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.ChestYaml;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.config.Messages;
import de.jeff_media.angelchest.config.Permissions;
import de.jeff_media.angelchest.enums.EconomyStatus;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.handlers.GraveyardManager;
import de.jeff_media.angelchest.listeners.EnderCrystalListener;
import de.jeff_media.angelchest.utils.*;
import de.jeff_media.daddy.Daddy;
import de.jeff_media.jefflib.thirdparty.io.papermc.paperlib.PaperLib;
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

    private static final int MAX_INVENTORY_SIZE = 54;
    private static final int STORAGE_INVENTORY_SIZE = 36;
    public ItemStack[] armorInv;
    public List<ItemStack> blacklistedItems;
    public Block block;
    public long created;
    public DeathCause deathCause;
    public int experience = 0;
    public ItemStack[] extraInv;
    public Hologram hologram;
    public boolean infinite = false;
    public boolean isProtected;
    public int levels = 0;
    public String logfile;
    public Main main;
    public List<String> openedBy;
    public Inventory overflowInv;
    public UUID owner;
    public UUID killer;
    public @Nullable Graveyard graveyard;
    public Set<ItemStack> randomlyLostItems = null;
    public int secondsLeft;
    public ItemStack[] storageInv;
    public boolean success = true;
    public int unlockIn;
    public UUID worldid;
    double price = 0;

    /**
     * Loads an AngelChest from a YAML file
     *
     * @param file File containing the AngelChest data
     */
    public @Nullable AngelChest(final File file) {
        main = Main.getInstance();
        if (main.debug) main.debug("Creating AngelChest from file " + file.getName());
        final YamlConfiguration yaml;
        try {
            yaml = new YamlConfiguration();
            yaml.load(file);
        } catch (final Throwable t) {
            main.getLogger().warning("Could not load AngelChest file " + file.getName());
            success = false;
            if (main.debug) {
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
        this.levels = yaml.getInt(ChestYaml.EXP_LEVELS, 0);
        this.isProtected = yaml.getBoolean(ChestYaml.IS_PROTECTED);
        this.secondsLeft = yaml.getInt(ChestYaml.SECONDS_LEFT);
        this.infinite = yaml.getBoolean(ChestYaml.IS_INFINITE, false);
        this.killer = yaml.isSet("killer") ? UUID.fromString(yaml.getString("killer")) : null;
        this.unlockIn = yaml.getInt("unlockIn", -1);
        this.price = yaml.getDouble(ChestYaml.PRICE, main.getConfig().getDouble(Config.PRICE));
        this.logfile = yaml.getString("logfile", null);
        this.created = yaml.getLong("created", 0);

        if(yaml.isSet("graveyard")) {
            Graveyard yard = GraveyardManager.fromName(yaml.getString("graveyard"));
            if(yard == null) {
                main.getLogger().warning("AngelChest loaded in removed Graveyard " + yaml.getString("graveyard"));
            } else {
                this.graveyard = yard;
            }
        }

        if (yaml.isSet("deathCause")) {
            this.deathCause = yaml.getSerializable("deathCause", DeathCause.class);
        } else {
            this.deathCause = new DeathCause(EntityDamageEvent.DamageCause.CUSTOM, "UNKNOWN");
        }

        if (yaml.contains("opened-by")) {
            this.openedBy = yaml.getStringList("opened-by");
        } else {
            openedBy = new ArrayList<>();
        }


        // Check if this is the current save format
        final int saveVersion = yaml.getInt(ChestYaml.SAVE_VERSION, 1);
        if (saveVersion == 1) {
            try {
                this.block = Objects.requireNonNull(yaml.getLocation(ChestYaml.LEGACY_BLOCK)).getBlock();
                this.worldid = block.getWorld().getUID();
            } catch (final Exception exception) {
                success = false;

                main.getLogger().warning("Failed to create AngelChest from file");
                exception.printStackTrace();

            }
            if (!success) return;
        } else {
            this.worldid = UUID.fromString(Objects.requireNonNull(yaml.getString(ChestYaml.WORLD_UID)));
            if (main.getServer().getWorld(worldid) == null) {
                success = false;
                if (main.debug)
                    main.debug("Failed to create AngelChest because no world with this id (" + worldid.toString() + ") could be found");
                return;
            }
            this.block = Objects.requireNonNull(main.getServer().getWorld(worldid)).getBlockAt(yaml.getInt(ChestYaml.BLOCK_X), yaml.getInt(ChestYaml.BLOCK_Y), yaml.getInt(ChestYaml.BLOCK_Z));
        }

        // Check for blocks being too high or too low
        // No, we do it differently. The Hologram etc can spawn fine, we just omit the block and check for this too in the
        // "is AngelChest broken" method
        /*if(this.block.getY() < 0 || this.block.getY() >= this.block.getWorld().getMaxHeight()) {
            main.getLogger().severe("You have an invalid AngelChest which is either in the void or above the maximum build height at "+block.toString());
            main.getLogger().severe("The chest will NOT be spawned. Please manually delete the AngelChest file and report this problem.");
            main.getLogger().severe("The file name of the corrupt chest is "+file.getName());
            success=false;
            return;
        }*/

        //String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
        final String inventoryName = main.messages.ANGELCHEST_INVENTORY_NAME.replaceAll("\\{player}", Objects.requireNonNull(main.getServer().getOfflinePlayer(owner).getName()));

        if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
            if (main.debug) main.debug("Chunk is not loaded, trying to load chunk async...");
            PaperLib.getChunkAtAsync(block.getLocation());
            if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                if (main.debug) main.debug("The chunk is still unloaded... Trying to load chunk synced...");
                block.getChunk().load();
                if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                    if (main.debug) main.debug("The chunk is still unloaded... creating the chest will probably fail.");
                }
            }
        }

        // Load OverflowInv
        final AngelChestHolder holder = new AngelChestHolder();
        overflowInv = Bukkit.createInventory(holder, MAX_INVENTORY_SIZE, inventoryName);
        holder.setInventory(overflowInv);
        int iOverflow = 0;
        //noinspection SuspiciousToArrayCall
        for (final ItemStack is : Objects.requireNonNull(yaml.getList("overflowInv")).toArray(new ItemStack[MAX_INVENTORY_SIZE])) {
            if (is != null) overflowInv.setItem(iOverflow, is);
            iOverflow++;
        }

        // Load ArmorInv
        armorInv = new ItemStack[4];
        int iArmor = 0;
        //noinspection SuspiciousToArrayCall
        for (final ItemStack is : Objects.requireNonNull(yaml.getList("armorInv")).toArray(new ItemStack[4])) {
            if (is != null) armorInv[iArmor] = is;
            iArmor++;
        }

        // Load StorageInv
        storageInv = new ItemStack[STORAGE_INVENTORY_SIZE];
        int iStorage = 0;
        //noinspection SuspiciousToArrayCall
        for (final ItemStack is : Objects.requireNonNull(yaml.getList("storageInv")).toArray(new ItemStack[STORAGE_INVENTORY_SIZE])) {
            if (is != null) storageInv[iStorage] = is;
            iStorage++;
        }

        // Load ExtraInv
        extraInv = new ItemStack[1];
        int iExtra = 0;
        //noinspection SuspiciousToArrayCall
        for (final ItemStack is : Objects.requireNonNull(yaml.getList("extraInv")).toArray(new ItemStack[1])) {
            if (is != null) extraInv[iExtra] = is;
            iExtra++;
        }

        createChest(block, owner);

        if (!file.delete()) {
            main.getLogger().severe("Could not remove AngelChest file " + file.getAbsolutePath());
        }


    }

    /**
     * Creates a new AngelChest
     *
     * @param player  Player that this AngelChest belongs to
     * @param block   Block where the AngelCcest should be created
     * @param logfile Name of the logfile for this AngelChest
     */
    public AngelChest(final Player player, final Block block, final String logfile, final DeathCause deathCause) {

        main = Main.getInstance();
        if (main.debug) main.debug("Creating AngelChest natively for player " + player.getName());

        this.main = Main.getInstance();
        this.owner = player.getUniqueId();
        this.block = block;
        this.worldid = block.getWorld().getUID();
        this.logfile = logfile;
        this.openedBy = new ArrayList<>();
        this.price = main.groupUtils.getSpawnPricePerPlayer(player);
        this.isProtected = Objects.requireNonNull(main.getServer().getPlayer(owner)).hasPermission(Permissions.PROTECT);
        this.secondsLeft = main.groupUtils.getDurationPerPlayer(main.getServer().getPlayer(owner));
        this.unlockIn = main.groupUtils.getUnlockDurationPerPlayer(main.getServer().getPlayer(owner));
        this.deathCause = deathCause;
        this.blacklistedItems = new ArrayList<>();
        this.created = System.currentTimeMillis();
        this.graveyard = GraveyardManager.fromBlock(block);

        if (player.getKiller() == null) {
            if (deathCause.isEnderCrystalDeath() && EnderCrystalListener.lastEnderCrystalKiller != null && !EnderCrystalListener.lastEnderCrystalKiller.equals(owner)) {
                this.killer = EnderCrystalListener.lastEnderCrystalKiller;
            } else {
                this.killer = null;
            }
        } else {
            this.killer = player.getKiller().getUniqueId();
        }


        if (secondsLeft <= 0) infinite = true;

        final String inventoryName = main.messages.ANGELCHEST_INVENTORY_NAME.replaceAll("\\{player}", player.getName());
        overflowInv = Bukkit.createInventory(null, MAX_INVENTORY_SIZE, inventoryName);

        final PlayerInventory playerInventory = player.getInventory();

        // Remove curse of vanishing equipment and Minepacks backpacks
        LogUtils.debugBanner(new String[]{"PLAYER INVENTORY CONTENTS"});
        for (int i = 0; i < playerInventory.getSize(); i++) {
            if (Utils.isEmpty(playerInventory.getItem(i))) {
                continue;
            }
            final String isBlacklisted = main.isItemBlacklisted(playerInventory.getItem(i));
            if (isBlacklisted != null) {
                if (main.debug)
                    main.debug("Slot " + i + ": [BLACKLISTED: \"" + isBlacklisted + "\"] " + playerInventory.getItem(i) + "\n");
                blacklistedItems.add(playerInventory.getItem(i));
                playerInventory.clear(i);
            } else {
                if (main.debug) main.debug("Slot " + i + ": " + playerInventory.getItem(i) + "\n");
                if (toBeRemoved(playerInventory.getItem(i))) playerInventory.setItem(i, null);
            }
        }
        LogUtils.debugBanner(new String[]{"PLAYER INVENTORY CONTENTS END"});

        final int randomItemLoss = main.groupUtils.getItemLossPerPlayer(player);
        if (randomItemLoss > 0) {
            if (Daddy.allows(PremiumFeatures.RANDOM_ITEM_LOSS)) {
                LogUtils.debugBanner(new String[]{"RANDOM ITEM LOSS"});
                if (main.debug) main.debug("Removed " + randomItemLoss + " item stacks randomly:");
                randomlyLostItems = InventoryUtils.removeRandomItemsFromInventory(playerInventory, randomItemLoss);
                for (final ItemStack lostItem : randomlyLostItems) {
                    if (main.debug) main.debug(lostItem.toString() + "\n");
                }
                LogUtils.debugBanner(new String[]{"RANDOM ITEM LOSS END"});
            } else {
                main.getLogger().warning("You are using random-item-loss, which is only available in AngelChestPlus. See here: " + Main.UPDATECHECKER_LINK_DOWNLOAD_PLUS);
            }
        }

        armorInv = playerInventory.getArmorContents();
        storageInv = playerInventory.getStorageContents();
        extraInv = playerInventory.getExtraContents();

        removeKeptItems();
    }

    public @Nullable UUID getKiller() {
        return killer;
    }

    public void setKiller(final UUID killer) {
        this.killer = killer;
    }

    public int getNumberOfItems() {
        int items = 0;
        for (final ItemStack item : storageInv) {
            if (item != null && item.getAmount() > 0) items++;
        }
        for (final ItemStack item : armorInv) {
            if (item != null && item.getAmount() > 0) items++;
        }
        for (final ItemStack item : extraInv) {
            if (item != null && item.getAmount() > 0) items++;
        }
        return items;
    }

    @Nullable
    public Graveyard getGraveyard() {
        return graveyard;
    }

    public void createChest(final Block block, final UUID uuid) {
        createChest(block, uuid, true);
    }

    /**
     * Sets the block at the location to the correct material
     *
     * @param block The block where the AngelChest is spawned
     * @param uuid  The owner's UUID (to correctly set player heads)
     */
    public void createChest(final Block block, final UUID uuid, final boolean createHologram) {
        if (main.debug)
            main.debug("Attempting to create chest with material " + main.getChestMaterial(this).name() + " at " + block.getLocation());
        block.setType(main.getChestMaterial(this));

        // Material is PLAYER_HEAD, so either use the custom texture, or the player skin's texture
        if (main.getChestMaterial(this) == Material.PLAYER_HEAD) {
            /*if(Material.getMaterial(Values.PLAYER_HEAD) == null) {
                main.getLogger().warning("Using a custom PLAYER_HEAD as chest material is NOT SUPPORTED in versions < 1.13. Consider using another chest material.");
            } else {*/
            HeadCreator.createHeadInWorld(block, uuid);
            //}
        }
        if (createHologram) {
            createHologram(block, uuid);
        }
    }

    /**
     * Creates the hologram above the AngelChest
     *
     * @param block Block where the hologram should be spawned
     * @param uuid  Owner of this AngelChest
     */
    public void createHologram(final Block block, final UUID uuid) {
        String hologramText = main.messages.HOLOGRAM_TEXT;

        Graveyard graveyard = GraveyardManager.fromBlock(block);
        if(graveyard != null) {
            if(graveyard.hasCustomHologram()) {
                hologramText = graveyard.getCustomHologram();
            }
        }

        hologramText = hologramText.replaceAll("\\{player}", Objects.requireNonNull(main.getServer().getOfflinePlayer(uuid).getName())).replaceAll("\\{deathcause}", deathCause.getText());
        hologram = new Hologram(block, hologramText, this);
    }

    /**
     * Removes the Block from the world. Attempts to load the chunk first.
     *
     * @param refund Whether the owner should get the price back they paid to have the chest spawned.
     */
    public void destroy(final boolean refund) {
        if (main.debug) main.debug("Destroying AngelChest");

        if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
            if (main.debug) main.debug("Chunk is not loaded, trying to load chunk async...");
            PaperLib.getChunkAtAsync(block.getLocation());
            if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                if (main.debug) main.debug("The chunk is still unloaded... Trying to load chunk synced...");
                block.getChunk().load();
                if (main.debug && !block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                    main.debug("The chunk is still unloaded... destroying the chest will probably fail.");
                }
            }
        }


        if (!main.isAngelChest(block)) return;

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

        if (refund && main.getConfig().getBoolean(Config.REFUND_EXPIRED_CHESTS) && price > 0) {
            CommandUtils.payMoney(Bukkit.getOfflinePlayer(owner), price, "AngelChest expired");
        }

        final int currentChestId = AngelChestUtils.getAllAngelChestsFromPlayer(Bukkit.getOfflinePlayer(owner)).indexOf(this) + 1;
        final Player player = Bukkit.getPlayer(owner);
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> main.guiManager.updateGUI(player, currentChestId), 1L);
        }

    }

    /**
     * Removes the chest block from the world and displays explosion particles.
     *
     * @param block Block where the AngelChest was spawned
     */
    public void destroyChest(final Block block) {
        if (main.debug) main.debug("Destroying chest at " + block.getLocation() + this);
        block.setType(Material.AIR);
        Objects.requireNonNull(block.getLocation().getWorld()).spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1);
        hologram.destroy();
    }

    @Override
    public ItemStack[] getArmorInv() {
        return armorInv;
    }

    @Override
    public void setArmorInv(final ItemStack[] armorInv) {
        if (armorInv.length != 4) {
            throw new IllegalArgumentException("Armor inventory must be an array of exactly 4 ItemStacks.");
        }
        this.armorInv = armorInv;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public int getExperience() {
        return experience;
    }

    @Override
    public void setExperience(final int experience) {
        this.experience = experience;
    }

    /**
     * Returns the filename where this AngelChest is saved
     *
     * @return filename for this AngelChest
     */
    public String getFileName() {
        return main.getConfig().getString(Config.CHEST_FILENAME).replaceAll("\\{world}", block.getWorld().getName()).replaceAll("\\{uuid}", owner.toString()).replaceAll("\\{player}", Objects.requireNonNull(Bukkit.getOfflinePlayer(owner).getName())).replaceAll("\\{x}", String.valueOf(block.getX())).replaceAll("\\{y}", String.valueOf(block.getY())).replaceAll("\\{z}", String.valueOf(block.getZ()));
    }

    @Override
    public ItemStack getOffhandItem() {
        return extraInv[0];
    }

    @Override
    public void setOffhandItem(final ItemStack extraInv) {
        this.extraInv[0] = extraInv;
    }

    @Override
    public List<OfflinePlayer> getOpenedBy() {
        final List<OfflinePlayer> players = new ArrayList<>();
        for (final String uuid : openedBy) {
            players.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        }
        return players;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }

    @Override
    public int getSecondsLeft() {
        return secondsLeft;
    }

    @Override
    public void setSecondsLeft(final int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    @Override
    public ItemStack[] getStorageInv() {
        return storageInv;
    }

    @Override
    public void setStorageInv(final ItemStack[] storageInv) {
        if (storageInv.length != STORAGE_INVENTORY_SIZE) {
            throw new IllegalArgumentException("Storage inventory must be an array of exactly 36 ItemStacks.");
        }
        this.storageInv = storageInv;
    }

    @Override
    public int getUnlockIn() {
        return unlockIn;
    }

    @Override
    public void setUnlockIn(final int unlockIn) {
        this.unlockIn = unlockIn;
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld(worldid);
    }

    /**
     * Attempts to charge the player for opening the chest. Each player will only be charged once per chest.
     *
     * @param player The player that attempted to open the chest
     * @return true if the player successfully paid now or has already paid, otherwise false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasPaidForOpening(final Player player) {
        if (main.debug) main.debug("Checking whether " + player + " already paid to open this chest...");
        if (openedBy.contains(player.getUniqueId().toString())) {
            if (main.debug) main.debug("Yes, they did!");
            return true;
        }
        final double price = main.groupUtils.getOpenPricePerPlayer(player);
        if (main.debug) main.debug("No, they didn't... It will cost " + price);
        main.logger.logPaidForChest(player, price, main.logger.getLogFile(logfile));
        if (CommandUtils.hasEnoughMoney(player, price, main.messages.MSG_NOT_ENOUGH_MONEY, "AngelChest opened")) {
            openedBy.add(player.getUniqueId().toString());
            if (main.economyStatus == EconomyStatus.ACTIVE) {
                if (price > 0) {
                    Messages.send(player, main.messages.MSG_PAID_OPEN.replaceAll("\\{price}", String.valueOf(price)).replaceAll("\\{currency}", CommandUtils.getCurrency(price)));
                }
            }
            return true;
        }
        Messages.send(player, main.messages.MSG_NOT_ENOUGH_MONEY);
        return false;
    }

    /**
     * Checks whether this AngelChest has been completely looted
     *
     * @return true when AngelChest is empty, otherwise false
     */
    @Override
    public boolean isEmpty() {
        for (final ItemStack item : storageInv) {
            if (!Utils.isEmpty(item)) return false;
        }
        for (final ItemStack item : armorInv) {
            if (!Utils.isEmpty(item)) return false;
        }
        for (final ItemStack item : extraInv) {
            if (!Utils.isEmpty(item)) return false;
        }
        for (final ItemStack item : overflowInv) {
            if (!Utils.isEmpty(item)) return false;
        }
        return experience <= 0;
    }

    @Override
    public boolean isInfinite() {
        return infinite;
    }

    @Override
    public void setInfinite(final boolean isInfinite) {
        this.infinite = isInfinite;
    }

    @Override
    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public void setProtected(final boolean aProtected) {
        isProtected = aProtected;
    }

    /**
     * Removes this AngelChest from the memory
     */
    public void remove() {
        if (main.debug) main.debug("Removing AngelChest");
        main.angelChests.remove(block);
    }

    /**
     * Removes all items that the player kept on death, and all other items that should not
     * appear in the chest (e.g. curse of vanishing, soulbound items, ...)
     */
    private void removeKeptItems() {

        for (int i = 0; i < armorInv.length; i++) {
            if (main.genericHooks.keepOnDeath(armorInv[i]) || main.genericHooks.removeOnDeath(armorInv[i])) {
                armorInv[i] = null;
            }
        }
        for (int i = 0; i < storageInv.length; i++) {
            if (main.genericHooks.keepOnDeath(storageInv[i]) || main.genericHooks.removeOnDeath(storageInv[i])) {
                storageInv[i] = null;
            }
        }
        for (int i = 0; i < extraInv.length; i++) {
            if (main.genericHooks.keepOnDeath(extraInv[i]) || main.genericHooks.removeOnDeath(extraInv[i])) {
                extraInv[i] = null;
            }
        }
    }

    /**
     * Saves the AngelChest to a yaml file.
     *
     * @param removeChest Whether to also remove the AngelChest from the world
     * @return File where the AngelChest has been saved to
     */
    public File saveToFile(final boolean removeChest) {
        final File yamlFile = new File(main.getDataFolder() + File.separator + "angelchests", this.getFileName());
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
        yaml.set(ChestYaml.SAVE_VERSION, 2);

        // We are not using block objects to avoid problems with unloaded or removed worlds
        yaml.set(ChestYaml.WORLD_UID, Objects.requireNonNull(block.getLocation().getWorld()).getUID().toString());
        yaml.set(ChestYaml.BLOCK_X, block.getX());
        yaml.set(ChestYaml.BLOCK_Y, block.getY());
        yaml.set(ChestYaml.BLOCK_Z, block.getZ());

        yaml.set(ChestYaml.IS_INFINITE, infinite);
        yaml.set(ChestYaml.OWNER_UUID, owner.toString());
        yaml.set(ChestYaml.IS_PROTECTED, isProtected);
        yaml.set(ChestYaml.SECONDS_LEFT, secondsLeft);
        yaml.set("unlockIn", unlockIn);
        yaml.set("created", created);
        if (killer != null) yaml.set("killer", killer.toString());
        yaml.set("experience", experience);
        yaml.set(ChestYaml.EXP_LEVELS, levels);
        yaml.set(ChestYaml.PRICE, price);
        yaml.set("deathCause", deathCause);
        yaml.set("opened-by", openedBy);
        yaml.set("logfile", logfile);
        yaml.set("storageInv", storageInv);
        yaml.set("armorInv", armorInv);
        yaml.set("extraInv", extraInv);
        yaml.set("overflowInv", overflowInv.getContents());


        if (removeChest) {
            // Duplicate Start
            block.setType(Material.AIR);
            if (hologram != null) hologram.destroy();
        }
        // Duplicate End
        try {
            yaml.save(yamlFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return yamlFile;
    }

    public void scheduleBlockChange() {
        scheduleBlockChange(true);
    }

    public void scheduleBlockChange(final boolean firstTry) {
        if (firstTry) {
            if (main.debug) main.debug("scheduleBlockChange: " + block.toString());
        }
        if (main.chestMaterial == main.chestMaterialUnlocked) {
            if (main.debug) main.debug("scheduleBlockChange abort: matching materials");
            return;
        }
        final int x = block.getX();
        final int z = block.getZ();

        if (!block.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
            if (firstTry) {
                if (main.debug)
                    main.debug("Tried to change block for chest in unloaded chunk because of unlocking, will do so once chunk is loaded.");
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> scheduleBlockChange(false), 1L);
        } else {
            if (main.debug) main.debug("Changed block for chest because of unlocking.");
            createChest(block, owner, false);
        }

    }

    /**
     * Checks whether this item should be removed from the inventory
     *
     * @param item The item to check
     * @return true when this item should be removed, otherwise false
     */
    private boolean toBeRemoved(final ItemStack item) {
        if (item == null) return false;
        if (main.getConfig().getBoolean(Config.REMOVE_CURSE_OF_VANISHING) && item.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
            return true;
        }
        if (main.getConfig().getBoolean(Config.REMOVE_CURSE_OF_BINDING) && item.getEnchantments().containsKey(Enchantment.BINDING_CURSE)) {
            return true;
        }
        if (main.minepacksHook.isMinepacksBackpack(item)) {
            return true;
        }

        return false;
    }

    /*@Override
    public String toString() {
        return "AngelChest{block=" + block.toString() + ",owner=" + owner.toString() + "}";
    }*/

    @Override
    public String toString() {
        return "AngelChest{" +
                "block=" + block +
                ", created=" + created +
                ", deathCause=" + deathCause +
                ", infinite=" + infinite +
                ", isProtected=" + isProtected +
                ", openedBy=" + openedBy +
                ", owner=" + owner +
                ", secondsLeft=" + secondsLeft +
                ", unlockIn=" + unlockIn +
                ", worldid=" + worldid +
                '}';
    }

    /**
     * Unlocks this AngelChest for ALL players.
     */
    public void unlock() {
        this.isProtected = false;
    }

}