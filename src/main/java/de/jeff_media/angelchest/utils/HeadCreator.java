package de.jeff_media.angelchest.utils;

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
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
            profile.properties().put("textures", new Property("textures", base64));

            final Skull skullState = (Skull) block.getState();
            final PlayerProfile playerProfile = Bukkit.createPlayerProfile(profile.id(), profile.name());
            final PlayerTextures textures = playerProfile.getTextures();
            final String textureUrl = extractTextureUrl(base64);
            if (textureUrl != null) {
                try {
                    textures.setSkin(new URL(textureUrl));
                    playerProfile.setTextures(textures);
                    skullState.setOwnerProfile(playerProfile);
                    skullState.update();
                } catch (MalformedURLException ignored) {
                    main.getLogger().warning("Invalid custom head texture URL");
                }
            }

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
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;
        final String textureUrl = extractTextureUrl(base64);
        if (textureUrl != null) {
            try {
                final PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                final PlayerTextures textures = profile.getTextures();
                textures.setSkin(new URL(textureUrl));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (MalformedURLException ignored) {
            }
        }
        head.setItemMeta(meta);
        return head;
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

    private static String extractTextureUrl(final String base64) {
        try {
            final String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            final String marker = "\"url\":\"";
            final int urlStart = decoded.indexOf(marker);
            if (urlStart < 0) return null;
            final int valueStart = urlStart + marker.length();
            final int valueEnd = decoded.indexOf('"', valueStart);
            return valueEnd < 0 ? null : decoded.substring(valueStart, valueEnd);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
