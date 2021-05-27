package de.jeff_media.angelchest.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.utils.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class SkinManager {

    private final HashMap<UUID, String> skins = new HashMap<>();
    private final HashMap<UUID, String> signatures = new HashMap<>();
    private final Main main = Main.getInstance();

    public SkinManager() {
        if(Bukkit.getOnlinePlayers().isEmpty()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            register(player.getUniqueId());
        }
    }

    private static String insertDashUUID(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(13, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(18, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(23, "-");

        return sb.toString();
    }

    private static UUID getUUIDFromMojang(String username) throws IOException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        String answer = content.toString().replace("\n","");
        String uuid = answer.split("\"id\":\"")[1].split("\"")[0];
        return UUID.fromString(insertDashUUID(uuid));
    }

    private static String getJsonFromMojang(UUID uuid) throws IOException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        String answer = content.toString().replace("\n","");
        return answer;
    }

    public void registerLazy(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(main, ()->register(uuid));
    }

    private void registerFromProfile(UUID uuid) {
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            GameProfile profile = NMSUtils.getGameProfile(player);
            Property property = profile.getProperties().get("textures").iterator().next();
            String skin = property.getValue();
            String signature = property.getSignature();
            skins.put(uuid, skin);
            signatures.put(uuid, signature);
        } catch(Exception ignored) {
            System.out.println("Could not get skin from Profile");
        }
    }

    // TODO: Add fallback skin / signature when offline and running in offline mode
    private void register(UUID uuid) {
        UUID offlineUUID = uuid;
        registerFromProfile(uuid);
        if(skins.containsKey(uuid) && signatures.containsKey(uuid)) return;
        System.out.println("Getting skin online...");
        if(!Bukkit.getServer().getOnlineMode()) {
            System.out.println("Server is running in offline mode, fixing UUID...");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            System.out.println("Offline UUID: " + uuid);
            try {
                uuid = getUUIDFromMojang(offlinePlayer.getName());
                System.out.println("Online UUID: " + uuid);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        try {
            String answer = getJsonFromMojang(uuid);
            String skin = answer.split("\"value\" : \"")[1].split("\"")[0];
            String signature = answer.split("\"signature\" : \"")[1].split("\"")[0];
            skins.put(offlineUUID, skin);
            signatures.put(offlineUUID, signature);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public String getSkin(OfflinePlayer player) {
        return getSkin(player.getUniqueId());
    }

    public String getSignature(OfflinePlayer player) {
        return getSignature(player.getUniqueId());
    }

    public String getSkin(UUID uuid) {
        if(!skins.containsKey(uuid)) register(uuid);
        return skins.get(uuid);
    }

    public String getSignature(UUID uuid) {
        if(!signatures.containsKey(uuid)) register(uuid);
        return signatures.get(uuid);
    }

}
