package de.jeff_media.angelchest.hooks;

import com.jeff_media.jefflib.PDCUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class MoneyhuntersHook {

    //private static final NamespacedKey MONEY_CURRENCY_KEY = Objects.requireNonNull(NamespacedKey.fromString("moneyhunters:money.currency" ));
    private static final NamespacedKey MONEY_AMOUNT_KEY = Objects.requireNonNull(PDCUtils.getKeyFromString("moneyhunters","money.amount" ));
    //private static final NamespacedKey MONEY_ID_KEY = Objects.requireNonNull(NamespacedKey.fromString("moneyhunters:money.id"));

    public static boolean isMoneyHuntersItem(ItemStack item) {
        if(item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(MONEY_AMOUNT_KEY, PersistentDataType.DOUBLE);
    }



}
