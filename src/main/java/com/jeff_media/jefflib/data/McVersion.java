package com.jeff_media.jefflib.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

public final class McVersion implements Comparable<McVersion> {

    private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private static final McVersion CURRENT_VERSION = parse(Bukkit.getBukkitVersion());
    private final int major;
    private final int minor;
    private final int patch;

    public McVersion(final int major, final int minor) {
        this(major, minor, 0);
    }

    public McVersion(final int major, final int minor, final int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    private static McVersion parse(final String version) {
        final Matcher matcher = VERSION.matcher(version);
        if (!matcher.find()) return new McVersion(0, 0, 0);
        return new McVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3)));
    }

    public static McVersion current() {
        return CURRENT_VERSION;
    }

    public boolean isAtLeast(final int major, final int minor) {
        return compareTo(new McVersion(major, minor)) >= 0;
    }

    public boolean isAtLeast(final int major, final int minor, final int patch) {
        return compareTo(new McVersion(major, minor, patch)) >= 0;
    }

    @Override
    public int compareTo(final McVersion other) {
        int result = Integer.compare(major, other.major);
        if (result != 0) return result;
        result = Integer.compare(minor, other.minor);
        return result != 0 ? result : Integer.compare(patch, other.patch);
    }
}
