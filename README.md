# Smart Pantry Manager

A Java Android application that suggests recipes based **strictly** on the ingredients you already have at home — helping reduce food waste without requiring a shopping trip.

## App Description

Smart Pantry Manager lets you:
- **Track your pantry** – add, edit, and delete ingredients with name, quantity, unit, and optional expiry date.
- **Get recipe suggestions** – see only recipes you can make *right now* using the strict-matching algorithm.
- **Almost There** – a bonus section shows recipes missing just one ingredient.
- **Settings** – toggle expiry alerts and choose metric vs imperial units.

The core value of the app is the **strict-matching rule**: a recipe only appears in suggestions if *every* ingredient it requires is present in your pantry in sufficient quantity. Partial matches are excluded.

## Database Choice: SQLite (via SQLiteOpenHelper)

SQLite was chosen because:
1. It is covered in the module's persistent data chapter and integrates natively with Android with no extra dependencies.
2. The data is entirely local to the user's device — there is no need for cloud sync for a personal pantry tracker.
3. SQLiteOpenHelper gives full control over schema creation, upgrades, and raw queries, making the implementation transparent and easy to explain in the video.
4. No API keys, network permissions, or external services are required, keeping setup simple.

## Setup & Run Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1) or later
- Android SDK API 24+ (minSdk 24, targetSdk 34)
- JDK 8 or higher

### Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/SmartPantryManager.git
   cd SmartPantryManager
   ```
2. **Open in Android Studio**
   - Select *File → Open* and navigate to the cloned `SmartPantryManager` folder.
   - Wait for Gradle sync to complete (requires internet on first run to download dependencies).

3. **Run the app**
   - Connect a physical device (USB debugging enabled) **or** start an AVD (API 24+) via the AVD Manager.
   - Click the green **Run ▶** button or press `Shift+F10`.

4. **First launch**
   - The database is created and seeded with 20 recipes automatically on first run.
   - No additional setup or data import is required.

### Permissions
No special Android permissions are required. The app uses only on-device SQLite storage.

## Project Structure

```
app/src/main/
├── java/com/smartpantry/
│   ├── activities/         # SplashActivity, MainActivity, AddEditIngredientActivity,
│   │                       #   RecipeDetailActivity, SettingsActivity
│   ├── adapters/           # PantryAdapter, RecipeAdapter, RecipeIngredientAdapter
│   ├── database/           # DatabaseHelper (SQLiteOpenHelper), RecipeMatcher
│   ├── fragments/          # PantryFragment, SuggestedRecipesFragment, SettingsFragment
│   └── models/             # Ingredient, Recipe, RecipeIngredient
└── res/
    ├── layout/             # All XML layouts
    ├── menu/               # bottom_nav_menu.xml
    ├── values/             # colors, strings, themes
    └── drawable/           # circle_green shape
```

## Module: Mobile App Development 700 — Richfield Graduate Institute of Technology
