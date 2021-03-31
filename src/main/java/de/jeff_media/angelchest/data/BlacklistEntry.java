package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.enums.BlacklistResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class BlacklistEntry {

    final String name;
    boolean wildcardFront = false;
    boolean wildcardEnd = false;
    String material;
    final List<String> loreContains;
    final List<String> loreExact;
    final String nameContains;
    final String nameExact;
    final boolean ignoreColors;

    public String getName() {
        return name;
    }

    public BlacklistEntry(String name, FileConfiguration config) {
        this.name = name;
        Main main = Main.getInstance();
        main.debug("Reading Blacklist entry \""+this.name+"\"");
        String materialName = config.getString(name+".material","any");
        assert materialName != null;
        if(materialName.equalsIgnoreCase("any")) {
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
        main.debug("- materialName: "+ (materialName == null ? "null" : materialName));
        main.debug("- wildcardFront: "+wildcardFront);
        main.debug("- wildcardEnd: "+wildcardEnd);
        this.loreContains = config.getStringList(name+".loreContains");
        for(int i = 0; i < loreContains.size();i++) {
            String line = ChatColor.translateAlternateColorCodes('&',loreContains.get(i));
            main.debug("- loreContains: " + line);
            loreContains.set(i,line);
        }
        this.loreExact = config.getStringList(name+".loreExact");
        for(int i = 0; i < loreExact.size();i++) {
            String line = ChatColor.translateAlternateColorCodes('&',loreExact.get(i));
            main.debug("- loreExact: "+line);
            loreExact.set(i,line);
        }
        this.nameContains = ChatColor.translateAlternateColorCodes('&',config.getString(name+".nameContains",""));
        main.debug("- nameContains: "+nameContains);
        this.nameExact = ChatColor.translateAlternateColorCodes('&',config.getString(name+".nameExact",""));
        main.debug("- nameExact: "+nameExact);
        this.ignoreColors = config.getBoolean(name+".ignoreColors",false);
        main.debug("- ignoreColors: "+ignoreColors);

    }

    private String join(List<String> list, boolean ignoreColors) {
        StringBuilder result = new StringBuilder();
        for (String line : list) {
            if(ignoreColors) line = ChatColor.stripColor(line);
            result.append("\n").append(line).append("\n");
        }
        return result.toString();
    }

    private boolean materialMatches(Material type) {
        if(wildcardFront && wildcardEnd) {
            return type.name().contains(material);
        }
        if(wildcardEnd) {
            return type.name().startsWith(material);
        }
        if(wildcardFront) {
            return type.name().endsWith(material);
        }
        return type.name().equals(material);
    }

    public BlacklistResult matches(ItemStack item) {
        Main main = Main.getInstance();
        if(item==null) return null;
        if(material!=null) {
            if(!materialMatches(item.getType())) {
                main.verbose("Blacklist: no, other material");
                return BlacklistResult.NO_MATCH_MATERIAL;
            }
        }
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        assert meta != null;

        // Exact name
        if(nameExact != null && nameExact.length()>0) {
            if(!meta.hasDisplayName()) {
                main.verbose("Blacklist: no, nameExact but no name");
                return BlacklistResult.NO_MATCH_NAME_EXACT;
            }
            if(ignoreColors) {
                if(!ChatColor.stripColor(meta.getDisplayName()).equals(nameExact)) {
                    main.verbose("Blacklist: no, nameExact but no match (ignore colors)");
                    return BlacklistResult.NO_MATCH_NAME_EXACT;
                }
            } else {
                if(!meta.getDisplayName().equals(nameExact)) {
                    main.verbose("Blacklist: no, nameExact but no match (check colors)");
                    return BlacklistResult.NO_MATCH_NAME_EXACT;
                }
            }
        }

        // Name contains
        if(nameContains != null && nameContains.length()>0) {
            if(!meta.hasDisplayName()) {
                main.verbose("Blacklist: no, nameContains but no name");
                return BlacklistResult.NO_MATCH_NAME_CONTAINS;
            }
            if(ignoreColors) {
                if(!ChatColor.stripColor(meta.getDisplayName()).contains(nameContains)) {
                    main.verbose("Blacklist: no, nameContains but no match (ignore colors)");
                    return BlacklistResult.NO_MATCH_NAME_CONTAINS;
                }
            } else {
                if(!meta.getDisplayName().contains(nameContains)) {
                    main.verbose("Blacklist: no, nameContains but no match (check colors)");
                    return BlacklistResult.NO_MATCH_NAME_CONTAINS;
                }
            }
        }

        if(loreContains != null && loreContains.size()>0) {
            if (!meta.hasLore()) {
                main.verbose("Blacklist: no, loreContains but no lore");
                return BlacklistResult.NO_MATCH_LORE_CONTAINS;
            }
            for (String lineBlacklist : loreContains) {
                String loreItem = join(meta.getLore(), ignoreColors);
                if (!loreItem.contains(lineBlacklist)) {
                    main.verbose("Blacklist: no, loreContains but no match");
                    return BlacklistResult.NO_MATCH_LORE_CONTAINS;
                }
            }
        }
        if(loreExact != null && loreExact.size()>0) {
            if(!meta.hasLore()) {
                main.verbose("Blacklist: no, loreExact but no lore");
                return BlacklistResult.NO_MATCH_LORE_EXACT;
            }
            for(String lineBlacklist : loreExact) {
                String loreItem = join(meta.getLore(),ignoreColors);
                if(!loreItem.contains("\n" + lineBlacklist + "\n")) {
                    main.verbose("Blacklist: no, loreExact but no match");
                    return BlacklistResult.NO_MATCH_LORE_EXACT;
                }
            }
        }

        BlacklistResult result = BlacklistResult.MATCH;
        result.setName(name);
        return result;
    }

}
