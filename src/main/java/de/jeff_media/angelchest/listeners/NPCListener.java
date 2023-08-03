//package de.jeff_media.angelchest.listeners;
//
//import de.jeff_media.angelchest.AngelChestMain;
//import de.jeff_media.angelchest.npc.NPC;
//import org.bukkit.Location;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.block.Action;
//import org.bukkit.event.player.PlayerChangedWorldEvent;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.event.player.PlayerJoinEvent;
//
//public class NPCListener implements Listener {
//
//    private final AngelChestMain main = AngelChestMain.getInstance();
//
//    private void showNPCs(Player player) {
//        for(NPC npc : main.getNpcManager().getNPCs()) {
//            npc.showToPlayer(player);
//        }
//    }
//
//    /*@EventHandler
//    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
//        showNPCs(event.getPlayer());
//    }
//
//    @EventHandler
//    public void onClick(PlayerInteractEvent event) {
//        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
//        Location loc = event.getClickedBlock().getRelative(0,1,0).getLocation();
//        main.getNpcManager().createNPC(event.getPlayer(), loc);
//    }
//
//    @EventHandler
//    public void getSkin(PlayerJoinEvent event) {
//        main.getSkinManager().registerLazy(event.getPlayer().getUniqueId());
//        showNPCs(event.getPlayer());
//    }*/
//}
