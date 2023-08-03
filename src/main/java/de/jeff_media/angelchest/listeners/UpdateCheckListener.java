//package de.jeff_media.angelchest.listeners;
//
//import de.jeff_media.angelchest.AngelChestMain;
//import org.bukkit.ChatColor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.jetbrains.annotations.NotNull;
//
//public class UpdateCheckListener implements @NotNull Listener {
//
//    private final AngelChestMain main;
//
//    public UpdateCheckListener() {
//        this.main = AngelChestMain.getInstance();
//    }
//
//    @EventHandler
//    public void onUpdateCheckFinishedEvent(final UpdateCheckFinishedEvent event) {
//        if (event.getRequester() == null) return;
//        final CommandSender sender = event.getRequester();
//        if (!event.getPlugin().equals(main)) return;
//        /*if(!event.isNewVersionAvailable()) {
//            Messages.send(sender,("§aYou are running the latest version of §6"+main.getDescription().getName());
//            return;
//        }*/
//        if (sender instanceof Player) {
//            final Player player = (Player) sender;
//            main.updateChecker.sendUpdateMessageToPlayer(player);
//
//            if (!event.isNewVersionAvailable()) {
//                Messages.send(player,ChatColor.GREEN + "You are running the latest version of " + ChatColor.GOLD + main.getDescription().getName() + ChatColor.GREEN + ".");
//            }
//
//        }
//        //main.updateChecker.printCheckResultToConsole();
//
//    }
//}
