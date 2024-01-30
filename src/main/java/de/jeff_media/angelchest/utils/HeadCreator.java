package de.jeff_media.angelchest.utils;

import com.jeff_media.jefflib.SkullUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public final class HeadCreator {

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
    private static String defaultBase64 = "SSBkb24ndCBsaWtlIHBlb3BsZSB3aG8gdHJ5IHRvIHN0ZWFsIG15IHBsdWdpbnMuIEl0J3Mgb3BlbiBzb3VyY2UgYnJvLCBzbyB3aHkgZG8geW91IGV2ZW4gYm90aGVyIGFib3V0IHNlbmRpbmcgdGhlIC5qYXIgZmlsZSBhd2F5PyBBdCBsZWFzdCBoYXZlIG90aGVyIHBlb3BsZSBjb21waWxlIGl0IHRoZW1zZWx2ZXMuLi4=eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90Z%%__USER__%%Xh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThiYzhmYTcxNmN%%__NONCE__%%hZGQwMDRiODI4Y2IyN2NjMGY2ZjZhZGUzYmU0MTUxMTY4OGNhOWVjZWZmZDE2NDdmYjkifX19SSBkb24ndCBsaWtlIHBlb3BsZSB3aG8gdHJ5IHRvIHN0ZWFsIG15IHBsdWdpbnMuIEl0J3Mgb3BlbiBzb3VyY2UgYnJvLCBzbyB3aHkgZG8geW91IGV2ZW4gYm90aGVyIGFib3V0IHNlbmRpbmcgdGhlIC5qYXIgZmlsZSBhd2F5PyBBdCBsZWFzdCBoYXZlIG90aGVyIHBlb3BsZSBjb21waWxlIGl0IHRoZW1zZWx2ZXMuLi4=";

    public static void createHeadInWorld(final Block block, final UUID uuid) {
        final AngelChestMain main = AngelChestMain.getInstance();

        if (main.isOutsideOfNormalWorld(block)) return;

        if (!(block.getState() instanceof Skull)) {
            main.getLogger().severe("Could not spawn head at " + block);
            return;
        }

        final Skull state = (Skull) block.getState();

        // Use the player skin's texture
        if (main.getConfig().getBoolean(Config.HEAD_USES_PLAYER_NAME)) {
            if (main.debug) main.debug("Player head = username");
            final OfflinePlayer player = main.getServer().getOfflinePlayer(uuid);
            state.setOwningPlayer(player);
            state.update();
        }
        // Use a predefined texture
        else {
            if (main.debug) main.debug("Player head = base64");
            final String base64 = main.getConfig().getString(Config.CUSTOM_HEAD_BASE64);
            final GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", base64));

            SkullUtils.setHeadTexture(block, profile);

        }
    }

    public static ItemStack getHead(final String base64) {

//        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
//        final SkullMeta meta = (SkullMeta) head.getItemMeta();
//        final GameProfile profile = new GameProfile(UUID.randomUUID(), "");
//        profile.getProperties().put("textures", new Property("textures", base64));
//        final Field profileField;
//        try {
//            profileField = meta.getClass().getDeclaredField("profile");
//            profileField.setAccessible(true);
//            profileField.set(meta, profile);
//        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
//            e.printStackTrace();
//            return new ItemStack(Material.PLAYER_HEAD);
//        }
//
//        head.setItemMeta(meta);
//        return head;
        return SkullUtils.getHead(base64);
    }

    @SuppressWarnings("unused")
    public static ItemStack getHead() {
        return getHead(defaultBase64);
    }

    public static ItemStack getPlayerHead(final UUID uuid) {
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta skullMeta = (SkullMeta) (head.hasItemMeta() ? head.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD));
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        head.setItemMeta(skullMeta);
        return head;
    }
}
