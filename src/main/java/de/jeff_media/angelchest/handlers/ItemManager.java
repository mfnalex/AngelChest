package de.jeff_media.angelchest.handlers;

import com.google.common.base.Enums;
import de.jeff_media.angelchest.Main;
import de.jeff_media.angelchest.nbt.NBTTags;
import de.jeff_media.jefflib.ItemStackUtils;
import de.jeff_media.jefflib.PDCUtils;
import lombok.Getter;
import lombok.NonNull;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class ItemManager {

    private static final Main main = Main.getInstance();

    @Getter private HashMap<String, ItemStack> items;

    public ItemManager() {

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

        items = new HashMap<>();

        for(String itemId : yaml.getKeys(false)) {
            ItemStack item = ItemStackUtils.fromConfigurationSection(yaml.getConfigurationSection(itemId));
            PDCUtils.set(item, NBTTags.IS_TOKEN_ITEM, PersistentDataType.STRING,itemId);
            if(yaml.getBoolean(itemId+".glow")) {
                item.addUnsafeEnchantment(main.getGlowEnchantment(), 1);
            }
            if(yaml.getBoolean(itemId+".keep-on-death")) {
                PDCUtils.set(item, NBTTags.IS_TOKEN_ITEM_KEEP, PersistentDataType.BYTE,(byte) 1);
            }
            if(yaml.isConfigurationSection(itemId+".recipe")) {
                Recipe recipe = getRecipe(Objects.requireNonNull(yaml.getConfigurationSection(itemId + ".recipe")));
                if(recipe != null) {
                    Bukkit.addRecipe(recipe);
                }
            }
            items.put(itemId, item);
        }
    }

    @Nullable
    private Recipe getRecipe(@NonNull ConfigurationSection section, String itemId) {
        switch (section.getString("type","")) {
            case "shaped":
                return getRecipeShaped(section,itemId);
            case "shapeless":
                return getRecipeShapeless(section, itemId);
            default:
                main.getLogger().warning("Invalid recipe type specified for item " + itemId+": " + section.getString("type","<null>"));
                return null;
        }
    }

    private void printInvalidIngredientWarn(Object item, String itemId) {
        main.getLogger().warning("Invalid recipe ingredient for item "+itemId+": " + item);
    }

    private List<RecipeChoice> getIngredients(List<?> list, String itemId) {
        List<RecipeChoice> ingredients = new ArrayList<>();

        for(Object item : list) {
            if(item instanceof String) {
                Material mat = Enums.getIfPresent(Material.class, ((String)item).toUpperCase(Locale.ROOT)).orNull();
                if(mat==null) {
                    printInvalidIngredientWarn(item, itemId);
                    return null;
                }
                ingredients.add(new RecipeChoice.MaterialChoice(mat));
            } else if(item instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) item;
                ingredients.add(new RecipeChoice.ExactChoice(itemStack));
            } else {
                printInvalidIngredientWarn(item, itemId);
            }
        }

        return ingredients;

    }

    private Recipe getRecipeShapeless(ConfigurationSection section, String itemId) {
        List<RecipeChoice> ingredients = getIngredients(Objects.requireNonNull(section.getList("ingredients")),itemId);

    }

    @Nullable
    public ItemStack getItem(String itemId) {
        return items.get(itemId);
    }

}
