package com.smartpantry.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartpantry.R;
import com.smartpantry.fragments.PantryFragment;
import com.smartpantry.fragments.SuggestedRecipesFragment;
import com.smartpantry.fragments.SettingsFragment;

/**
 * MainActivity hosts the bottom navigation bar and swaps between
 * the three main Fragments: Pantry, Suggested Recipes, and Settings.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Load pantry fragment on start
        if (savedInstanceState == null) {
            loadFragment(new PantryFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                selected = new PantryFragment();
            } else if (id == R.id.nav_recipes) {
                selected = new SuggestedRecipesFragment();
            } else if (id == R.id.nav_settings) {
                selected = new SettingsFragment();
            } else {
                return false;
            }
            loadFragment(selected);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
