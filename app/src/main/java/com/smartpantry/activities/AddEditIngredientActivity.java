package com.smartpantry.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;

import com.smartpantry.R;
import com.smartpantry.database.DatabaseHelper;
import com.smartpantry.fragments.PantryFragment;
import com.smartpantry.models.Ingredient;

/**
 * AddEditIngredientActivity handles both creating a new pantry ingredient
 * and editing an existing one. It receives an optional Ingredient via Intent
 * (if editing) and performs input validation before saving to the database.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etName, etQuantity, etUnit, etExpiry;
    private DatabaseHelper db;
    private Ingredient existingIngredient; // null when adding a new ingredient
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = DatabaseHelper.getInstance(this);

        // Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar_add_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind views
        etName = findViewById(R.id.et_ingredient_name);
        etQuantity = findViewById(R.id.et_ingredient_quantity);
        etUnit = findViewById(R.id.et_ingredient_unit);
        etExpiry = findViewById(R.id.et_ingredient_expiry);
        Button btnSave = findViewById(R.id.btn_save_ingredient);

        // Check if we are editing an existing ingredient
        if (getIntent().hasExtra(PantryFragment.EXTRA_INGREDIENT)) {
            existingIngredient = getIntent().getParcelableExtra(PantryFragment.EXTRA_INGREDIENT);
            isEditMode = true;
        }

        if (isEditMode && existingIngredient != null) {
            setTitle("Edit Ingredient");
            // Pre-populate fields with existing data
            etName.setText(existingIngredient.getName());
            String qty = (existingIngredient.getQuantity() == Math.floor(existingIngredient.getQuantity()))
                    ? String.valueOf((int) existingIngredient.getQuantity())
                    : String.valueOf(existingIngredient.getQuantity());
            etQuantity.setText(qty);
            etUnit.setText(existingIngredient.getUnit());
            if (existingIngredient.getExpiryDate() != null) {
                etExpiry.setText(existingIngredient.getExpiryDate());
            }
        } else {
            setTitle("Add Ingredient");
        }

        btnSave.setOnClickListener(v -> saveIngredient());
    }

    /**
     * Validates all input fields and either inserts or updates the ingredient.
     * On success, finishes the Activity (returning to PantryFragment via onResume refresh).
     */
    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        // --- Input Validation ---
        if (TextUtils.isEmpty(name)) {
            etName.setError("Ingredient name is required");
            etName.requestFocus();
            return;
        }
        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                etQuantity.setError("Quantity must be greater than zero");
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Please enter a valid number");
            etQuantity.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(unit)) {
            etUnit.setError("Unit is required (e.g. g, ml, whole, tbsp)");
            etUnit.requestFocus();
            return;
        }
        // Validate optional expiry date format if provided
        if (!TextUtils.isEmpty(expiry) && !expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etExpiry.setError("Use format YYYY-MM-DD (e.g. 2025-12-31)");
            etExpiry.requestFocus();
            return;
        }

        String expiryValue = TextUtils.isEmpty(expiry) ? null : expiry;

        if (isEditMode && existingIngredient != null) {
            existingIngredient.setName(name);
            existingIngredient.setQuantity(quantity);
            existingIngredient.setUnit(unit);
            existingIngredient.setExpiryDate(expiryValue);
            int rows = db.updateIngredient(existingIngredient);
            if (rows > 0) {
                Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed – please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Ingredient newIngredient = new Ingredient(name, quantity, unit, expiryValue);
            long id = db.addIngredient(newIngredient);
            if (id != -1) {
                Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add ingredient – please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // back button closes this Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
