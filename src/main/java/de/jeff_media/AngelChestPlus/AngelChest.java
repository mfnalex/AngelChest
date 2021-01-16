package de.jeff_media.AngelChestPlus;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jeff_media.AngelChestPlus.utils.CommandUtils;
import de.jeff_media.AngelChestPlus.utils.Utils;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class AngelChest {

    public ItemStack[] armorInv;
    public ItemStack[] storageInv;
    public ItemStack[] extraInv;
    public Inventory overflowInv;
    boolean success = true;
    public Block block;
    public UUID worldid;
    public UUID owner;
    public Hologram hologram;
    public boolean isProtected;
    //long configDuration;
    //long taskStart;
    public int secondsLeft;
    public int experience = 0;
    public int levels = 0;
    double price = 0;
    public boolean infinite = false;
    public Main main;


    public @Nullable AngelChest(File file, Main main) {
        main.debug("Creating AngelChest from file " + file.getName());
        YamlConfiguration yaml;
        try {
            yaml = loadYaml(file);
        } catch (Throwable t) {
            main.getLogger().warning("Could not load legacy AngelChest file " + file.getName());
            success = false;
            t.printStackTrace();
            return;
        }

        this.main = main;
        this.owner = UUID.fromString(yaml.getString("owner"));
        this.levels = yaml.getInt("levels", 0);
        this.isProtected = yaml.getBoolean("isProtected");
        this.secondsLeft = yaml.getInt("secondsLeft");
        this.infinite = yaml.getBoolean("infinite",false);
        this.price = yaml.getDouble("price", main.getConfig().getDouble(Config.PRICE));

        // Check if this is the current save format
        int saveVersion = yaml.getInt("angelchest-saveversion", 1);
        if (saveVersion == 1) {
            try {
                this.block = yaml.getLocation("block").getBlock();
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
        for (ItemStack is : yaml.getList("overflowInv").toArray(new ItemStack[0])) {
            if (is != null) overflowInv.setItem(iOverflow, is);
            iOverflow++;
        }

        // Load ArmorInv
        armorInv = new ItemStack[4];
        int iArmor = 0;
        for (ItemStack is : yaml.getList("armorInv").toArray(new ItemStack[0])) {
            if (is != null) armorInv[iArmor] = is;
            iArmor++;
        }

        // Load StorageInv
        storageInv = new ItemStack[36];
        int iStorage = 0;
        for (ItemStack is : yaml.getList("storageInv").toArray(new ItemStack[0])) {
            if (is != null) storageInv[iStorage] = is;
            iStorage++;
        }

        // Load ExtraInv
        extraInv = new ItemStack[1];
        int iExtra = 0;
        for (ItemStack is : yaml.getList("extraInv").toArray(new ItemStack[0])) {
            if (is != null) extraInv[iExtra] = is;
            iExtra++;
        }

        file.delete();
    }

    public AngelChest(Player p, Block block, Main main) {
    	this(p, p.getUniqueId(), block, p.getInventory(), main);
    }


    public AngelChest(Player p, UUID owner, Block block, PlayerInventory playerItems, Main main) {

        main.debug("Creating AngelChest natively for player "+p.getName());

        this.main = main;
        this.owner = owner;
        this.block = block;
        this.price = main.getConfig().getDouble(Config.PRICE);
        this.isProtected = main.getServer().getPlayer(owner).hasPermission("angelchest.protect");
        this.secondsLeft = main.groupUtils.getDurationPerPlayer(main.getServer().getPlayer(owner));
        if(secondsLeft<=0) infinite = true;

        String inventoryName = main.messages.ANGELCHEST_INVENTORY_NAME.replaceAll("\\{player}", main.getServer().getPlayer(owner).getName());
        overflowInv = Bukkit.createInventory(null, 54, inventoryName);
        createChest(block,p.getUniqueId());

        // Remove curse of vanishing equipment and Minepacks backpacks
        for (int i = 0; i<playerItems.getSize();i++) {
            if (Utils.isEmpty(playerItems.getItem(i))) {
                continue;
            }
            main.debug("Slot "+i+": "+playerItems.getItem(i));
            if(toBeRemoved(playerItems.getItem(i))) playerItems.setItem(i,null);
        }

        armorInv = playerItems.getArmorContents();
        storageInv = playerItems.getStorageContents();
        extraInv = playerItems.getExtraContents();

        removeKeepedItems();
    }


    private void removeKeepedItems() {

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

    private boolean toBeRemoved(ItemStack i) {
        if(i==null) return false;
        if(main.getConfig().getBoolean(Config.REMOVE_CURSE_OF_VANISHING)
                && i.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
            return true;
        }
        if(main.getConfig().getBoolean(Config.REMOVE_CURSE_OF_BINDING)
                && i.getEnchantments().containsKey(Enchantment.BINDING_CURSE)) {
            return true;
        }
        if (main.minepacksHook.isMinepacksBackpack(i, main)) {
            return true;
        }


        //if(m.hasLore() && m.getLore().contains)


        return false;
    }

    private YamlConfiguration loadYaml(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    // Creates a physcial chest
    public void createChest(Block block, UUID uuid) {
        main.debug("Attempting to create chest with material " + main.chestMaterial.name() + " at "+block.getLocation().toString());
        block.setType(main.chestMaterial);
        if(main.chestMaterial.name().equalsIgnoreCase("PLAYER_HEAD")) {
            if(Material.getMaterial("PLAYER_HEAD") == null) {
                main.getLogger().warning("Using a custom PLAYER_HEAD as chest material is NOT SUPPORTED in versions < 1.13. Consider using another chest material.");
            } else {
                Skull state = (Skull) block.getState();
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                String skullName = main.getConfig().getString("custom-head");

                if(main.getConfig().getBoolean(Config.HEAD_USES_PLAYER_NAME)) {
                    main.debug("Player head = username");
                    OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
                    state.setOwningPlayer(player);
                    state.update();
                } else {
                    main.debug("Player head = base64");
                    String base64 = main.getConfig().getString(Config.CUSTOM_HEAD_BASE64);
                    GameProfile profile = new GameProfile(UUID.randomUUID(), "");
                    profile.getProperties().put("textures", new Property("textures", base64));

                    Field profileField = null;
                    try {
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
        createHologram(main, block, uuid);
    }

    // Destroys a physical chest
    public void destroyChest(Block b) {
        main.debug("Destroying chest at "+b.getLocation()+toString());
        b.setType(Material.AIR);
        b.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, b.getLocation(), 1);
        hologram.destroy();
    }

    public void unlock() {
        this.isProtected = false;
    }

    public File saveToFile(boolean removeChest) {
        File yamlFile = new File(main.getDataFolder() + File.separator + "angelchests",
                this.hashCode() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
        yaml.set("angelchest-saveversion", 2);
        yaml.set("worldid", block.getLocation().getWorld().getUID().toString());
        //yaml.set("block", block.getLocation());
        yaml.set("x", block.getX());
        yaml.set("y", block.getY());
        yaml.set("z", block.getZ());
        yaml.set("infinite",infinite);
        yaml.set("owner", owner.toString());
        yaml.set("isProtected", isProtected);
        //yaml.set("configDuration", configDuration);
        //yaml.set("taskStart", taskStart);
        yaml.set("secondsLeft", secondsLeft);
        yaml.set("experience", experience);
        yaml.set("levels", levels);
        yaml.set("price",price);
        yaml.set("storageInv", storageInv);
        yaml.set("armorInv", armorInv);
        yaml.set("extraInv", extraInv);
        yaml.set("overflowInv", overflowInv.getContents());

        if(removeChest) {
            // Duplicate Start
            block.setType(Material.AIR);
        /*for (UUID uuid : hologram.armorStandUUIDs) {
            if (main.getServer().getEntity(uuid) != null) {
                main.getServer().getEntity(uuid).remove();
            }
        }
        for (ArmorStand armorStand : hologram.getArmorStands()) {
            if (armorStand == null) continue;
            armorStand.remove();
        }*/
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

        /*for(UUID uuid : hologram.armorStandUUIDs) {
            if(Bukkit.getEntity(uuid)!=null) {
                Bukkit.getEntity(uuid).remove();
            }
        }*/
        hologram.destroy();

        // drop contents
        Utils.dropItems(block, armorInv);
        Utils.dropItems(block, storageInv);
        Utils.dropItems(block, extraInv);
        Utils.dropItems(block, overflowInv);

        if (experience > 0) {
            Utils.dropExp(block, experience);
        }

        if(refund
                && main.getConfig().getBoolean(Config.REFUND_EXPIRED_CHESTS)
                && main.getConfig().getDouble(Config.PRICE) > 0) {
            CommandUtils.payMoney(Bukkit.getOfflinePlayer(owner),price, main,"AngelChest expired");
        }

        int currentChestId = Utils.getAllAngelChestsFromPlayer(Bukkit.getOfflinePlayer(owner),main).indexOf(this)+1;
        Player player = Bukkit.getPlayer(owner);
        if(player != null && player.isOnline()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> main.guiManager.updateGUI(player, currentChestId), 1L);
        }

    }

    public void remove() {
        main.debug("Removing AngelChest");
        main.angelChests.remove(block);
    }

	public void createHologram(Main main, Block block, UUID uuid) {
		//String hologramText = String.format(plugin.messages.HOLOGRAM_TEXT, plugin.getServer().getOfflinePlayer(uuid).getName());
        String hologramText = main.messages.HOLOGRAM_TEXT
                .replaceAll("\\{player}",main.getServer().getOfflinePlayer(uuid).getName());
		hologram = new Hologram(block, hologramText, main,this);
	}

}