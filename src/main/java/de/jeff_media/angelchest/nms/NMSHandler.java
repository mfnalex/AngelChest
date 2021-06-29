package de.jeff_media.angelchest.nms;

import com.mojang.authlib.GameProfile;
import de.jeff_media.angelchest.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NMSHandler {

    private static final Main main = Main.getInstance();
    static AbstractNMSHandler instance;

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
        if (instance == null) return false;

        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        assert meta != null;
        meta.setCustomModelData(customModelData);
        totem.setItemMeta(meta);
        ItemStack hand = p.getInventory().getItemInMainHand();
        p.getInventory().setItemInMainHand(totem);
        instance.playTotemAnimation(p);
        p.getInventory().setItemInMainHand(hand);

        return true;
    }

    public static void createHeadInWorld(final Block block, final GameProfile profile) {
        instance.createHeadInWorld(block, profile);
    }
}
