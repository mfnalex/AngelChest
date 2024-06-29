package de.jeff_media.angelchest.data;

import com.jeff_media.jefflib.InventoryUtils;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static de.jeff_media.angelchest.utils.CommandUtils.payMoney;

@Data
public class Transaction {

    public static final Transaction EMPTY = new Transaction(0, null);

    private final double money;
    private final ItemStack item;

    public void refund(Player player) {
        if(isEmpty()) return;

        if(money > 0) {
            payMoney(player, money, "Refund for AngelChest");
        }
        if(item != null) {
            InventoryUtils.addOrDrop(player, item);
        }
    }

    public boolean isEmpty() {
        return money == 0 && item == null;
    }
}
