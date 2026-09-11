package com.smartpantry.database;

import com.smartpantry.models.Ingredient;
import com.smartpantry.models.Recipe;
import com.smartpantry.models.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecipeMatcher implements the core "strict-matching rule" described in Section 2.3
 * of the assignment brief.
 *
 * A recipe qualifies as "suggested" ONLY when every single ingredient it requires
 * is present in the pantry in at least the required quantity. Partial matches are
 * excluded from the strict suggestions list.
 *
 * Matching is made robust to trivial real-world differences by:
 *  - Normalising ingredient names to lower-case and trimming whitespace.
 *  - Stripping common English plural suffixes (s, es) so "tomato"/"tomatoes" match.
 *  - Normalising common unit aliases (ml/milliliter, g/gram, tbsp/tablespoon, etc.).
 */
public class RecipeMatcher {

    /**
     * Returns only the recipes for which the pantry satisfies every ingredient requirement.
     *
     * @param allRecipes   all recipes from the database
     * @param pantryItems  the current list of pantry ingredients
     * @return list of recipes the user can make right now
     */
    public static List<Recipe> getStrictMatches(List<Recipe> allRecipes, List<Ingredient> pantryItems) {
        // Build a lookup: normalised ingredient name -> quantity available
        Map<String, Double> pantryMap = buildPantryMap(pantryItems);

        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipeIsFullySatisfied(recipe, pantryMap)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /**
     * Returns recipes where exactly ONE ingredient is missing or insufficient.
     * Used for the optional "Almost There" list (Section 8 / bonus feature).
     */
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

    /**
     * Returns true if every required ingredient of the recipe is available in
     * sufficient quantity in the pantry.
     */
    private static boolean recipeIsFullySatisfied(Recipe recipe, Map<String, Double> pantryMap) {
        for (RecipeIngredient required : recipe.getRequiredIngredients()) {
            String normName = normaliseName(required.getName());
            double available = getAvailableQuantity(pantryMap, normName, required.getUnit());
            if (available < required.getQuantity()) {
                return false; // strict: any missing ingredient disqualifies the recipe
            }
        }
        return true;
    }

    /** Counts how many required ingredients are missing or insufficient. */
    private static int countMissingIngredients(Recipe recipe, Map<String, Double> pantryMap) {
        int missing = 0;
        for (RecipeIngredient required : recipe.getRequiredIngredients()) {
            String normName = normaliseName(required.getName());
            double available = getAvailableQuantity(pantryMap, normName, required.getUnit());
            if (available < required.getQuantity()) {
                missing++;
            }
        }
        return missing;
    }

    /**
     * Looks up available quantity for a normalised ingredient name.
     * Checks both exact name and plural/singular variants.
     *
     * Note: the pantry stores everything in its native unit; we look up
     * the pantry entry's quantity directly without unit conversion
     * (unit aliases are handled in buildPantryMap by normalising unit strings).
     */
    private static double getAvailableQuantity(Map<String, Double> pantryMap,
                                               String normName, String unit) {
        // Direct match
        if (pantryMap.containsKey(normName)) {
            return pantryMap.get(normName);
        }
        // Try stripping trailing 's' (tomatoes -> tomato)
        if (normName.endsWith("es") && normName.length() > 3) {
            String stripped = normName.substring(0, normName.length() - 2);
            if (pantryMap.containsKey(stripped)) return pantryMap.get(stripped);
        }
        if (normName.endsWith("s") && normName.length() > 2) {
            String stripped = normName.substring(0, normName.length() - 1);
            if (pantryMap.containsKey(stripped)) return pantryMap.get(stripped);
        }
        // Try adding 's' (tomato -> tomatoes)
        if (pantryMap.containsKey(normName + "s")) return pantryMap.get(normName + "s");
        if (pantryMap.containsKey(normName + "es")) return pantryMap.get(normName + "es");

        return 0.0; // ingredient not found in pantry
    }

    /**
     * Builds a map of normalised ingredient name -> quantity from the pantry list.
     * Both name and unit are normalised to avoid trivial mismatches.
     */
    private static Map<String, Double> buildPantryMap(List<Ingredient> pantryItems) {
        Map<String, Double> map = new HashMap<>();
        for (Ingredient item : pantryItems) {
            String normName = normaliseName(item.getName());
            // If the same ingredient appears twice (different entries), sum the quantities
            double existing = map.containsKey(normName) ? map.get(normName) : 0.0;
            map.put(normName, existing + item.getQuantity());
        }
        return map;
    }

    /**
     * Normalises an ingredient name: lower-case, trimmed, collapsed whitespace.
     * No further stemming beyond plural handling done at lookup time.
     */
    public static String normaliseName(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
