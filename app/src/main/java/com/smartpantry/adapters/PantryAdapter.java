package com.smartpantry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.R;
import com.smartpantry.models.Ingredient;

import java.util.List;

/**
 * RecyclerView adapter for displaying pantry ingredients.
 * Each row shows the ingredient name, quantity/unit, and optional expiry date.
 * Provides callbacks for edit and delete actions via the PantryAdapterListener interface.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Callback interface so the hosting Activity/Fragment handles edit and delete events. */
    public interface PantryAdapterListener {
        void onEditIngredient(Ingredient ingredient);
        void onDeleteIngredient(Ingredient ingredient);
    }

    private List<Ingredient> ingredients;
    private final PantryAdapterListener listener;

    public PantryAdapter(List<Ingredient> ingredients, PantryAdapterListener listener) {
        this.ingredients = ingredients;
        this.listener = listener;
    }

    /** Replace the dataset and refresh the list. */
    public void updateData(List<Ingredient> newData) {
        this.ingredients = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        holder.tvName.setText(capitalise(ingredient.getName()));

        // Format quantity: omit decimal if it is a whole number
        String qty = (ingredient.getQuantity() == Math.floor(ingredient.getQuantity()))
                ? String.valueOf((int) ingredient.getQuantity())
                : String.valueOf(ingredient.getQuantity());
        holder.tvQuantity.setText(qty + " " + ingredient.getUnit());

        if (ingredient.getExpiryDate() != null && !ingredient.getExpiryDate().isEmpty()) {
            holder.tvExpiry.setVisibility(View.VISIBLE);
            holder.tvExpiry.setText("Expires: " + ingredient.getExpiryDate());
        } else {
            holder.tvExpiry.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditIngredient(ingredient));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteIngredient(ingredient));
    }

    @Override
    public int getItemCount() {
        return ingredients == null ? 0 : ingredients.size();
    }

    /** Capitalise the first letter of an ingredient name for display. */
    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // --- ViewHolder ---

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvExpiry;
        ImageButton btnEdit, btnDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ingredient_name);
            tvQuantity = itemView.findViewById(R.id.tv_ingredient_quantity);
            tvExpiry = itemView.findViewById(R.id.tv_ingredient_expiry);
            btnEdit = itemView.findViewById(R.id.btn_edit_ingredient);
            btnDelete = itemView.findViewById(R.id.btn_delete_ingredient);
        }
    }
}
