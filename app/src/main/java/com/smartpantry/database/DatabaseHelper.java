package com.smartpantry.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartpantry.models.Ingredient;
import com.smartpantry.models.Recipe;
import com.smartpantry.models.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper manages the SQLite database for the Smart Pantry Manager app.
 * Implements full CRUD for pantry ingredients and provides read access to seeded recipes.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // --- Database metadata ---
    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 1;

    // --- Table: pantry_ingredients ---
    public static final String TABLE_PANTRY = "pantry_ingredients";
    public static final String COL_PANTRY_ID = "_id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date"; // nullable, format YYYY-MM-DD

    // --- Table: recipes ---
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";
    public static final String COL_RECIPE_CATEGORY = "category";

    // --- Table: recipe_ingredients ---
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    // --- Singleton instance ---
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create pantry table
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QUANTITY + " REAL NOT NULL DEFAULT 0, " +
                COL_PANTRY_UNIT + " TEXT NOT NULL, " +
                COL_PANTRY_EXPIRY + " TEXT" +
                ");");

        // Create recipes table
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT NOT NULL, " +
                COL_RECIPE_CATEGORY + " TEXT NOT NULL" +
                ");");

        // Create recipe_ingredients table
        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QUANTITY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + ")" +
                ");");

        // Seed the database with 20 recipes
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ============================================================
    // PANTRY CRUD OPERATIONS
    // ============================================================

    /** Insert a new pantry ingredient. Returns new row ID or -1 on failure. */
    public long addIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, ingredient.getName().trim().toLowerCase());
        cv.put(COL_PANTRY_QUANTITY, ingredient.getQuantity());
        cv.put(COL_PANTRY_UNIT, ingredient.getUnit().trim().toLowerCase());
        cv.put(COL_PANTRY_EXPIRY, ingredient.getExpiryDate());
        long id = db.insert(TABLE_PANTRY, null, cv);
        db.close();
        return id;
    }

    /** Fetch all pantry ingredients ordered by name. */
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null, COL_PANTRY_NAME + " ASC");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToIngredient(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    /** Fetch a single pantry ingredient by ID. */
    public Ingredient getIngredientById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Ingredient ingredient = null;
        if (cursor.moveToFirst()) {
            ingredient = cursorToIngredient(cursor);
        }
        cursor.close();
        db.close();
        return ingredient;
    }

    /** Update an existing pantry ingredient. Returns rows affected. */
    public int updateIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, ingredient.getName().trim().toLowerCase());
        cv.put(COL_PANTRY_QUANTITY, ingredient.getQuantity());
        cv.put(COL_PANTRY_UNIT, ingredient.getUnit().trim().toLowerCase());
        cv.put(COL_PANTRY_EXPIRY, ingredient.getExpiryDate());
        int rows = db.update(TABLE_PANTRY, cv, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(ingredient.getId())});
        db.close();
        return rows;
    }

    /** Delete a pantry ingredient by ID. Returns rows affected. */
    public int deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /** Helper: map a Cursor row to an Ingredient object. */
    private Ingredient cursorToIngredient(Cursor c) {
        return new Ingredient(
                c.getLong(c.getColumnIndexOrThrow(COL_PANTRY_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PANTRY_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_EXPIRY))
        );
    }

    // ============================================================
    // RECIPE READ OPERATIONS
    // ============================================================

    /** Fetch all recipes with their ingredient lists. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null, COL_RECIPE_NAME + " ASC");
        if (cursor.moveToFirst()) {
            do {
                Recipe r = cursorToRecipe(cursor);
                r.setRequiredIngredients(getRecipeIngredients(db, r.getId()));
                recipes.add(r);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recipes;
    }

    /** Fetch a single recipe with its ingredient list by recipe ID. */
    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
            recipe.setRequiredIngredients(getRecipeIngredients(db, recipe.getId()));
        }
        cursor.close();
        db.close();
        return recipe;
    }

    /** Fetch the list of required ingredients for a given recipe. */
    private List<RecipeIngredient> getRecipeIngredients(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null,
                COL_RI_RECIPE_ID + "=?", new String[]{String.valueOf(recipeId)},
                null, null, COL_RI_NAME + " ASC");
        if (cursor.moveToFirst()) {
            do {
                list.add(new RecipeIngredient(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_RI_ID)),
                        recipeId,
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RI_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_UNIT))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /** Helper: map a Cursor row to a Recipe object (without ingredients). */
    private Recipe cursorToRecipe(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_CATEGORY))
        );
    }

    // ============================================================
    // SEED DATA – 20 recipes inserted at database creation
    // ============================================================

    /**
     * Inserts 20 predefined recipes and their ingredients into the database.
     * Called only from onCreate, so it runs exactly once on first launch.
     */
    private void seedRecipes(SQLiteDatabase db) {
        // Helper lambdas are not available in Java 7/8 Android minimum, so we use a helper method.
        long r1 = insertRecipe(db, "Spaghetti Bolognese", "Breakfast",
                "1. Brown the mince in a pan.\n2. Add onion and garlic, cook 5 min.\n" +
                "3. Stir in tomato paste and canned tomatoes.\n4. Season and simmer 20 min.\n" +
                "5. Cook spaghetti per packet. Serve with sauce.");
        addRI(db, r1, "spaghetti", 200, "g");
        addRI(db, r1, "beef mince", 300, "g");
        addRI(db, r1, "onion", 1, "whole");
        addRI(db, r1, "garlic", 2, "cloves");
        addRI(db, r1, "canned tomatoes", 400, "g");
        addRI(db, r1, "tomato paste", 2, "tbsp");

        long r2 = insertRecipe(db, "Vegetable Stir-Fry", "Vegetarian",
                "1. Heat oil in wok on high.\n2. Add garlic and ginger, 30 sec.\n" +
                "3. Add vegetables, stir-fry 5 min.\n4. Add soy sauce and sesame oil.\n" +
                "5. Serve immediately over rice.");
        addRI(db, r2, "broccoli", 150, "g");
        addRI(db, r2, "carrot", 1, "whole");
        addRI(db, r2, "bell pepper", 1, "whole");
        addRI(db, r2, "soy sauce", 3, "tbsp");
        addRI(db, r2, "garlic", 2, "cloves");
        addRI(db, r2, "ginger", 1, "tsp");
        addRI(db, r2, "sesame oil", 1, "tbsp");

        long r3 = insertRecipe(db, "Scrambled Eggs on Toast", "Breakfast",
                "1. Whisk eggs with milk, salt and pepper.\n2. Melt butter in pan over low heat.\n" +
                "3. Add egg mixture, stir slowly until just set.\n4. Toast bread.\n" +
                "5. Serve eggs on toast.");
        addRI(db, r3, "egg", 3, "whole");
        addRI(db, r3, "milk", 50, "ml");
        addRI(db, r3, "butter", 10, "g");
        addRI(db, r3, "bread", 2, "slices");

        long r4 = insertRecipe(db, "Chicken Fried Rice", "Main",
                "1. Cook rice and let it cool.\n2. Scramble egg in wok.\n" +
                "3. Add chicken pieces, cook through.\n4. Add rice, soy sauce and vegetables.\n" +
                "5. Stir-fry until heated. Serve hot.");
        addRI(db, r4, "rice", 200, "g");
        addRI(db, r4, "chicken breast", 200, "g");
        addRI(db, r4, "egg", 2, "whole");
        addRI(db, r4, "soy sauce", 2, "tbsp");
        addRI(db, r4, "onion", 1, "whole");
        addRI(db, r4, "garlic", 1, "cloves");

        long r5 = insertRecipe(db, "Banana Pancakes", "Breakfast",
                "1. Mash banana in bowl.\n2. Add eggs and mix well.\n" +
                "3. Add flour and milk to form batter.\n4. Cook in buttered pan, 2 min per side.\n" +
                "5. Serve with honey or syrup.");
        addRI(db, r5, "banana", 2, "whole");
        addRI(db, r5, "egg", 2, "whole");
        addRI(db, r5, "flour", 100, "g");
        addRI(db, r5, "milk", 100, "ml");
        addRI(db, r5, "butter", 15, "g");

        long r6 = insertRecipe(db, "Tomato Soup", "Soup",
                "1. Sauté onion and garlic in butter.\n2. Add canned tomatoes and stock.\n" +
                "3. Simmer 15 min then blend smooth.\n4. Season with salt, pepper and basil.\n" +
                "5. Serve with crusty bread.");
        addRI(db, r6, "canned tomatoes", 800, "g");
        addRI(db, r6, "onion", 1, "whole");
        addRI(db, r6, "garlic", 2, "cloves");
        addRI(db, r6, "vegetable stock", 500, "ml");
        addRI(db, r6, "butter", 20, "g");
        addRI(db, r6, "dried basil", 1, "tsp");

        long r7 = insertRecipe(db, "Greek Salad", "Salad",
                "1. Chop tomato, cucumber and onion.\n2. Add olives and feta.\n" +
                "3. Drizzle with olive oil.\n4. Season with salt, pepper and oregano.\n" +
                "5. Toss gently and serve.");
        addRI(db, r7, "tomato", 2, "whole");
        addRI(db, r7, "cucumber", 1, "whole");
        addRI(db, r7, "red onion", 0.5, "whole");
        addRI(db, r7, "feta cheese", 100, "g");
        addRI(db, r7, "olives", 50, "g");
        addRI(db, r7, "olive oil", 3, "tbsp");

        long r8 = insertRecipe(db, "Omelette", "Breakfast",
                "1. Whisk eggs with salt and pepper.\n2. Heat butter in pan.\n" +
                "3. Pour in eggs, tilt to spread.\n4. Add cheese and fold in half.\n" +
                "5. Slide onto plate and serve.");
        addRI(db, r8, "egg", 3, "whole");
        addRI(db, r8, "butter", 10, "g");
        addRI(db, r8, "cheddar cheese", 30, "g");
        addRI(db, r8, "milk", 2, "tbsp");

        long r9 = insertRecipe(db, "Lentil Soup", "Soup",
                "1. Sauté onion, garlic and cumin in oil.\n2. Add lentils and stock.\n" +
                "3. Simmer 25 min until lentils are soft.\n4. Season with salt, pepper and lemon juice.\n" +
                "5. Serve with bread.");
        addRI(db, r9, "red lentils", 200, "g");
        addRI(db, r9, "onion", 1, "whole");
        addRI(db, r9, "garlic", 3, "cloves");
        addRI(db, r9, "vegetable stock", 1000, "ml");
        addRI(db, r9, "cumin", 1, "tsp");
        addRI(db, r9, "lemon juice", 2, "tbsp");
        addRI(db, r9, "olive oil", 2, "tbsp");

        long r10 = insertRecipe(db, "Pasta Aglio e Olio", "Main",
                "1. Cook pasta in salted water.\n2. Gently warm garlic slices in olive oil.\n" +
                "3. Add chilli flakes and parsley.\n4. Toss cooked pasta in the oil.\n" +
                "5. Finish with parmesan and black pepper.");
        addRI(db, r10, "spaghetti", 200, "g");
        addRI(db, r10, "garlic", 4, "cloves");
        addRI(db, r10, "olive oil", 60, "ml");
        addRI(db, r10, "chilli flakes", 0.5, "tsp");
        addRI(db, r10, "parsley", 2, "tbsp");
        addRI(db, r10, "parmesan", 30, "g");

        long r11 = insertRecipe(db, "Tuna Pasta Bake", "Main",
                "1. Cook pasta and drain.\n2. Mix tuna, sweetcorn and cream.\n" +
                "3. Combine with pasta.\n4. Top with cheese.\n" +
                "5. Bake at 180°C for 20 min.");
        addRI(db, r11, "pasta", 200, "g");
        addRI(db, r11, "canned tuna", 160, "g");
        addRI(db, r11, "sweetcorn", 100, "g");
        addRI(db, r11, "cream", 200, "ml");
        addRI(db, r11, "cheddar cheese", 80, "g");

        long r12 = insertRecipe(db, "Fried Rice with Egg", "Main",
                "1. Cook rice ahead and refrigerate.\n2. Heat oil, fry garlic.\n" +
                "3. Add cold rice, stir-fry 3 min.\n4. Push aside, scramble eggs in.\n" +
                "5. Add soy sauce and spring onion.");
        addRI(db, r12, "rice", 200, "g");
        addRI(db, r12, "egg", 2, "whole");
        addRI(db, r12, "soy sauce", 2, "tbsp");
        addRI(db, r12, "garlic", 2, "cloves");
        addRI(db, r12, "spring onion", 2, "whole");
        addRI(db, r12, "vegetable oil", 2, "tbsp");

        long r13 = insertRecipe(db, "Guacamole", "Snack",
                "1. Halve and stone avocados.\n2. Scoop flesh into bowl.\n" +
                "3. Mash with fork.\n4. Add lime juice, salt and chilli.\n" +
                "5. Mix in diced tomato and coriander. Serve fresh.");
        addRI(db, r13, "avocado", 2, "whole");
        addRI(db, r13, "lime juice", 1, "tbsp");
        addRI(db, r13, "tomato", 1, "whole");
        addRI(db, r13, "coriander", 2, "tbsp");
        addRI(db, r13, "chilli flakes", 0.25, "tsp");

        long r14 = insertRecipe(db, "Cheese Toastie", "Snack",
                "1. Butter outsides of two bread slices.\n2. Place cheese between unbuttered sides.\n" +
                "3. Cook in dry pan on medium heat.\n4. Press down, 2-3 min per side.\n" +
                "5. Serve immediately while cheese is melted.");
        addRI(db, r14, "bread", 2, "slices");
        addRI(db, r14, "cheddar cheese", 60, "g");
        addRI(db, r14, "butter", 15, "g");

        long r15 = insertRecipe(db, "Potato and Egg Frittata", "Breakfast",
                "1. Boil potato cubes 10 min, drain.\n2. Whisk eggs with milk and seasoning.\n" +
                "3. Fry potato in ovenproof pan.\n4. Pour egg over potato.\n" +
                "5. Cook stovetop 2 min, then bake 180°C for 10 min.");
        addRI(db, r15, "potato", 2, "whole");
        addRI(db, r15, "egg", 4, "whole");
        addRI(db, r15, "milk", 60, "ml");
        addRI(db, r15, "olive oil", 2, "tbsp");
        addRI(db, r15, "onion", 0.5, "whole");

        long r16 = insertRecipe(db, "Garlic Butter Pasta", "Main",
                "1. Boil pasta until al dente.\n2. Melt butter in pan, add garlic.\n" +
                "3. Drain pasta, add to pan.\n4. Toss, add parsley and parmesan.\n" +
                "5. Serve immediately.");
        addRI(db, r16, "pasta", 200, "g");
        addRI(db, r16, "butter", 40, "g");
        addRI(db, r16, "garlic", 3, "cloves");
        addRI(db, r16, "parsley", 2, "tbsp");
        addRI(db, r16, "parmesan", 30, "g");

        long r17 = insertRecipe(db, "Baked Potato", "Side",
                "1. Scrub potato and prick with fork.\n2. Rub with olive oil and salt.\n" +
                "3. Bake at 200°C for 60 min.\n4. Split open, add butter.\n" +
                "5. Top with cheese and sour cream.");
        addRI(db, r17, "potato", 2, "whole");
        addRI(db, r17, "olive oil", 1, "tbsp");
        addRI(db, r17, "butter", 20, "g");
        addRI(db, r17, "cheddar cheese", 50, "g");
        addRI(db, r17, "sour cream", 50, "ml");

        long r18 = insertRecipe(db, "Avocado Toast", "Breakfast",
                "1. Toast bread slices.\n2. Halve and stone avocado.\n" +
                "3. Mash avocado with lime juice and salt.\n4. Spread onto toast.\n" +
                "5. Top with chilli flakes and serve.");
        addRI(db, r18, "bread", 2, "slices");
        addRI(db, r18, "avocado", 1, "whole");
        addRI(db, r18, "lime juice", 1, "tbsp");
        addRI(db, r18, "chilli flakes", 0.25, "tsp");

        long r19 = insertRecipe(db, "Chickpea Curry", "Main",
                "1. Sauté onion and garlic in oil.\n2. Add curry powder, cook 1 min.\n" +
                "3. Add canned chickpeas and tomatoes.\n4. Simmer 15 min.\n" +
                "5. Season and serve over rice with coriander.");
        addRI(db, r19, "canned chickpeas", 400, "g");
        addRI(db, r19, "canned tomatoes", 400, "g");
        addRI(db, r19, "onion", 1, "whole");
        addRI(db, r19, "garlic", 3, "cloves");
        addRI(db, r19, "curry powder", 2, "tsp");
        addRI(db, r19, "vegetable oil", 2, "tbsp");
        addRI(db, r19, "coriander", 2, "tbsp");

        long r20 = insertRecipe(db, "French Toast", "Breakfast",
                "1. Whisk eggs, milk, sugar and cinnamon.\n2. Dip bread slices in mixture.\n" +
                "3. Fry in buttered pan 2 min per side.\n4. Serve warm with syrup or fruit.\n" +
                "5. Dust with icing sugar if desired.");
        addRI(db, r20, "bread", 4, "slices");
        addRI(db, r20, "egg", 2, "whole");
        addRI(db, r20, "milk", 60, "ml");
        addRI(db, r20, "butter", 15, "g");
        addRI(db, r20, "cinnamon", 0.5, "tsp");
        addRI(db, r20, "sugar", 1, "tbsp");
    }

    /** Helper: insert a recipe row and return its ID. */
    private long insertRecipe(SQLiteDatabase db, String name, String category, String steps) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME, name);
        cv.put(COL_RECIPE_CATEGORY, category);
        cv.put(COL_RECIPE_STEPS, steps);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    /** Helper: insert a recipe ingredient row. */
    private void addRI(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RI_RECIPE_ID, recipeId);
        cv.put(COL_RI_NAME, name);
        cv.put(COL_RI_QUANTITY, qty);
        cv.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
    }
}
