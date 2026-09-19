# Smart Pantry Manager

An Android app that helps you keep track of what's in your kitchen and tells you what you can cook with it.

**Module:** Mobile App Development 700, Richfield Graduate Institute of Technology
**Author:** Nonkululeko Malambo (402112101)
**Language / Tools:** Java, Android Studio, SQLite

---

## Description

Smart Pantry Manager lets you record the ingredients you have at home (name, quantity, unit) and then matches your pantry against a built-in recipe collection. Instead of scrolling through recipes you can't make, the app shows:

- **Recipes you can cook now:** every ingredient is in your pantry in a sufficient quantity.
- **Almost There:** recipes where exactly one ingredient is missing or short on quantity.

Matching updates live: add or edit a pantry item and the recipe lists change straight away.

### Features

- **Pantry management (full CRUD):** add, view, edit and delete pantry items (name, quantity, unit and optional expiry date).
- **Smart recipe matching:** strict quantity-based matching, with an "Almost There" category for near matches.
- **Unit conversion:** the matcher understands equivalent units (g ↔ kg, ml ↔ l, and tsp/tbsp/cup ↔ ml) and normalises unit aliases, so 500 g in the pantry satisfies a recipe asking for 0.5 kg.
- **Recipe detail screen:** the full ingredient list (with quantities and units), category and step-by-step preparation steps.
- **Input validation:** empty names, invalid quantities, missing units and impossible expiry dates are rejected with clear error messages. Quantities accept a decimal point or a decimal comma.
- **Persistent storage:** data survives closing and reopening the app.
- **Settings screen:** expiry alerts that highlight items expiring within 3 days, a saved unit-system preference, and a pantry item count.

---

## Why SQLite?

SQLite is the right storage choice for this app because:

1. **Structured, relational data.** Pantry items, recipes and recipe ingredients are naturally tabular, and recipes have a one-to-many relationship with their ingredients. A relational database models this directly, whereas key-value storage (SharedPreferences) or flat files would not. Foreign key enforcement is switched on so a recipe ingredient cannot reference a recipe that does not exist.
2. **Querying.** SQL gives ordered, filtered lookups over pantry and recipe data (for example, listing ingredients alphabetically or loading all ingredients for one recipe by its ID), while the matching logic itself is handled in Java by `RecipeMatcher`. All queries are parameterised.
3. **Persistence and reliability.** SQLite provides durable on-device storage with transactional safety, so pantry data is not lost when the app closes.
4. **Built into Android.** No external server, network connection or extra dependency is needed. The app works fully offline, which suits a kitchen setting.
5. **Lightweight.** The dataset is small and local, so a full client-server database would be unnecessary overhead.

User preferences (the Settings toggles) are stored separately in SharedPreferences, since they are simple key-value settings and not relational data.

---

## Setup and Run Instructions

### Requirements

- Android Studio (recent stable release)
- JDK 17 (bundled with recent Android Studio)
- An Android emulator or physical device (developed and tested on **Pixel 8, API 34**)
- Minimum SDK: 24 (Android 7.0 Nougat)

### Steps

1. **Clone the repository**
```
   git clone https://github.com/BellaBear23/SmartPantryManager.git
```
   (or download and extract the project ZIP).
2. **Open in Android Studio:** *File → Open* and select the `SmartPantryManager` folder.
3. **Sync Gradle:** Android Studio will prompt you to sync. Accept and wait for it to finish. An internet connection is needed the first time to download dependencies.
4. **Choose a device:** create an emulator via *Device Manager* (e.g. Pixel 8, API 34) or connect a physical device with USB debugging enabled.
5. **Run:** click the green **Run ▶** button, or press `Shift + F10`.

To build from a terminal instead, run `gradlew.bat assembleDebug` (Windows) or `./gradlew assembleDebug` (macOS/Linux) from the project folder.

### Notes

- No API keys or accounts are needed.
- The database is created automatically on first launch and is pre-populated with 20 sample recipes.
- If you keep the project in a synced folder such as OneDrive, pause syncing while building, as file locking can cause Gradle build failures.

---

## Project Structure

```
app/src/main/
├── java/.../        Activities, fragments, adapters, models,
│                    database helper, RecipeMatcher
├── res/layout/      Screen and list-item layouts
├── res/menu/        Menus
└── res/values/      Strings, colours, themes
```

---

## Repository

https://github.com/BellaBear23/SmartPantryManager
