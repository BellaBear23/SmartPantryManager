package com.smartpantry.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.smartpantry.R;

/**
 * SplashActivity is the launcher Activity.
 * It shows the app logo for 1.5 seconds then navigates to MainActivity.
 * Also triggers database initialisation on the first run.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Use a Handler on the main Looper to delay the transition.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // remove splash from back-stack
        }, SPLASH_DELAY_MS);
    }
}
