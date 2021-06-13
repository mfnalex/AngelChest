package de.jeff_media.angelchest.nms;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.config.Config;
import de.jeff_media.angelchest.utils.NMSUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class NMSHandler {

    static AbstractNMSHandler instance;
    private static final Main main = Main.getInstance();

    static {
        init();
    }

    private static void init() {
        try {
            instance = new NMSGeneric();
            main.getLogger().info("Loaded generic NMS handler.");
            return;
        } catch (Throwable ignored) {

        }

        try {
            instance = new NMSLegacy();
            main.getLogger().info("Loaded legacy NMS handler.");
            return;
        } catch (Throwable ignored) {

        }

        main.getLogger().severe("Could not load any NMS handler. Some features might be disabled.");
        instance = null;
    }

    public static boolean playTotemAnimation(Player p, int customModelData) {
        if(instance == null) return false;

        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        meta.setCustomModelData(customModelData);
        totem.setItemMeta(meta);
        ItemStack hand = p.getInventory().getItemInMainHand();
        p.getInventory().setItemInMainHand(totem);
        instance.playTotemAnimation(p);
        p.getInventory().setItemInMainHand(hand);

        return true;

    }
}
