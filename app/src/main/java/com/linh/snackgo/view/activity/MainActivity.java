package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.badge.BadgeDrawable;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.CartController;
import com.linh.snackgo.databinding.ActivityMainBinding;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.view.fragment.CartFragment;
import com.linh.snackgo.view.fragment.HomeFragment;
import com.linh.snackgo.view.fragment.OrdersFragment;
import com.linh.snackgo.view.fragment.ProfileFragment;

/**
 * Main Shell Activity hosting Home, Cart, Orders, and Profile tabs with a BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_TAB = "key_selected_tab";

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private CartController cartController;
    private int currentSelectedTabId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartController = new CartController(this);

        if (savedInstanceState != null) {
            currentSelectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_home);
        }

        setupBottomNavigation();
        updateCartBadge();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(IntentKeys.EXTRA_OPEN_TAB)) {
            int tabId = intent.getIntExtra(IntentKeys.EXTRA_OPEN_TAB, R.id.nav_home);
            navigateToTab(tabId);
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentSelectedTabId && getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) != null) {
                return true;
            }
            currentSelectedTabId = itemId;
            switchFragment(itemId);
            return true;
        });

        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) == null) {
            binding.bottomNav.setSelectedItemId(currentSelectedTabId);
        }
    }

    private void switchFragment(int menuItemId) {
        Fragment fragment;
        if (menuItemId == R.id.nav_cart) {
            fragment = new CartFragment();
        } else if (menuItemId == R.id.nav_orders) {
            fragment = new OrdersFragment();
        } else if (menuItemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
        } else {
            fragment = new HomeFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void navigateToTab(int menuItemId) {
        binding.bottomNav.setSelectedItemId(menuItemId);
    }


    public void updateCartBadge() {
        long userId = sessionManager.getUserId();
        int cartCount = cartController.getCartCount(userId);

        BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart);
        if (cartCount > 0) {
            badge.setNumber(cartCount);
            badge.setBackgroundColor(getColor(R.color.accent_orange));
            badge.setBadgeTextColor(getColor(R.color.surface));
            badge.setVisible(true);
        } else {
            badge.setVisible(false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, currentSelectedTabId);
    }
}
