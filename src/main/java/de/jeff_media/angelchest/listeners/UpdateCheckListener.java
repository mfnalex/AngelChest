//package de.jeff_media.angelchest.listeners;
//
//import de.jeff_media.angelchest.Main;
//import org.bukkit.ChatColor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.jetbrains.annotations.NotNull;
//
//public class UpdateCheckListener implements @NotNull Listener {
//
//    private final Main main;
//
//    public UpdateCheckListener() {
//        this.main = Main.getInstance();
//    }
//
//    @EventHandler
//    public void onUpdateCheckFinishedEvent(final UpdateCheckFinishedEvent event) {
//        if (event.getRequester() == null) return;
//        final CommandSender sender = event.getRequester();
//        if (!event.getPlugin().equals(main)) return;
//        /*if(!event.isNewVersionAvailable()) {
//            sender.sendMessage("§aYou are running the latest version of §6"+main.getDescription().getName());
//            return;
//        }*/
//        if (sender instanceof Player) {
//            final Player player = (Player) sender;
//            main.updateChecker.sendUpdateMessageToPlayer(player);
//
//            if (!event.isNewVersionAvailable()) {
//                player.sendMessage(ChatColor.GREEN + "You are running the latest version of " + ChatColor.GOLD + main.getDescription().getName() + ChatColor.GREEN + ".");
//            }
//
//        }
//        //main.updateChecker.printCheckResultToConsole();
//
//    }
//}
