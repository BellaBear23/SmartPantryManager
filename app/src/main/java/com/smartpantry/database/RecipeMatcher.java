package com.smartpantry.database;

import com.smartpantry.models.Ingredient;
import com.smartpantry.models.Recipe;
import com.smartpantry.models.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMatcher {

    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();
    static {
        UNIT_ALIASES.put("g", "g");
        UNIT_ALIASES.put("gram", "g");
        UNIT_ALIASES.put("grams", "g");
        UNIT_ALIASES.put("kg", "kg");
        UNIT_ALIASES.put("kilogram", "kg");
        UNIT_ALIASES.put("kilograms", "kg");
        UNIT_ALIASES.put("ml", "ml");
        UNIT_ALIASES.put("milliliter", "ml");
        UNIT_ALIASES.put("millilitre", "ml");
        UNIT_ALIASES.put("milliliters", "ml");
        UNIT_ALIASES.put("millilitres", "ml");
        UNIT_ALIASES.put("l", "l");
        UNIT_ALIASES.put("liter", "l");
        UNIT_ALIASES.put("litre", "l");
        UNIT_ALIASES.put("liters", "l");
        UNIT_ALIASES.put("litres", "l");
        UNIT_ALIASES.put("tsp", "tsp");
        UNIT_ALIASES.put("teaspoon", "tsp");
        UNIT_ALIASES.put("teaspoons", "tsp");
        UNIT_ALIASES.put("tbsp", "tbsp");
        UNIT_ALIASES.put("tablespoon", "tbsp");
        UNIT_ALIASES.put("tablespoons", "tbsp");
        UNIT_ALIASES.put("cup", "cup");
        UNIT_ALIASES.put("cups", "cup");
        UNIT_ALIASES.put("whole", "whole");
        UNIT_ALIASES.put("clove", "cloves");
        UNIT_ALIASES.put("cloves", "cloves");
        UNIT_ALIASES.put("slice", "slices");
        UNIT_ALIASES.put("slices", "slices");
        UNIT_ALIASES.put("pinch", "pinch");
        UNIT_ALIASES.put("pinches", "pinch");
    }

    public static List<Recipe> getStrictMatches(List<Recipe> allRecipes, List<Ingredient> pantryItems) {
        Map<String, Double> pantryMap = buildPantryMap(pantryItems);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipeIsFullySatisfied(recipe, pantryMap)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    public static List<Recipe> getAlmostMatches(List<Recipe> allRecipes, List<Ingredient> pantryItems) {
        Map<String, Double> pantryMap = buildPantryMap(pantryItems);
        List<Recipe> almost = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (!recipeIsFullySatisfied(recipe, pantryMap)) {
                int missing = countMissingIngredients(recipe, pantryMap);
                if (missing == 1) {
                    almost.add(recipe);
                }
            }
        }
        return almost;
    }

    private static boolean recipeIsFullySatisfied(Recipe recipe, Map<String, Double> pantryMap) {
        for (RecipeIngredient required : recipe.getRequiredIngredients()) {
            if (!isIngredientSatisfied(required, pantryMap)) {
                return false;
            }
        }
        return true;
    }

    private static int countMissingIngredients(Recipe recipe, Map<String, Double> pantryMap) {
        int missing = 0;
        for (RecipeIngredient required : recipe.getRequiredIngredients()) {
            if (!isIngredientSatisfied(required, pantryMap)) {
                missing++;
            }
        }
        return missing;
    }

    private static boolean isIngredientSatisfied(RecipeIngredient required, Map<String, Double> pantryMap) {
        String normName = normaliseName(required.getName());
        String normUnit = normaliseUnit(required.getUnit());
        String family = unitFamily(normUnit);
        double requiredBase = toBaseAmount(required.getQuantity(), normUnit);
        double available = getAvailableQuantity(pantryMap, normName, family);
        return available >= requiredBase;
    }

    private static double getAvailableQuantity(Map<String, Double> pantryMap, String normName, String family) {
        String key = normName + "|" + family;
        if (pantryMap.containsKey(key)) {
            return pantryMap.get(key);
        }
        if (normName.endsWith("es") && normName.length() > 3) {
            String strippedKey = normName.substring(0, normName.length() - 2) + "|" + family;
            if (pantryMap.containsKey(strippedKey)) return pantryMap.get(strippedKey);
        }
        if (normName.endsWith("s") && normName.length() > 2) {
            String strippedKey = normName.substring(0, normName.length() - 1) + "|" + family;
            if (pantryMap.containsKey(strippedKey)) return pantryMap.get(strippedKey);
        }
        if (pantryMap.containsKey(normName + "s|" + family)) return pantryMap.get(normName + "s|" + family);
        if (pantryMap.containsKey(normName + "es|" + family)) return pantryMap.get(normName + "es|" + family);
        return 0.0;
    }

    private static Map<String, Double> buildPantryMap(List<Ingredient> pantryItems) {
        Map<String, Double> map = new HashMap<>();
        for (Ingredient item : pantryItems) {
            String normName = normaliseName(item.getName());
            String normUnit = normaliseUnit(item.getUnit());
            String family = unitFamily(normUnit);
            double baseQty = toBaseAmount(item.getQuantity(), normUnit);
            String key = normName + "|" + family;
            double existing = map.containsKey(key) ? map.get(key) : 0.0;
            map.put(key, existing + baseQty);
        }
        return map;
    }

    public static String normaliseName(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static String normaliseUnit(String raw) {
        if (raw == null) return "";
        String key = raw.trim().toLowerCase();
        return UNIT_ALIASES.containsKey(key) ? UNIT_ALIASES.get(key) : key;
    }

    private static String unitFamily(String normUnit) {
        switch (normUnit) {
            case "kg":
            case "g":
                return "weight";
            case "l":
            case "ml":
            case "tsp":
            case "tbsp":
            case "cup":
                return "volume";
            default:
                return normUnit;
        }
    }

    private static double toBaseAmount(double qty, String normUnit) {
        switch (normUnit) {
            case "kg":   return qty * 1000;
            case "l":    return qty * 1000;
            case "tsp":  return qty * 5;
            case "tbsp": return qty * 15;
            case "cup":  return qty * 250;
            default:     return qty;
        }
    }
}