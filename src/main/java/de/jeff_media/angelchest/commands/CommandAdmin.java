//package de.jeff_media.angelchest.commands;
//
//import de.jeff_media.angelchest.AngelChestMain;
//import de.jeff_media.angelchest.config.Messages;
//import de.jeff_media.angelchest.config.Permissions;
//import com.jeff_media.jefflib.BlockUtils;
//import org.bukkit.block.Block;
//import org.bukkit.block.data.BlockData;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.command.TabCompleter;
//import org.bukkit.entity.Player;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
///**
// * Handles the /acadmin command
// */
//public final class CommandAdmin implements CommandExecutor, TabCompleter {
//
//    private final AngelChestMain main = AngelChestMain.getInstance();
//
//    private static String[] shift(final String[] args) {
//        return Arrays.stream(args).skip(1).toArray(String[]::new);
//    }
//
//
//    // TODO: Duplicate, move to ConfigUtils
//    private @Nullable List<String> getMatching(final String[] commands, final String entered) {
//        final List<String> list = new ArrayList<>(Arrays.asList(commands));
//        list.removeIf(current -> !current.startsWith(entered));
//        return list;
//    }
//
//    @Override
//    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
//
//        if (!commandSender.hasPermission(Permissions.ADMIN)) {
//            Messages.send(commandSender, main.messages.MSG_NO_PERMISSION);
//            return true;
//        }
//
//        Player player = (Player) commandSender;
//
//        if (args.length > 0) {
//            switch (args[0].toLowerCase()) {
//                case "saveblockdata":
//                    Block block = player.getTargetBlockExact(10);
//                    if (block == null) {
//                        player.sendMessage("§cYou are not looking at a close enough block.");
//                        return true;
//                    }
//                    BlockData data = block.getBlockData();
//                    BlockDataManager.save(data);
//                    List<Map.Entry<String, String>> entries = BlockUtils.getBlockDataAsEntries(block);
//                    player.sendMessage("§aSaved block data for material §b" + data.getMaterial().name()+"§a:");
//                    if(entries.size()==0) {
//                        player.sendMessage("§7  (this material does not have any block data)");
//                        return true;
//                    }
//                    for (Map.Entry<String, String> entry : entries) {
//                        String key = entry.getKey();
//                        String value = entry.getValue();
//                        player.sendMessage("§6  " + key + "§r: §b" + value);
//                    }
//                    Messages.showReloadNotice(player);
//                    return true;
//            }
//        }
//
//        Messages.send(commandSender, "§eAvailable commands:",
//                "/acadmin saveblockdata §6Saves the block data for the block you are looking at");
//
//        return true;
//
//    }
//
//    @Override
//    public @Nullable List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
//        final String[] mainCommands = {"saveblockdata"};
//
//        if (args.length == 0) {
//            return Arrays.asList(mainCommands);
//        }
//        if (args.length == 1) {
//            return getMatching(mainCommands, args[0]);
//        }
//        return null;
//    }
//}
