package de.jeff_media.angelchest.npc;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.utils.NMSUtils;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.UUID;


public class NPC {

    private final Location location;
    private final Location bed;
    private final Main main = Main.getInstance();
    private final String name;
    private final EntityPlayer npc;
    private final UUID uuid;

    public NPC(Player player, String name, Location location, String skin, String signature) {

        System.out.println("Creating NPC");
        System.out.println("Skin: " + skin);
        System.out.println("Signature: " + signature);

        this.location = location;
        location.add(0,-0.1,0);
        this.uuid = player.getUniqueId();
        this.name = name;

        //String skin[] = {defaultSkin, defaultSignature};

        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", new Property("textures", skin, signature));

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        npc = new EntityPlayer(server, server.getWorldServer(World.OVERWORLD), profile, new PlayerInteractManager(server.getWorldServer(World.OVERWORLD)));
        npc.setPosition(location.getX(), location.getY(), location.getZ());
        bed = location.add(1, 0, 0);
        npc.entitySleep(new BlockPosition(bed.getX(), bed.getY(), bed.getZ()));

        new KeepAliveTask().runTask(main);
    }

    public org.bukkit.World getWorld() {
        return location.getWorld();
    }

    private void showToAllPlayers() {
        for (Player otherPlayer : location.getWorld().getPlayers()) {
            showToPlayer(otherPlayer);
        }
    }

    public void showToPlayer(Player otherPlayer) {
        try {

            System.out.println("Showing NPC " + npc.getId() + " to player " + otherPlayer.getName());

            PlayerConnection playerConnection = (PlayerConnection) NMSUtils.getConnection(otherPlayer);

            // Invisible name start
            ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), npc.getName());
            team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
            playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
            playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
            playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, new ArrayList<String>(){{add(npc.getName());}}, 3));
            // Invisible name end

            playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
            playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            playerConnection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), false));
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), ()->((CraftPlayer) otherPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc)), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class KeepAliveTask extends BukkitRunnable {
        @Override
        public void run() {
            showToAllPlayers();
        }
    }


}

