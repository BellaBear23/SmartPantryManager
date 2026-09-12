package com.smartpantry.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.R;
import com.smartpantry.activities.RecipeDetailActivity;
import com.smartpantry.adapters.RecipeAdapter;
import com.smartpantry.database.DatabaseHelper;
import com.smartpantry.database.RecipeMatcher;
import com.smartpantry.models.Ingredient;
import com.smartpantry.models.Recipe;

import java.util.List;

/**
 * SuggestedRecipesFragment runs the strict-matching algorithm on the current pantry
 * and displays only recipes the user can make with ingredients they already have.
 *
 * A separate "Almost There" section (bonus feature) lists recipes missing exactly
 * one ingredient, clearly labelled to separate it from strict matches.
 */
public class SuggestedRecipesFragment extends Fragment implements RecipeAdapter.RecipeClickListener {

    private RecyclerView rvStrict, rvAlmost;
    private RecipeAdapter strictAdapter, almostAdapter;
    private TextView tvNoMatches, tvAlmostHeader, tvStrictHeader;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());

        tvNoMatches = view.findViewById(R.id.tv_no_matches);
        tvStrictHeader = view.findViewById(R.id.tv_strict_header);
        tvAlmostHeader = view.findViewById(R.id.tv_almost_header);

        rvStrict = view.findViewById(R.id.rv_strict_recipes);
        rvStrict.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvAlmost = view.findViewById(R.id.rv_almost_recipes);
        rvAlmost.setLayoutManager(new LinearLayoutManager(requireContext()));

        runMatching();
    }

    @Override
    public void onResume() {
        super.onResume();
        runMatching(); // re-run whenever pantry may have changed
    }

    /**
     * Loads pantry and all recipes, runs strict and almost-there matching,
     * then updates the UI accordingly.
     */
    private void runMatching() {
        List<Ingredient> pantry = db.getAllIngredients();
        List<Recipe> allRecipes = db.getAllRecipes();

        List<Recipe> strictMatches = RecipeMatcher.getStrictMatches(allRecipes, pantry);
        List<Recipe> almostMatches = RecipeMatcher.getAlmostMatches(allRecipes, pantry);

        // -- Strict matches section --
        if (strictMatches.isEmpty()) {
            tvStrictHeader.setVisibility(View.GONE);
            rvStrict.setVisibility(View.GONE);
            tvNoMatches.setVisibility(View.VISIBLE);
            tvNoMatches.setText(pantry.isEmpty()
                    ? "Your pantry is empty – add ingredients to see suggested recipes."
                    : "No recipes match your pantry yet – add more ingredients.");
        } else {
            tvNoMatches.setVisibility(View.GONE);
            tvStrictHeader.setVisibility(View.VISIBLE);
            rvStrict.setVisibility(View.VISIBLE);
            if (strictAdapter == null) {
                strictAdapter = new RecipeAdapter(strictMatches, this);
                rvStrict.setAdapter(strictAdapter);
            } else {
                strictAdapter.updateData(strictMatches);
            }
        }

        // -- Almost-there section (bonus) --
        if (almostMatches.isEmpty()) {
            tvAlmostHeader.setVisibility(View.GONE);
            rvAlmost.setVisibility(View.GONE);
        } else {
            tvAlmostHeader.setVisibility(View.VISIBLE);
            rvAlmost.setVisibility(View.VISIBLE);
            if (almostAdapter == null) {
                almostAdapter = new RecipeAdapter(almostMatches, this);
                rvAlmost.setAdapter(almostAdapter);
            } else {
                almostAdapter.updateData(almostMatches);
            }
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
