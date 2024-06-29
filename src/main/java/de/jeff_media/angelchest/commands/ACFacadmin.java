package de.jeff_media.angelchest.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.aikar.commands.ACFBukkitUtil.sendMsg;

@CommandAlias("acadmin")
@CommandPermission("angelchest.admin")
public class ACFacadmin extends BaseCommand {

    private static final AngelChestMain main = AngelChestMain.getInstance();
    private static final Logger logger = main.getLogger();

    /*@Subcommand("applytag")
    @CommandCompletion("@items")
    public static void setItem(Player player, String itemId) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null | item.getType().isAir() || item.getAmount() == 0) {
            player.sendMessage("§cYou must hold an item in your main hand to run this command.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PDCUtils.set(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING, ); // TODO: Make it work lol
    }*/

    @Subcommand("saveitem")
    @Syntax("<itemname>")
    public static void onSave(Player player, String itemName) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType().isAir() || item.getAmount() == 0) {
            player.sendMessage("§cYou must hold an item in your main hand.");
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "items.yml"));
        yaml.set(itemName + ".exact",item);
        player.sendMessage("§aSaved your currently held item as §6" + itemName + " §ato items.yml.");
        try {
            yaml.save(new File(main.getDataFolder(), "items.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConfigUtils.reloadCompleteConfig(true);
    }

    @Subcommand("giveitem")
    @CommandCompletion("@items @players @range:1-64")
    @Syntax("<item> [player] [amount]")
    public static void onDefault(CommandSender sender, String itemName, @Optional String playerName, @Optional Integer amount) {
        Player player = null;
        int finalAmount = 1;
        if (amount != null) finalAmount = amount;
        if (playerName == null && sender instanceof Player) {
            player = (Player) sender;
        }
        if (playerName == null && !(sender instanceof Player)) {
            sender.sendMessage("§cYou must specify a player.");
            return;
        }
        if (playerName != null) {
            player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(String.format(main.messages.MSG_UNKNOWN_PLAYER, playerName));
                return;
            }
        }

        ItemStack item = main.getItemManager().getItem(itemName);
        if (item == null) {
            sender.sendMessage("§cUnknown AngelChest item: §f" + itemName);
            return;
        }

        item.setAmount(finalAmount);

        giveItem(player, item);

        String name = itemName;
        if (item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName() != null
                && !item.getItemMeta().getDisplayName().isEmpty()) {
            name = item.getItemMeta().getDisplayName();
        }

        sender.sendMessage("§aGave §r" + finalAmount + "x " + name + "§r§a to " + player.getName());
    }

    private static void giveItem(Player player, ItemStack item) {
        int amount = item.getAmount();
        while (amount > 0) {

            int droppingNowAmount = amount;
            if(droppingNowAmount > item.getType().getMaxStackSize()) {
                droppingNowAmount = item.getType().getMaxStackSize();
            }
            amount -= droppingNowAmount;

            ItemStack toDrop = item.clone();
            toDrop.setAmount(droppingNowAmount);

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(toDrop);
            for (ItemStack leftover : leftovers.values()) {
                player.getLocation().getWorld().dropItemNaturally(player.getLocation(), leftover);
            }

        }
    }

    @Subcommand("open")
    @CommandCompletion("@onlinePlayerNames @offlinePlayerNamesWithChests @chestsBySecondArg @truefalse")
    @Syntax("<player> <owner> <chestId> <preview>")
    public void open(CommandSender sender, String player, String owner, int chestId, boolean preview) {
        if(player == null) {
            sender.sendMessage("§cEnter a valid player name.");
            return;
        }
        Player aPlayer = Bukkit.getPlayer(player);
        if(aPlayer == null) {
            sender.sendMessage("§cPlayer not found: " + player);
            return;
        }
        if(owner == null) {
            sender.sendMessage("§cEnter a valid owner name.");
            return;
        }
        AngelChest someChestFromThisPlayer = main.getAllAngelChests().stream().filter(ac -> {
            String name = ac.getPlayer().getName();
            if(name == null) return false;
            return name.equalsIgnoreCase(owner);
        }).findFirst().orElse(null);
        if(someChestFromThisPlayer == null) {
            sender.sendMessage("§cOwner not found, or they don't have any AngelChests: " + owner);
            return;
        }
        OfflinePlayer aOwner = someChestFromThisPlayer.getPlayer();
        LinkedHashSet<AngelChest> chests = main.getAllAngelChestsFromPlayer(aOwner);
        chestId--;
        if(chestId < 0 || chestId >= chests.size()) {
            sender.sendMessage("§cInvalid chest ID.");
            return;
        }
        de.jeff_media.angelchest.data.AngelChest chest = (de.jeff_media.angelchest.data.AngelChest) chests.toArray()[chestId];
        main.guiManager.showPreviewGUI(aPlayer, chest, preview, false);
    }

    @HelpCommand
    @Default
    @Syntax("")
    public void doHelp(CommandSender sender, CommandHelp help) {
        sendMsg(sender, "§6AngelChest Admin Commands:");
        help.showHelp();
    }

    @Subcommand("custommodeldataset")
    public void setCustomModelData(Player player, int modelData) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType().isAir() || item.getAmount() == 0 || item.getItemMeta() == null) {
            player.sendMessage("§cYou must hold an item in your main hand to run this command.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
        player.sendMessage("§aSet CustomModelData to §6" + modelData + "§a.");
    }

    @Subcommand("custommodeldataremove")
    public void removeCustomModelData(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType().isAir() || item.getAmount() == 0 || item.getItemMeta() == null) {
            player.sendMessage("§cYou must hold an item in your main hand to run this command.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(null);
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
        player.sendMessage("§aRemoved CustomModelData.");
    }

    @Subcommand("custommodeldataget")
    public void getCustomModelData(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType().isAir() || item.getAmount() == 0 || item.getItemMeta() == null) {
            player.sendMessage("§cYou must hold an item in your main hand to run this command.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta.hasCustomModelData()) {
            player.sendMessage("§aCustomModelData: §6" + meta.getCustomModelData());
        } else {
            player.sendMessage("§aNo CustomModelData found.");
        }
    }

    private static CompletableFuture<OfflinePlayer> getPlayer(String name) {
        return CompletableFuture.supplyAsync(() -> {
            Player exact = Bukkit.getPlayerExact(name);
            if (exact != null) return exact;
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            if (offline.hasPlayedBefore()) return offline;
            return null;
        });
    }

    // CHARGES

    @Subcommand("charges set")
    @CommandCompletion("@players")
    public void setCharges(CommandSender sender, String player, int charges) {
        getPlayer(player).thenAcceptAsync(offlinePlayer -> {
            if(offlinePlayer == null) {
                sender.sendMessage("§cPlayer not found: " + player);
                return;
            }
            sender.sendMessage("§aSet charges for §e" + player + "§a to §e" + charges);
            main.getChargesManager().setRemainingCharges(offlinePlayer.getUniqueId(), charges).join();
        }).exceptionally(ex -> {
            logger.log(Level.SEVERE, "Error while setting charges", ex);
            sender.sendMessage("§cAn error occurred while setting charges: " + ex.getMessage());
            return null;
        });
    }

    @Subcommand("charges get")
    @CommandCompletion("@players")
    public void getCharges(CommandSender sender, String player) {
        getPlayer(player).thenAcceptAsync(offlinePlayer -> {
            if(offlinePlayer == null) {
                sender.sendMessage("§cPlayer not found: " + player);
                return;
            }
            int charges = main.getChargesManager().getRemainingCharges(offlinePlayer.getUniqueId()).join();
            sender.sendMessage("§e" + player + "§a has §e" + charges + "§a charges.");
        }).exceptionally(ex -> {
            logger.log(Level.SEVERE, "Error while getting charges", ex);
            sender.sendMessage("§cAn error occurred while getting charges: " + ex.getMessage());
            return null;
        });
    }

    @Subcommand("charges add")
    @CommandCompletion("@players")
    public void addCharges(CommandSender sender, String player, int charges) {
        getPlayer(player).thenAcceptAsync(offlinePlayer -> {
            if(charges < 0) {
                sender.sendMessage("§cCharges must be positive.");
                return;
            }
            if(offlinePlayer == null) {
                sender.sendMessage("§cPlayer not found: " + player);
                return;
            }
            int newTotal = main.getChargesManager().addCharges(offlinePlayer.getUniqueId(), charges).join();
            sender.sendMessage("§aAdded §e" + charges + "§a charges to §e" + player + "§a. New total: §e" + newTotal);
        }).exceptionally(ex -> {
            logger.log(Level.SEVERE, "Error while adding charges", ex);
            sender.sendMessage("§cAn error occurred while adding charges: " + ex.getMessage());
            return null;
        });
    }

    @Subcommand("charges remove")
    @CommandCompletion("@players")
    public void removeCharges(CommandSender sender, String player, int charges) {
        getPlayer(player).thenAcceptAsync(offlinePlayer -> {
            if(charges < 0) {
                sender.sendMessage("§cCharges must be positive.");
                return;
            }
            if(offlinePlayer == null) {
                sender.sendMessage("§cPlayer not found: " + player);
                return;
            }
            int newTotal = main.getChargesManager().removeCharges(offlinePlayer.getUniqueId(), charges).join();
            sender.sendMessage("§cRemoved §e" + charges + "§a charges from §e" + player + "§a. New total: §e" + newTotal);
        }).exceptionally(ex -> {
            logger.log(Level.SEVERE, "Error while adding charges", ex);
            sender.sendMessage("§cAn error occurred while removing charges: " + ex.getMessage());
            return null;
        });
    }
}
