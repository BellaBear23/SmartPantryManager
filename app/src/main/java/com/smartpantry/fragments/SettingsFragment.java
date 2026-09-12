package com.smartpantry.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smartpantry.R;
import com.smartpantry.database.DatabaseHelper;

/**
 * SettingsFragment allows the user to configure app preferences:
 *  - Toggle expiring-soon alerts (persisted via SharedPreferences).
 *  - Choose preferred unit system (metric / imperial).
 * Also shows basic pantry statistics.
 */
public class SettingsFragment extends Fragment {

    public static final String PREFS_NAME = "SmartPantryPrefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_UNIT_SYSTEM = "unit_system";

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Expiry alerts toggle
        Switch switchExpiry = view.findViewById(R.id.switch_expiry_alerts);
        switchExpiry.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        switchExpiry.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        // Unit system toggle (metric default)
        Switch switchUnits = view.findViewById(R.id.switch_unit_system);
        switchUnits.setChecked(prefs.getBoolean(KEY_UNIT_SYSTEM, false)); // false = metric
        switchUnits.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean(KEY_UNIT_SYSTEM, isChecked).apply());

        // Pantry stats
        TextView tvStats = view.findViewById(R.id.tv_pantry_stats);
        int pantryCount = DatabaseHelper.getInstance(requireContext()).getAllIngredients().size();
        tvStats.setText("Pantry items: " + pantryCount);
    }
}
