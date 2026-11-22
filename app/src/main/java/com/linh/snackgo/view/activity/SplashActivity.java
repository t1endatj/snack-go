package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.linh.snackgo.utils.ProductImageSyncer;
import com.linh.snackgo.utils.SessionManager;
import java.util.concurrent.Executors;

/**
 * Splash launcher Activity dispatching users to Onboarding, Main, or Login while syncing asset product images.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        // Run asset image sync on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            new ProductImageSyncer(SplashActivity.this).syncAssetImages();
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!sessionManager.isOnboardingDone()) {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            } else if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 300);
    }
}
