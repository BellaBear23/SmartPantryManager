package com.smartpantry.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpantry.R;
import com.smartpantry.activities.AddEditIngredientActivity;
import com.smartpantry.adapters.PantryAdapter;
import com.smartpantry.database.DatabaseHelper;
import com.smartpantry.models.Ingredient;

import java.util.List;

public class PantryFragment extends Fragment implements PantryAdapter.PantryAdapterListener {

    public static final String EXTRA_INGREDIENT = "ingredient";

    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());

        tvEmpty = view.findViewById(R.id.tv_pantry_empty);
        recyclerView = view.findViewById(R.id.rv_pantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Ingredient> ingredients = db.getAllIngredients();
        adapter = new PantryAdapter(ingredients, this);
        recyclerView.setAdapter(adapter);

        updateEmptyState(ingredients);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_ingredient);
        fab.setOnClickListener(v -> openAddEditScreen(null));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void openAddEditScreen(@Nullable Ingredient ingredient) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        if (ingredient != null) {
            intent.putExtra(EXTRA_INGREDIENT, ingredient);
        }
        startActivity(intent);
    }

    @Override
    public void onEditIngredient(Ingredient ingredient) {
        openAddEditScreen(ingredient);
    }

    @Override
    public void onDeleteIngredient(Ingredient ingredient) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete ingredient")
                .setMessage("Remove \"" + ingredient.getName() + "\" from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteIngredient(ingredient.getId());
                    Toast.makeText(requireContext(), ingredient.getName() + " removed", Toast.LENGTH_SHORT).show();
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshList() {
        List<Ingredient> updated = db.getAllIngredients();
        adapter.updateData(updated);
        updateEmptyState(updated);
    }

    private void updateEmptyState(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}