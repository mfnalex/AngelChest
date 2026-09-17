package com.jeff_media.jefflib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

/** Color and placeholder formatting used by AngelChest messages. */
public final class TextUtils {

    private static final Pattern GRADIENT = Pattern.compile("<#([0-9a-fA-F]{6})>(.*?)<#/([0-9a-fA-F]{6})>");
    private static final Pattern HEX = Pattern.compile("(?:<|&)#([0-9a-fA-F]{6})(?:>)?");

    private TextUtils() {
    }

    public static String format(final String text) {
        return format(text, null);
    }

    public static String format(String text, final OfflinePlayer player) {
        if (text == null) return null;
        final var placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderApi != null) {
            try {
                text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (Throwable ignored) {
            }
        }
        text = applyGradients(text);
        final Matcher hex = HEX.matcher(text);
        final StringBuffer hexResult = new StringBuffer();
        while (hex.find()) hex.appendReplacement(hexResult, Matcher.quoteReplacement(toColorCode(hex.group(1))));
        hex.appendTail(hexResult);
        return ChatColor.translateAlternateColorCodes('&', hexResult.toString().replace("&&", "{ampersand}")).replace("{ampersand}", "&");
    }

    public static String replaceInString(String string, final Map<String, String> placeholders) {
        if (string == null) return null;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }

    public static List<String> replaceInString(final List<String> strings, final Map<String, String> placeholders) {
        final List<String> result = new ArrayList<>(strings.size());
        for (String string : strings) result.add(replaceInString(string, placeholders));
        return result;
    }

    public static List<String> format(final List<String> text, final OfflinePlayer player) {
        final List<String> result = new ArrayList<>(text.size());
        for (String line : text) result.add(format(line, player));
        return result;
    }

    private static String applyGradients(final String text) {
        final Matcher matcher = GRADIENT.matcher(text);
        final StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            final String value = matcher.group(2);
            final int start = Integer.parseInt(matcher.group(1), 16);
            final int end = Integer.parseInt(matcher.group(3), 16);
            final StringBuilder gradient = new StringBuilder();
            final int length = Math.max(1, value.length() - 1);
            for (int i = 0; i < value.length(); i++) {
                final double position = (double) i / length;
                final int red = interpolate(start >> 16 & 0xff, end >> 16 & 0xff, position);
                final int green = interpolate(start >> 8 & 0xff, end >> 8 & 0xff, position);
                final int blue = interpolate(start & 0xff, end & 0xff, position);
                gradient.append(toColorCode(String.format("%02x%02x%02x", red, green, blue))).append(value.charAt(i));
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(gradient.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static int interpolate(final int start, final int end, final double position) {
        return (int) Math.round(start + (end - start) * position);
    }

    private static String toColorCode(final String hex) {
        final StringBuilder result = new StringBuilder("&x");
        for (char character : hex.toCharArray()) result.append('&').append(character);
        return result.toString();
    }
}
