package de.jeff_media.angelchest.handlers;

import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.enums.PremiumFeatures;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.daddy.Stepsister;
import com.jeff_media.jefflib.ItemStackUtils;
import com.jeff_media.jefflib.PDCUtils;
import com.jeff_media.jefflib.RecipeUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ItemManager {

    private static final Main main = Main.getInstance();

    @Getter private final HashMap<String, ItemStack> items;
    //@Getter private final HashMap<String, String> itemNames;
    @Getter private final HashSet<NamespacedKey> autodiscoverRecipes;

    public ItemManager() {

        items = new HashMap<>();
        //itemNames = new HashMap<>();
        autodiscoverRecipes = new HashSet<>();

        if(!Stepsister.allows(PremiumFeatures.CUSTOM_ITEMS)) return;

        Set<NamespacedKey> recipesToRemove = new HashSet<>();
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while(iterator.hasNext()) {
            Recipe recipe = iterator.next();
            Keyed keyed = (Keyed) recipe;
            if(keyed.getKey().getNamespace().equals(PDCUtils.getKey("cazcez").getNamespace())) {
                recipesToRemove.add(keyed.getKey());
            }
        }
        recipesToRemove.forEach(Bukkit::removeRecipe);


        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "items.yml"));

        for(String itemId : yaml.getKeys(false)) {
            if(yaml.getConfigurationSection(itemId).isItemStack("exact")) {
                ItemStack item = yaml.getItemStack(itemId + ".exact");
                items.put(itemId, item);
                continue;

            }
            ItemStack item = ItemStackUtils.fromConfigurationSection(yaml.getConfigurationSection(itemId));
            PDCUtils.set(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING,itemId);
            if(yaml.getBoolean(itemId+".glow")) {
                item.addUnsafeEnchantment(main.getGlowEnchantment(), 1);
            }
            if(yaml.getBoolean(itemId+".keep-on-death")) {
                PDCUtils.set(item, NBTTags.IS_TOKEN_ITEM_KEEP, PersistentDataType.BYTE,(byte) 1);
            }
            if(yaml.isConfigurationSection(itemId+".recipe") && yaml.getBoolean(itemId+".crafting-enabled",false)) {
                NamespacedKey recipeKey = PDCUtils.getKey("recipe-"+itemId);
                Recipe recipe = RecipeUtils.getRecipe(yaml.getConfigurationSection(itemId+".recipe"),recipeKey,item);
                if(recipe != null) {
                    Bukkit.addRecipe(recipe);
                    if(yaml.getBoolean(itemId+".autodiscover",false)) {
                        autodiscoverRecipes.add(recipeKey);
                    }
                }
            }
            //itemNames.put(itemId, yaml.getString(itemId+".display-name", MaterialUtils.getNiceMaterialName(item.getType())));
            ItemStack withoutPdc = item.clone();
            PDCUtils.remove(withoutPdc, NBTTags.IS_TOKEN_ITEM);
            if(isVanillaItem(withoutPdc)) {
                item = withoutPdc;
            }
            items.put(itemId, item);
            Bukkit.getOnlinePlayers().forEach(this::autodiscover);
        }
    }

    public boolean isVanillaItem(ItemStack item) {
        if(item.isSimilar(new ItemStack(item.getType()))) {
            return true;
        } else {
            return false;
        }
    }

    public void autodiscover(Player player) {
        for(NamespacedKey recipe : autodiscoverRecipes) {
            if(player.hasDiscoveredRecipe(recipe)) continue;
            player.discoverRecipe(recipe);
        }
    }

    @Nullable
    public ItemStack getItem(String itemId) {
        return items.get(itemId);
    }

}
