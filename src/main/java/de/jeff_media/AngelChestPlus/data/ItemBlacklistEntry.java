package de.jeff_media.AngelChestPlus.data;

import com.google.common.base.Enums;
import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class ItemBlacklistEntry {

    String name;
    Material material;
    List<String> loreContains;
    List<String> loreExact;
    String nameContains;
    String nameExact;
    boolean ignoreColors;

    public ItemBlacklistEntry(String name, FileConfiguration config) throws EnumConstantNotPresentException {
        if(config.getString(name+".material","any").equalsIgnoreCase("any")) {
            material = null;
        }
        else if(Enums.getIfPresent(Material.class,config.getString(name+".material").toUpperCase()).orNull()==null) {
            throw new EnumConstantNotPresentException(Material.class,config.getString(name+".material"));
        } else {
            material = Enums.getIfPresent(Material.class,config.getString(name+".material").toUpperCase()).orNull();
        }
        this.name = name;
        this.loreContains = config.getStringList(name+".loreContains");
        for(int i = 0; i < loreContains.size();i++) {
            loreContains.set(i,ChatColor.translateAlternateColorCodes('&',loreContains.get(i)));
        }
        this.loreExact = config.getStringList(name+".loreExact");
        for(int i = 0; i < loreExact.size();i++) {
            loreExact.set(i,ChatColor.translateAlternateColorCodes('&',loreExact.get(i)));
        }
        this.nameContains = ChatColor.translateAlternateColorCodes('&',config.getString(name+".nameContains",""));
        this.nameExact = ChatColor.translateAlternateColorCodes('&',config.getString(name+".nameExact",""));
        this.ignoreColors = config.getBoolean(name+".ignoreColors",false);
    }

    private String join(List<String> list, boolean ignoreColors) {
        String result = "";
        for (String line : list) {
            if(ignoreColors) line = ChatColor.stripColor(line);
            result = result + "\n" + line + "\n";
        }
        return result;
    }

    public @Nullable String matches(ItemStack item) {
        Main main = Main.getInstance();
        if(item==null) return null;
        if(material!=null) {
            if(item.getType() != material) {
                main.verbose("Blacklist: no, other material");
                return null;
            }
        }
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());

        // Exact name
        if(nameExact != null && nameExact.length()>0) {
            if(!meta.hasDisplayName()) {
                main.verbose("Blacklist: no, nameExact but no name");
                return null;
            }
            if(ignoreColors) {
                if(!ChatColor.stripColor(meta.getDisplayName()).equals(nameExact)) {
                    main.verbose("Blacklist: no, nameExact but no match (ignore colors)");
                    return null;
                }
            } else {
                if(!meta.getDisplayName().equals(nameExact)) {
                    main.verbose("Blacklist: no, nameExact but no match (check colors)");
                    return null;
                }
            }
        }

        // Name contains
        if(nameContains != null && nameContains.length()>0) {
            if(!meta.hasDisplayName()) {
                main.verbose("Blacklist: no, nameContains but no name");
                return null;
            }
            if(ignoreColors) {
                if(!ChatColor.stripColor(meta.getDisplayName()).contains(nameContains)) {
                    main.verbose("Blacklist: no, nameContains but no match (ignore colors)");
                    return null;
                }
            } else {
                if(!meta.getDisplayName().contains(nameContains)) {
                    main.verbose("Blacklist: no, nameContains but no match (check colors)");
                    return null;
                }
            }
        }

        if(loreContains != null && loreContains.size()>0) {
            if (!meta.hasLore()) {
                main.verbose("Blacklist: no, loreContains but no lore");
                return null;
            }
            for (String lineBlacklist : loreContains) {
                String loreItem = join(meta.getLore(), ignoreColors);
                if (!loreItem.contains(lineBlacklist)) {
                    main.verbose("Blacklist: no, loreContains but no match");
                    return null;
                }
            }
        }
        if(loreExact != null && loreExact.size()>0) {
            if(!meta.hasLore()) {
                main.verbose("Blacklist: no, loreExact but no lore");
                return null;
            }
            for(String lineBlacklist : loreExact) {
                String loreItem = join(meta.getLore(),ignoreColors);
                if(!loreItem.contains("\n" + lineBlacklist + "\n")) {
                    main.verbose("Blacklist: no, loreExact but no match");
                    return null;
                }
            }
        }

        return name;
    }

}
