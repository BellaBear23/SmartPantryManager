package com.smartpantry.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.R;
import com.smartpantry.adapters.RecipeIngredientAdapter;
import com.smartpantry.database.DatabaseHelper;
import com.smartpantry.models.Recipe;

/**
 * RecipeDetailActivity displays the full detail of a selected recipe:
 * its name, category, required ingredients list, and preparation steps.
 * It receives the recipe ID via Intent extra and re-queries the database.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar_recipe_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Recipe recipe = DatabaseHelper.getInstance(this).getRecipeById(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind data to views
        setTitle(recipe.getName());

        TextView tvCategory = findViewById(R.id.tv_detail_category);
        tvCategory.setText(recipe.getCategory());

        TextView tvSteps = findViewById(R.id.tv_detail_steps);
        tvSteps.setText(recipe.getSteps());

        RecyclerView rvIngredients = findViewById(R.id.rv_detail_ingredients);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(new RecipeIngredientAdapter(recipe.getRequiredIngredients()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
