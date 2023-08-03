//package de.jeff_media.angelchest.npc;
//
//import de.jeff_media.angelchest.AngelChestMain;
//import org.bukkit.Location;
//import org.bukkit.World;
//import org.bukkit.entity.Player;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//public class NPCManager {
//
//    private final Set<NPC> npcs = new HashSet<>();
//    private final AngelChestMain main = AngelChestMain.getInstance();
//
//    public NPCManager() {
//
//    }
//
//    public Set<NPC> getNPCs() {
//        return npcs;
//    }
//
//    public NPC createNPC(Player player, Location location) {
//        String skin = main.getSkinManager().getSkin(player);
//        String signature = main.getSkinManager().getSignature(player);
//        NPC npc = new NPC(player, "AngelChestNPC", location, skin, signature);
//        npcs.add(npc);
//        return npc;
//    }
//}
