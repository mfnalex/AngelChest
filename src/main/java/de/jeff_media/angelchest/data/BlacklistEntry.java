package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.enums.BlacklistResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public final class BlacklistEntry {

    final boolean ignoreColors;
    final List<String> loreContains;
    final List<String> loreExact;
    final List<String> enchantments;
    final String name;
    final String nameContains;
    final String nameExact;
    String material;
    boolean wildcardEnd = false;
    boolean wildcardFront = false;

    public BlacklistEntry(final String name, final FileConfiguration config) {
        this.name = name;
        final Main main = Main.getInstance();
        if(main.debug) main.debug("Reading Blacklist entry \"" + this.name + "\"");
        String materialName = config.getString(name + ".material", "any");
        assert materialName != null;
        if (materialName.equalsIgnoreCase("any")) {
            materialName = null;
        } else {
            if (materialName.startsWith("*")) {
                wildcardFront = true;
                materialName = materialName.substring(1);
            }
            if (materialName.endsWith("*")) {
                wildcardEnd = true;
                materialName = materialName.substring(0, materialName.length() - 1);
            }
            material = materialName.toUpperCase();
        }
        if(main.debug) main.debug("- materialName: " + (materialName == null ? "null" : materialName));
        if(main.debug) main.debug("- wildcardFront: " + wildcardFront);
        if(main.debug) main.debug("- wildcardEnd: " + wildcardEnd);
        this.loreContains = config.getStringList(name + ".loreContains");
        for (int i = 0; i < loreContains.size(); i++) {
            final String line = ChatColor.translateAlternateColorCodes('&', loreContains.get(i));
            if(main.debug) main.debug("- loreContains: " + line);
            loreContains.set(i, line);
        }
        this.loreExact = config.getStringList(name + ".loreExact");
        for (int i = 0; i < loreExact.size(); i++) {
            final String line = ChatColor.translateAlternateColorCodes('&', loreExact.get(i));
            if(main.debug) main.debug("- loreExact: " + line);
            loreExact.set(i, line);
        }
        this.nameContains = ChatColor.translateAlternateColorCodes('&', config.getString(name + ".nameContains", ""));
        if(main.debug) main.debug("- nameContains: " + nameContains);
        this.nameExact = ChatColor.translateAlternateColorCodes('&', config.getString(name + ".nameExact", ""));
        if(main.debug) main.debug("- nameExact: " + nameExact);
        this.enchantments = config.getStringList(name + ".enchantments");
        this.ignoreColors = config.getBoolean(name + ".ignoreColors", false);
        if(main.debug) main.debug("- ignoreColors: " + ignoreColors);

    }

    public String getName() {
        return name;
    }

    private String join(final List<String> list, final boolean ignoreColors) {
        final StringBuilder result = new StringBuilder();
        for (String line : list) {
            if (ignoreColors) line = ChatColor.stripColor(line);
            result.append("\n").append(line).append("\n");
        }
        return result.toString();
    }

    public BlacklistResult matches(final ItemStack item) {
        final Main main = Main.getInstance();
        if (item == null) return null;
        if (material != null) {
            if (!materialMatches(item.getType())) {
                main.verbose("Blacklist: no, other material");
                return BlacklistResult.NO_MATCH_MATERIAL;
            }
        }
        final ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        assert meta != null;

        // Exact name
        if (nameExact != null && !nameExact.isEmpty()) {
            if (!meta.hasDisplayName()) {
                main.verbose("Blacklist: no, nameExact but no name");
                return BlacklistResult.NO_MATCH_NAME_EXACT;
            }
            if (ignoreColors) {
                if (!ChatColor.stripColor(meta.getDisplayName()).equals(nameExact)) {
                    main.verbose("Blacklist: no, nameExact but no match (ignore colors)");
                    return BlacklistResult.NO_MATCH_NAME_EXACT;
                }
            } else {
                if (!meta.getDisplayName().equals(nameExact)) {
                    main.verbose("Blacklist: no, nameExact but no match (check colors)");
                    return BlacklistResult.NO_MATCH_NAME_EXACT;
                }
            }
        }

        // Name contains
        if (nameContains != null && !nameContains.isEmpty()) {
            if (!meta.hasDisplayName()) {
                main.verbose("Blacklist: no, nameContains but no name");
                return BlacklistResult.NO_MATCH_NAME_CONTAINS;
            }
            if (ignoreColors) {
                if (!ChatColor.stripColor(meta.getDisplayName()).contains(nameContains)) {
                    main.verbose("Blacklist: no, nameContains but no match (ignore colors)");
                    return BlacklistResult.NO_MATCH_NAME_CONTAINS;
                }
            } else {
                if (!meta.getDisplayName().contains(nameContains)) {
                    main.verbose("Blacklist: no, nameContains but no match (check colors)");
                    return BlacklistResult.NO_MATCH_NAME_CONTAINS;
                }
            }
        }

        if (loreContains != null && !loreContains.isEmpty()) {
            if (!meta.hasLore()) {
                main.verbose("Blacklist: no, loreContains but no lore");
                return BlacklistResult.NO_MATCH_LORE_CONTAINS;
            }
            for (final String lineBlacklist : loreContains) {
                final String loreItem = join(meta.getLore(), ignoreColors);
                if (!loreItem.contains(lineBlacklist)) {
                    main.verbose("Blacklist: no, loreContains but no match");
                    return BlacklistResult.NO_MATCH_LORE_CONTAINS;
                }
            }
        }
        if (loreExact != null && !loreExact.isEmpty()) {
            if (!meta.hasLore()) {
                main.verbose("Blacklist: no, loreExact but no lore");
                return BlacklistResult.NO_MATCH_LORE_EXACT;
            }
            for (final String lineBlacklist : loreExact) {
                final String loreItem = join(meta.getLore(), ignoreColors);
                if (!loreItem.contains("\n" + lineBlacklist + "\n")) {
                    main.verbose("Blacklist: no, loreExact but no match");
                    return BlacklistResult.NO_MATCH_LORE_EXACT;
                }
            }
        }

        for(final String enchantment : enchantments) {
            boolean contains = false;
            for(final Map.Entry<Enchantment,Integer> entry : meta.getEnchants().entrySet()) {
                if(entry.getKey().getKey().getKey().equalsIgnoreCase(enchantment)) {
                    contains = true;
                    break;
                }
            }
            if(!contains) {
                return BlacklistResult.NO_MATCH_ENCHANTMENTS;
            }
        }

        final BlacklistResult result = BlacklistResult.MATCH;
        result.setName(name);
        return result;
    }

    private boolean materialMatches(final Material type) {
        if (wildcardFront && wildcardEnd) {
            return type.name().contains(material);
        }
        if (wildcardEnd) {
            return type.name().startsWith(material);
        }
        if (wildcardFront) {
            return type.name().endsWith(material);
        }
        return type.name().equals(material);
    }

}
