package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.AngelChestMain;
import de.jeff_media.angelchest.enums.BlacklistResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public final class BlacklistEntry {

    final boolean ignoreColors;
    final List<String> loreContains;
    final List<String> loreExact;
    final List<String> enchantments;
    final List<String> pdcKeys;
    final List<Pattern> toStringRegex;

    Integer modelDataMin = null;
    Integer modelDataMax = null;
    final String name;
    final String nameContains;
    final String nameExact;
    String material;
    boolean wildcardEnd = false;
    boolean wildcardFront = false;
    boolean delete = false;
    final int slot;

    public BlacklistEntry(final String name, final FileConfiguration config) {
        this.name = name;
        final AngelChestMain main = AngelChestMain.getInstance();
        if (main.debug) main.debug("Reading Blacklist entry \"" + this.name + "\"");
        String materialName = config.getString(name + ".material", "any");
        slot = config.getInt(name + ".slot",-1);
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
        if (main.debug) main.debug("- materialName: " + (materialName == null ? "null" : materialName));
        if (main.debug) main.debug("- wildcardFront: " + wildcardFront);
        if (main.debug) main.debug("- wildcardEnd: " + wildcardEnd);
        this.loreContains = config.getStringList(name + ".loreContains");
        for (int i = 0; i < loreContains.size(); i++) {
            final String line = ChatColor.translateAlternateColorCodes('&', loreContains.get(i));
            if (main.debug) main.debug("- loreContains: " + line);
            loreContains.set(i, line);
        }
        this.loreExact = config.getStringList(name + ".loreExact");
        for (int i = 0; i < loreExact.size(); i++) {
            final String line = ChatColor.translateAlternateColorCodes('&', loreExact.get(i));
            if (main.debug) main.debug("- loreExact: " + line);
            loreExact.set(i, line);
        }
        this.nameContains = ChatColor.translateAlternateColorCodes('&', config.getString(name + ".nameContains", ""));
        if (main.debug) main.debug("- nameContains: " + nameContains);
        this.nameExact = ChatColor.translateAlternateColorCodes('&', config.getString(name + ".nameExact", ""));
        if (main.debug) main.debug("- nameExact: " + nameExact);
        this.enchantments = config.getStringList(name + ".enchantments");
        this.ignoreColors = config.getBoolean(name + ".ignoreColors", false);
        if (main.debug) main.debug("- ignoreColors: " + ignoreColors);
        this.delete = config.getBoolean(name + ".force-delete",false);
        if(main.debug) main.debug("- force-delete: " + delete);
        this.pdcKeys = config.getStringList(name + ".pdcKeys");
        if (main.debug) main.debug("- pdcKeys: " + pdcKeys);
        this.toStringRegex = config.getStringList(name + ".toStringRegex").stream().map(entry -> {
            entry = entry.replace("\\n", "\n");
            try {
                return Pattern.compile(entry);
            } catch (PatternSyntaxException exception) {
                main.getLogger().warning("Invalid regex in blacklist entry \"" + name + "\": " + entry);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());;
        if( main.debug) main.debug("- toStringRegex: " + toStringRegex.stream().map(Pattern::toString).collect(Collectors.joining(System.lineSeparator())));

        if(config.isInt(name + ".customModelData.max")) {
            modelDataMax = config.getInt(name + ".customModelData.max");
            if(main.debug) main.debug("- customModelData.max: " + modelDataMax);
        }
        if(config.isInt(name + ".customModelData.min")) {
            modelDataMin = config.getInt(name + ".customModelData.min");
            if(main.debug) main.debug("- customModelData.min: " + modelDataMin);
        }

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

    public BlacklistResult matches(final ItemStack item, int slot) {
        final AngelChestMain main = AngelChestMain.getInstance();
        if (item == null) return null;
        if(main.debug) {
            main.debug("Checking if " + item + " matches blacklist entry " + name + " ...");
        }
        if(this.slot != -1) {
            //System.out.println("Slot: " + this.slot + " - " + slot);
            int blacklisted = this.slot;
            //System.out.println("Blacklisted: " + blacklisted + " - " + slot);

            if(blacklisted != slot) {
                main.debug("Blacklist: no, slot does not match");
                return BlacklistResult.NO_MATCH_SLOT;
            }
        }  //System.out.println("Slot is -1");

        if (material != null) {
            if (!materialMatches(item.getType())) {
                main.debug("Blacklist: no, other material");
                return BlacklistResult.NO_MATCH_MATERIAL;
            }
        }
        final ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        assert meta != null;

        // Exact name
        if (nameExact != null && !nameExact.isEmpty()) {
            if (!meta.hasDisplayName()) {
                main.debug("Blacklist: no, nameExact but no name");
                return BlacklistResult.NO_MATCH_NAME_EXACT;
            }
            if (ignoreColors) {
                if (!ChatColor.stripColor(meta.getDisplayName()).equals(nameExact)) {
                    main.debug("Blacklist: no, nameExact but no match (ignore colors)");
                    return BlacklistResult.NO_MATCH_NAME_EXACT;
                }
            } else {
                if (!meta.getDisplayName().equals(nameExact)) {
                    main.debug("Blacklist: no, nameExact but no match (check colors)");
                    return BlacklistResult.NO_MATCH_NAME_EXACT;
                }
            }
        }

        Integer modelData = null;
        if(meta.hasCustomModelData()) {
            modelData = meta.getCustomModelData();
        }

        if(modelData == null) {
            // Fallback to using -1 as model data
            modelData = -1;
        }
        main.debug(" ModelData: " + modelData);
        main.debug(" ModelDataMin: " + modelDataMin);
        main.debug(" ModelDataMax: " + modelDataMax);

        // Custom Model Data
        if(modelDataMin != null) {
            if(modelData < modelDataMin) {
                main.debug("BlackList: no, modelDataMin but modelData is too low");
                return BlacklistResult.NO_MATCH_CUSTOM_MODEL_DATA_MIN;
            }
        }
        if(modelDataMax != null) {
            if(modelData > modelDataMax) {
                main.debug("BlackList: no, modelDataMax but modelData is too high");
                return BlacklistResult.NO_MATCH_CUSTOM_MODEL_DATA_MAX;
            }
        }

        // Name contains
        if (nameContains != null && !nameContains.isEmpty()) {
            if (!meta.hasDisplayName()) {
                main.debug("Blacklist: no, nameContains but no name");
                return BlacklistResult.NO_MATCH_NAME_CONTAINS;
            }
            if (ignoreColors) {
                if (!ChatColor.stripColor(meta.getDisplayName()).contains(nameContains)) {
                    main.debug("Blacklist: no, nameContains but no match (ignore colors)");
                    return BlacklistResult.NO_MATCH_NAME_CONTAINS;
                }
            } else {
                if (!meta.getDisplayName().contains(nameContains)) {
                    main.debug("Blacklist: no, nameContains but no match (check colors)");
                    return BlacklistResult.NO_MATCH_NAME_CONTAINS;
                }
            }
        }

        if (loreContains != null && !loreContains.isEmpty()) {
            if (!meta.hasLore()) {
                main.debug("Blacklist: no, loreContains but no lore");
                return BlacklistResult.NO_MATCH_LORE_CONTAINS;
            }
            for (final String lineBlacklist : loreContains) {
                final String loreItem = join(meta.getLore(), ignoreColors);
                if (!loreItem.contains(lineBlacklist)) {
                    main.debug("Blacklist: no, loreContains but no match");
                    return BlacklistResult.NO_MATCH_LORE_CONTAINS;
                }
            }
        }
        if (loreExact != null && !loreExact.isEmpty()) {
            if (!meta.hasLore()) {
                main.debug("Blacklist: no, loreExact but no lore");
                return BlacklistResult.NO_MATCH_LORE_EXACT;
            }
            for (final String lineBlacklist : loreExact) {
                final String loreItem = join(meta.getLore(), ignoreColors);
                if (!loreItem.contains("\n" + lineBlacklist + "\n")) {
                    main.debug("Blacklist: no, loreExact but no match");
                    return BlacklistResult.NO_MATCH_LORE_EXACT;
                }
            }
        }

        for (final String enchantment : enchantments) {
            boolean contains = false;
            for (final Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                if (entry.getKey().getKey().getKey().equalsIgnoreCase(enchantment)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return BlacklistResult.NO_MATCH_ENCHANTMENTS;
            }
        }

        // PDC Keys
        if(pdcKeys != null && pdcKeys.size() > 0) {
            for(String key : pdcKeys) {
                if(!item.hasItemMeta()) {
                    main.debug("Blacklist: no, pdcKeys but no meta");
                    return BlacklistResult.NO_MATCH_PDC_KEYS;
                }
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if(!hasPdcKey(pdc,key)) {
                    main.debug("Blacklist: no, pdcKeys but no match for " + key);
                    return BlacklistResult.NO_MATCH_PDC_KEYS;
                }
            }
        }

        // ToStringRegex
        if(toStringRegex != null && toStringRegex.size() > 0) {
            try {
                String itemAsString = item.toString();
                for(Pattern pattern : toStringRegex) {
                    if(!pattern.matcher(itemAsString).find()) {
                        main.debug("Blacklist: no, toStringRegex but no match for " + pattern.pattern());
                        return BlacklistResult.NO_MATCH_TO_STRING_REGEX;
                    }
                }
            } catch (Throwable paper) {
                main.debug("Could not turn ItemStack to string:");
                if(main.debug) paper.printStackTrace();
            }
        }

        final BlacklistResult result = delete ? BlacklistResult.MATCH_DELETE : BlacklistResult.MATCH_IGNORE;
        result.setName(name);
        return result;
    }

    private static boolean hasPdcKey(PersistentDataContainer pdc, String keyToCheck) {
        return pdc.getKeys().stream().anyMatch(key -> key.toString().equalsIgnoreCase(keyToCheck));
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
