package com.jeff_media.jefflib;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Msg {

    private Msg() {
    }

    public static void send(final CommandSender receiver, final String text) {
        receiver.sendMessage(TextUtils.format(text, receiver instanceof Player ? (OfflinePlayer) receiver : null));
    }

    public static void send(final CommandSender receiver, final String text, final OfflinePlayer player) {
        receiver.sendMessage(TextUtils.format(text, player));
    }
}
