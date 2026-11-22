package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.databinding.ActivityOrderSuccessBinding;
import com.linh.snackgo.utils.PriceFormatter;

/**
 * Order Success Activity displaying order confirmation, code, delivery estimation, and navigation actions.
 */
public class OrderSuccessActivity extends AppCompatActivity {
    private ActivityOrderSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String orderCode = getIntent().getStringExtra(IntentKeys.EXTRA_ORDER_CODE);
        long orderTotal = getIntent().getLongExtra(IntentKeys.EXTRA_ORDER_TOTAL, 0L);
        String paymentMethod = getIntent().getStringExtra(IntentKeys.EXTRA_PAYMENT_METHOD);

        setupUI(orderCode, orderTotal, paymentMethod);
        animateSuccessIcon();
        setupListeners();
    }

    private void setupUI(String code, long total, String method) {
        if (code != null) {
            binding.tvOrderCode.setText(getString(R.string.label_order_code_format, code));
        }

        String methodLabel = getString(R.string.payment_cash_title);
        if (AppConstants.PAYMENT_BANK.equalsIgnoreCase(method)) {
            methodLabel = getString(R.string.payment_bank_title);
        } else if (AppConstants.PAYMENT_EWALLET.equalsIgnoreCase(method)) {
            methodLabel = getString(R.string.payment_wallet_title);
        }

        binding.tvOrderDetails.setText(getString(R.string.label_order_details_format, PriceFormatter.formatPrice(total), methodLabel));
    }

    private void animateSuccessIcon() {
        binding.ivSuccessIcon.setScaleX(0.7f);
        binding.ivSuccessIcon.setScaleY(0.7f);
        binding.ivSuccessIcon.setAlpha(0f);
        binding.ivSuccessIcon.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(400)
                .start();
    }

    private void setupListeners() {
        binding.btnViewOrders.setOnClickListener(v -> navigateToMainTab(R.id.nav_orders));
        binding.btnGoHome.setOnClickListener(v -> navigateToMainTab(R.id.nav_home));
    }

    private void navigateToMainTab(int tabId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(IntentKeys.EXTRA_OPEN_TAB, tabId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
