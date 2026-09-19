package com.smartpantry.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.R;
import com.smartpantry.fragments.SettingsFragment;
import com.smartpantry.models.Ingredient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

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

        String qty = (ingredient.getQuantity() == Math.floor(ingredient.getQuantity()))
                ? String.valueOf((int) ingredient.getQuantity())
                : String.valueOf(ingredient.getQuantity());
        holder.tvQuantity.setText(qty + " " + ingredient.getUnit());

        boolean alertsOn = holder.itemView.getContext()
                .getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(SettingsFragment.KEY_EXPIRY_ALERTS, true);

        String expiry = ingredient.getExpiryDate();
        if (expiry != null && !expiry.isEmpty()) {
            boolean soon = alertsOn && isExpiringSoon(expiry);
            holder.tvExpiry.setVisibility(View.VISIBLE);
            holder.tvExpiry.setText("Expires: " + expiry + (soon ? " (expiring soon)" : ""));
            holder.tvExpiry.setTextColor(soon ? Color.RED : holder.defaultExpiryColor);
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

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private boolean isExpiringSoon(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            Date date = format.parse(value);
            long threeDays = 3L * 24 * 60 * 60 * 1000;
            return date != null && date.getTime() <= System.currentTimeMillis() + threeDays;
        } catch (ParseException e) {
            return false;
        }
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvExpiry;
        ImageButton btnEdit, btnDelete;
        int defaultExpiryColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ingredient_name);
            tvQuantity = itemView.findViewById(R.id.tv_ingredient_quantity);
            tvExpiry = itemView.findViewById(R.id.tv_ingredient_expiry);
            defaultExpiryColor = tvExpiry.getCurrentTextColor();
            btnEdit = itemView.findViewById(R.id.btn_edit_ingredient);
            btnDelete = itemView.findViewById(R.id.btn_delete_ingredient);
        }
    }
}