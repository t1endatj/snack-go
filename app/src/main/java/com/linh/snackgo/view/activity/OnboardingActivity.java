package com.linh.snackgo.view.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.linh.snackgo.R;
import com.linh.snackgo.databinding.ActivityOnboardingBinding;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;
import com.linh.snackgo.view.adapter.OnboardingAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding ViewPager2 Activity shown once to new users.
 */
public class OnboardingActivity extends AppCompatActivity {
    private static final String TAG = "OnboardingActivity";
    private ActivityOnboardingBinding binding;
    private SessionManager sessionManager;
    private OnboardingAdapter adapter;
    private View[] dotViews;
    private int currentPosition = 0;
    private ObjectAnimator floatingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (sessionManager.isOnboardingDone()) {
            navigateToNextScreen();
            return;
        }

        setupEdgeToEdge();

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handleWindowInsets();
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        setupViewPager();
        setupFloatingAnimation();
        setupListeners();
    }

    private void setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(true);
        }
    }

    private void handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnSkip, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = statusBarHeight + (int) (16 * getResources().getDisplayMetrics().density);
            v.setLayoutParams(lp);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomSheet, (v, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int bottomPadding = (int) (20 * getResources().getDisplayMetrics().density) + navBarHeight;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomPadding);
            return insets;
        });
    }

    private void navigateToNextScreen() {
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void setupFloatingAnimation() {
        float distance = -8f * getResources().getDisplayMetrics().density;
        floatingAnimator = ObjectAnimator.ofFloat(binding.viewPagerOnboarding, "translationY", 0f, distance);
        floatingAnimator.setDuration(2400);
        floatingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        floatingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatingAnimator.start();
    }

    private void setupViewPager() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.il_onboarding_snacks);
        images.add(R.drawable.il_onboarding_order);
        images.add(R.drawable.il_onboarding_delivery);

        String[] titles = new String[]{
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_title_3)
        };

        String[] descriptions = new String[]{
                getString(R.string.onboarding_desc_1),
                getString(R.string.onboarding_desc_2),
                getString(R.string.onboarding_desc_3)
        };

        adapter = new OnboardingAdapter(images);
        binding.viewPagerOnboarding.setAdapter(adapter);

        setupDotsIndicator(images.size());

        binding.viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= 0 && position < titles.length) {
                    binding.tvSheetTitle.setText(titles[position]);
                    binding.tvSheetDesc.setText(descriptions[position]);
                }
                if (position == images.size() - 1) {
                    binding.btnAction.setText(R.string.label_start);
                } else {
                    binding.btnAction.setText(R.string.label_next);
                }
                animateDotSelection(position);
                currentPosition = position;
            }
        });
    }

    private void setupDotsIndicator(int count) {
        binding.layoutDots.removeAllViews();
        dotViews = new View[count];
        float density = getResources().getDisplayMetrics().density;
        int height = (int) (8 * density);
        int inactiveWidth = (int) (8 * density);
        int activeWidth = (int) (24 * density);
        int margin = (int) (3 * density);

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            int width = (i == 0) ? activeWidth : inactiveWidth;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
            lp.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource((i == 0) ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive);
            dotViews[i] = dot;
            binding.layoutDots.addView(dot);
        }
    }

    private void animateDotSelection(int newPosition) {
        if (dotViews == null || newPosition == currentPosition) return;

        float density = getResources().getDisplayMetrics().density;
        int inactiveWidth = (int) (8 * density);
        int activeWidth = (int) (24 * density);

        if (currentPosition >= 0 && currentPosition < dotViews.length) {
            View prevDot = dotViews[currentPosition];
            prevDot.setBackgroundResource(R.drawable.bg_indicator_inactive);
            animateViewWidth(prevDot, activeWidth, inactiveWidth);
        }

        if (newPosition >= 0 && newPosition < dotViews.length) {
            View newDot = dotViews[newPosition];
            newDot.setBackgroundResource(R.drawable.bg_indicator_active);
            animateViewWidth(newDot, inactiveWidth, activeWidth);
        }
    }

    private void animateViewWidth(View view, int startWidth, int endWidth) {
        ValueAnimator anim = ValueAnimator.ofInt(startWidth, endWidth);
        anim.setDuration(250);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = (int) animation.getAnimatedValue();
            view.setLayoutParams(lp);
        });
        anim.start();
    }

    private void setupListeners() {
        binding.btnSkip.setOnClickListener(v -> finishOnboarding());
        binding.btnAction.setOnClickListener(v -> {
            int current = binding.viewPagerOnboarding.getCurrentItem();
            if (adapter != null && current < adapter.getItemCount() - 1) {
                binding.viewPagerOnboarding.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });
    }

    private void finishOnboarding() {
        sessionManager.setOnboardingDone(true);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatingAnimator != null) {
            floatingAnimator.cancel();
        }
    }
}
