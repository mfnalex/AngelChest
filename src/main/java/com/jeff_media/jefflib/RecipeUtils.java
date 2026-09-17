package com.jeff_media.jefflib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;

public final class RecipeUtils {

    private RecipeUtils() {
    }

    public static Recipe getRecipe(final ConfigurationSection section, final NamespacedKey key, final ItemStack result) {
        final String type = section.getString("type", "").toLowerCase(Locale.ROOT);
        return switch (type) {
            case "shapeless" -> shapeless(section, key, result);
            case "shaped" -> shaped(section, key, result);
            case "blasting" -> blasting(section, key, result);
            case "campfire" -> campfire(section, key, result);
            case "furnace" -> furnace(section, key, result);
            case "smoking" -> smoking(section, key, result);
            case "stonecutting" -> new StonecuttingRecipe(key, result, oneChoice(section));
            case "smithing" -> smithing(section, key, result);
            default -> throw new IllegalArgumentException("Invalid recipe type: " + type);
        };
    }

    private static ShapelessRecipe shapeless(final ConfigurationSection section, final NamespacedKey key, final ItemStack result) {
        final ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        choices(section).forEach(recipe::addIngredient);
        return recipe;
    }

    private static ShapedRecipe shaped(final ConfigurationSection section, final NamespacedKey key, final ItemStack result) {
        final ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(section.getStringList("shape").toArray(new String[0]));
        choicesMap(section).forEach(recipe::setIngredient);
        return recipe;
    }

    private static BlastingRecipe blasting(final ConfigurationSection s, final NamespacedKey k, final ItemStack r) {
        return new BlastingRecipe(k, r, oneChoice(s), (float) s.getDouble("experience", 0), s.getInt("cooking-time"));
    }

    private static CampfireRecipe campfire(final ConfigurationSection s, final NamespacedKey k, final ItemStack r) {
        return new CampfireRecipe(k, r, oneChoice(s), (float) s.getDouble("experience", 0), s.getInt("cooking-time"));
    }

    private static FurnaceRecipe furnace(final ConfigurationSection s, final NamespacedKey k, final ItemStack r) {
        return new FurnaceRecipe(k, r, oneChoice(s), (float) s.getDouble("experience", 0), s.getInt("cooking-time"));
    }

    private static SmokingRecipe smoking(final ConfigurationSection s, final NamespacedKey k, final ItemStack r) {
        return new SmokingRecipe(k, r, oneChoice(s), (float) s.getDouble("experience", 0), s.getInt("cooking-time"));
    }

    private static SmithingRecipe smithing(final ConfigurationSection section, final NamespacedKey key, final ItemStack result) {
        final List<RecipeChoice> choices = choices(section);
        if (choices.size() != 2) throw new IllegalArgumentException("Smithing recipes need exactly two ingredients");
        return new SmithingRecipe(key, result, choices.get(0), choices.get(1));
    }

    private static RecipeChoice oneChoice(final ConfigurationSection section) {
        final List<RecipeChoice> choices = choices(section);
        if (choices.size() != 1) throw new IllegalArgumentException("Recipe needs exactly one ingredient");
        return choices.get(0);
    }

    private static List<RecipeChoice> choices(final ConfigurationSection section) {
        final List<RecipeChoice> choices = new ArrayList<>();
        if (section.isList("ingredients")) {
            for (Object ingredient : Objects.requireNonNull(section.getList("ingredients"))) choices.add(choice(ingredient));
        } else if (section.isConfigurationSection("ingredients")) {
            choices.addAll(choicesMap(section).values());
        } else if (section.isSet("ingredient")) {
            choices.add(choice(section.get("ingredient")));
        } else {
            throw new IllegalArgumentException("No recipe ingredient defined");
        }
        return choices;
    }

    private static Map<Character, RecipeChoice> choicesMap(final ConfigurationSection section) {
        final Map<Character, RecipeChoice> result = new HashMap<>();
        final ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        if (ingredients == null) throw new IllegalArgumentException("Recipe ingredients must be a map");
        for (String key : ingredients.getKeys(false)) {
            if (key.length() != 1) throw new IllegalArgumentException("Ingredient keys must be one character");
            result.put(key.charAt(0), choice(ingredients.get(key)));
        }
        return result;
    }

    private static RecipeChoice choice(final Object value) {
        if (value instanceof ItemStack item) return new RecipeChoice.ExactChoice(item);
        final Material material = Material.matchMaterial(String.valueOf(value));
        if (material == null) throw new IllegalArgumentException("Invalid recipe ingredient: " + value);
        return new RecipeChoice.MaterialChoice(material);
    }
}
