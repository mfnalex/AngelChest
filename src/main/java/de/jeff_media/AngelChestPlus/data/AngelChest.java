package de.jeff_media.AngelChestPlus.data;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jeff_media.AngelChestPlus.Main;
import de.jeff_media.AngelChestPlus.config.ChestYaml;
import de.jeff_media.AngelChestPlus.config.Config;
import de.jeff_media.AngelChestPlus.enums.EconomyStatus;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
import de.jeff_media.AngelChestPlus.utils.Utils;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an AngelChest including its content and all other relevant information
 */
public class AngelChest {

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
    public int experience = 0;
    public int levels = 0;
    public boolean infinite = false;
    public Main main;
    public String logfile;
    public List<String> openedBy;
    public DeathCause deathCause;

    double price = 0;

    /**
     * Loads an AngelChest from a YAML file
     * @param file File containing the AngelChest data
     */
    public @Nullable AngelChest(File file) {
        main = Main.getInstance();
        main.debug("Creating AngelChest from file " + file.getName());
        YamlConfiguration yaml;
        try {
            yaml = YamlConfiguration.loadConfiguration(file);
        } catch (Throwable t) {
            main.getLogger().warning("Could not load legacy AngelChest file " + file.getName());
            success = false;
            t.printStackTrace();
            return;
        }

        this.main = Main.getInstance();
        this.owner = UUID.fromString(yaml.getString(ChestYaml.OWNER_UUID));
        this.levels = yaml.getInt("levels", 0);
        this.isProtected = yaml.getBoolean("isProtected");
        this.secondsLeft = yaml.getInt("secondsLeft");
        this.infinite = yaml.getBoolean("infinite",false);
        this.price = yaml.getDouble("price", main.getConfig().getDouble(Config.PRICE));
        this.logfile = yaml.getString("logfile",null);

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
                this.block = yaml.getLocation(ChestYaml.LEGACY_BLOCK).getBlock();
                this.worldid = block.getWorld().getUID();
            } catch (Exception exception) {
                success = false;

                main.getLogger().warning("Failed to create AngelChest from file");
                exception.printStackTrace();

            }
            if (!success) return;
        } else {
            this.worldid = UUID.fromString(yaml.getString("worldid"));
            if (main.getServer().getWorld(worldid) == null) {
                success = false;
                main.getLogger().warning("Failed to create AngelChest because no world with this id could be found");
                return;
            }
            this.block = main.getServer().getWorld(worldid).getBlockAt(yaml.getInt("x"), yaml.getInt("y"), yaml.getInt("z"));
        }

        //String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getPlayer(owner).getName());
        String inventoryName = main.messages.ANGELCHEST_INVENTORY_NAME.replaceAll("\\{player}", main.getServer().getOfflinePlayer(owner).getName());

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
        for (ItemStack is : yaml.getList("overflowInv").toArray(new ItemStack[54])) {
            if (is != null) overflowInv.setItem(iOverflow, is);
            iOverflow++;
        }

        // Load ArmorInv
        armorInv = new ItemStack[4];
        int iArmor = 0;
        //noinspection SuspiciousToArrayCall
        for (ItemStack is : yaml.getList("armorInv").toArray(new ItemStack[4])) {
            if (is != null) armorInv[iArmor] = is;
            iArmor++;
        }

        // Load StorageInv
        storageInv = new ItemStack[36];
        int iStorage = 0;
        //noinspection SuspiciousToArrayCall
        for (ItemStack is : yaml.getList("storageInv").toArray(new ItemStack[36])) {
            if (is != null) storageInv[iStorage] = is;
            iStorage++;
        }

        // Load ExtraInv
        extraInv = new ItemStack[1];
        int iExtra = 0;
        //noinspection SuspiciousToArrayCall
        for (ItemStack is : yaml.getList("extraInv").toArray(new ItemStack[1])) {
            if (is != null) extraInv[iExtra] = is;
            iExtra++;
        }

        if(!file.delete()) {
            main.getLogger().severe("Could not remove AngelChest file "+file.getAbsolutePath());
        }
    }

    /**
     * Creates a new AngelChest
     * @param player Player that this AngelChest belongs to
     * @param block Block where the AngelChest should be created
     * @param logfile Name of the logfile for this AngelChest
     */
    public AngelChest(Player player, Block block, String logfile, DeathCause deathCause) {
    	this(player, player.getUniqueId(), block, player.getInventory(),logfile,deathCause);
    }

    /**
     * Creates a new AngelChest
     * @param player Player that this AngelChest belongs to
     * @param owner UUID of the player that this AngelChest belongs to
     * @param block Block where the AngelCHest should be created
     * @param playerItems The player's inventory
     * @param logfile Name of the logfile for this AngelChest
     */
    public AngelChest(Player player, UUID owner, Block block, PlayerInventory playerItems, String logfile, DeathCause deathCause) {

        main = Main.getInstance();
        main.debug("Creating AngelChest natively for player "+player.getName());

        this.main = Main.getInstance();
        this.owner = owner;
        this.block = block;
        this.logfile = logfile;
        this.openedBy = new ArrayList<>();
        this.price = main.groupUtils.getSpawnPricePerPlayer(player);
        this.isProtected = main.getServer().getPlayer(owner).hasPermission("angelchest.protect");
        this.secondsLeft = main.groupUtils.getDurationPerPlayer(main.getServer().getPlayer(owner));
        this.deathCause = deathCause;
        if(secondsLeft<=0) infinite = true;

        String inventoryName = main.messages.ANGELCHEST_INVENTORY_NAME.replaceAll("\\{player}", main.getServer().getPlayer(owner).getName());
        overflowInv = Bukkit.createInventory(null, 54, inventoryName);
        createChest(block,player.getUniqueId());

        // Remove curse of vanishing equipment and Minepacks backpacks
        main.debug("===== PLAYER INVENTORY CONTENTS =====");
        for (int i = 0; i<playerItems.getSize();i++) {
            if (Utils.isEmpty(playerItems.getItem(i))) {
                continue;
            }
            main.debug("Slot "+i+": "+playerItems.getItem(i));
            if(toBeRemoved(playerItems.getItem(i))) playerItems.setItem(i,null);
        }
        main.debug("===== PLAYER INVENTORY CONTENTS END =====");

        armorInv = playerItems.getArmorContents();
        storageInv = playerItems.getStorageContents();
        extraInv = playerItems.getExtraContents();

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
                .replaceAll("\\{player}",Bukkit.getOfflinePlayer(owner).getName())
                .replaceAll("\\{x}", String.valueOf(block.getX()))
                .replaceAll("\\{y}", String.valueOf(block.getY()))
                .replaceAll("\\{z}", String.valueOf(block.getZ()))
        ;
    }

    /**
     * Checks whether this AngelChest has been completely looted
     * @return true when AngelChest is empty, otherwise false
     */
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
        if(experience>0) {
            return false;
        }
        return true;
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

    /**
     * Sets the block at the location to the correct material
     * @param block The block where the AngelChest is spawned
     * @param uuid The owner's UUID (to correctly set player heads)
     */
    public void createChest(Block block, UUID uuid) {
        main.debug("Attempting to create chest with material " + main.chestMaterial.name() + " at "+block.getLocation().toString());
        block.setType(main.chestMaterial);

        // Material is PLAYER_HEAD, so either use the custom texture, or the player skin's texture
        if(main.chestMaterial.name().equalsIgnoreCase("PLAYER_HEAD")) {
            if(Material.getMaterial("PLAYER_HEAD") == null) {
                main.getLogger().warning("Using a custom PLAYER_HEAD as chest material is NOT SUPPORTED in versions < 1.13. Consider using another chest material.");
            } else {
                Skull state = (Skull) block.getState();
                /*ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                String skullName = main.getConfig().getString("custom-head");*/

                // Use the player skin's texture
                if(main.getConfig().getBoolean(Config.HEAD_USES_PLAYER_NAME)) {
                    main.debug("Player head = username");
                    OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
                    state.setOwningPlayer(player);
                    state.update();
                }
                // Use a predefined texture
                else {
                    main.debug("Player head = base64");
                    String base64 = main.getConfig().getString(Config.CUSTOM_HEAD_BASE64);
                    GameProfile profile = new GameProfile(UUID.randomUUID(), "");
                    profile.getProperties().put("textures", new Property("textures", base64));

                    //Field profileField = null;
                    try {
                        // Not needed anymore but please don't remove
                        /*profileField = state.getClass().getDeclaredField("profile");
                        profileField.setAccessible(true);
                        profileField.set(state, profile);*/

                        // Some reflection because Spigot cannot place ItemStacks in the world, which ne need to keep the SkullMeta

                        Object nmsWorld = block.getWorld().getClass().getMethod("getHandle").invoke(block.getWorld());

                        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                        Class<?> blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
                        Class<?> tileEntityClass = Class.forName("net.minecraft.server." + version + ".TileEntitySkull");


                        Constructor<?> cons = blockPositionClass.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
                        Object blockPosition = cons.newInstance(block.getX(), block.getY(), block.getZ());

                        Method getTileEntity = nmsWorld.getClass().getMethod("getTileEntity", blockPositionClass);
                        Object tileEntity = tileEntityClass.cast(getTileEntity.invoke(nmsWorld, blockPosition));

                        tileEntityClass.getMethod("setGameProfile", GameProfile.class).invoke(tileEntity, profile);

                    } catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException | InstantiationException e) {
                        main.getLogger().warning("Could not set custom base64 player head.");
                    }

                }

                //state.set);
                //state.update();

            }
        }
        createHologram(block, uuid);
    }

    /**
     * Removes the chest block from the world and displays explosion particles.
     * @param block Block where the AngelChest was spawned
     */
    public void destroyChest(Block block) {
        main.debug("Destroying chest at "+block.getLocation()+toString());
        block.setType(Material.AIR);
        block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1);
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
        yaml.set("worldid", block.getLocation().getWorld().getUID().toString());
        yaml.set("x", block.getX());
        yaml.set("y", block.getY());
        yaml.set("z", block.getZ());

        yaml.set("infinite",infinite);
        yaml.set(ChestYaml.OWNER_UUID, owner.toString());
        yaml.set("isProtected", isProtected);
        yaml.set("secondsLeft", secondsLeft);
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
                            .replaceAll("\\{currency}", CommandUtils.getCurrency(price, main))
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

        int currentChestId = Utils.getAllAngelChestsFromPlayer(Bukkit.getOfflinePlayer(owner)).indexOf(this)+1;
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
                .replaceAll("\\{player}",main.getServer().getOfflinePlayer(uuid).getName())
                .replaceAll("\\{deathcause}",deathCause.getText());
		hologram = new Hologram(block, hologramText, this);
	}

}