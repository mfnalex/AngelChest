package de.jeff_media.angelchest.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.jeff_media.angelchest.Main;
import de.jeff_media.daddy.Chicken;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@CommandAlias("acadmin")
@CommandPermission("angelchest.admin")
public class ACFacadmin extends BaseCommand {

    private static final Main main = Main.getInstance();

    static {
        Chicken.wing(main);
    }

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

    @Subcommand("giveitem")
    @CommandCompletion("@items @players @range:1-64")
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

        sender.sendMessage("§aGave §r" + amount + "x " + name + "§r§a to " + player.getName());
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
}
