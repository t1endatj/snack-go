package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.CartController;
import com.linh.snackgo.controller.OrderController;
import com.linh.snackgo.databinding.ActivityCheckoutBinding;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.CartSummary;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.PriceFormatter;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checkout Activity for order validation, payment method selection, summary display, and placing orders.
 */
public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;
    private OrderController orderController;
    private CartController cartController;
    private SessionManager sessionManager;

    private String selectedPaymentMethod = AppConstants.PAYMENT_CASH;
    private boolean isPromoApplied = false;
    private Set<Long> selectedProductIds = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        orderController = new OrderController(this);
        cartController = new CartController(this);
        sessionManager = new SessionManager(this);

        isPromoApplied = getIntent().getBooleanExtra(IntentKeys.EXTRA_PROMO_APPLIED, false);
        long[] selectedIdsArray = getIntent().getLongArrayExtra(IntentKeys.EXTRA_SELECTED_PRODUCT_IDS);
        if (selectedIdsArray != null) {
            selectedProductIds = new HashSet<>();
            for (long id : selectedIdsArray) {
                selectedProductIds.add(id);
            }
        }

        long userId = sessionManager.getUserId();
        int cartCount = cartController.getCartCount(userId);
        if (cartCount == 0) {
            Toast.makeText(this, R.string.label_empty_cart_title, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        // Prefill delivery info
        binding.etReceiverName.setText(sessionManager.getUserName());
        binding.etPhone.setText(sessionManager.getUserPhone());
        binding.etAddress.setText(sessionManager.getUserAddress());

        // Default Payment Method = CASH
        selectPaymentMethod(AppConstants.PAYMENT_CASH);

        // Load Order Items & Summary
        loadOrderSummary();
    }

    private void loadOrderSummary() {
        long userId = sessionManager.getUserId();
        List<CartItem> allCartItems = cartController.getCartItems(userId);
        List<CartItem> cartItems = new ArrayList<>();
        if (allCartItems != null) {
            for (CartItem item : allCartItems) {
                if (selectedProductIds == null || selectedProductIds.contains(item.getProductId())) {
                    cartItems.add(item);
                }
            }
        }
        CartSummary summary = cartController.computeSummary(cartItems, isPromoApplied);

        // Populate compact item list with BlendImageView thumbnails
        binding.layoutOrderItemsList.removeAllViews();
        if (cartItems != null) {
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
            for (CartItem item : cartItems) {
                if (item.getProduct() != null) {
                    com.linh.snackgo.databinding.ItemCheckoutSummaryRowBinding rowBinding =
                            com.linh.snackgo.databinding.ItemCheckoutSummaryRowBinding.inflate(inflater, binding.layoutOrderItemsList, false);
                    rowBinding.tvCheckoutItemName.setText(item.getProduct().getName());
                    rowBinding.tvCheckoutItemQty.setText("x" + item.getQuantity());
                    rowBinding.tvCheckoutItemTotal.setText(PriceFormatter.formatPrice(item.getItemTotalPrice()));
                    ImageUtils.loadBlendImage(this, item.getProduct().getCoverImage(), rowBinding.ivCheckoutThumb, 80);
                    binding.layoutOrderItemsList.addView(rowBinding.getRoot());
                }
            }
        }

        binding.tvSubtotal.setText(PriceFormatter.formatPrice(summary.getSubtotal()));

        if (summary.isFreeDelivery()) {
            binding.tvDeliveryFee.setText(R.string.label_free);
            binding.tvDeliveryFee.setTextColor(getColor(R.color.accent_orange));
        } else {
            binding.tvDeliveryFee.setText(PriceFormatter.formatPrice(summary.getDeliveryFee()));
            binding.tvDeliveryFee.setTextColor(getColor(R.color.primary_ink));
        }

        if (isPromoApplied) {
            binding.tvDiscount.setText("-" + PriceFormatter.formatPrice(summary.getDiscount()));
            binding.layoutDiscountRow.setVisibility(View.VISIBLE);
        } else {
            binding.layoutDiscountRow.setVisibility(View.GONE);
        }

        String formattedTotal = PriceFormatter.formatPrice(summary.getTotal());
        binding.tvTotal.setText(formattedTotal);
        binding.tvBottomTotal.setText(formattedTotal);
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;

        binding.cardPaymentCash.setBackgroundResource(AppConstants.PAYMENT_CASH.equals(method) ? R.drawable.bg_payment_card_selected : R.drawable.bg_payment_card_unselected);
        binding.ivCheckCash.setVisibility(AppConstants.PAYMENT_CASH.equals(method) ? View.VISIBLE : View.GONE);

        binding.cardPaymentBank.setBackgroundResource(AppConstants.PAYMENT_BANK.equals(method) ? R.drawable.bg_payment_card_selected : R.drawable.bg_payment_card_unselected);
        binding.ivCheckBank.setVisibility(AppConstants.PAYMENT_BANK.equals(method) ? View.VISIBLE : View.GONE);

        binding.cardPaymentEWallet.setBackgroundResource(AppConstants.PAYMENT_EWALLET.equals(method) ? R.drawable.bg_payment_card_selected : R.drawable.bg_payment_card_unselected);
        binding.ivCheckWallet.setVisibility(AppConstants.PAYMENT_EWALLET.equals(method) ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.cardPaymentCash.setOnClickListener(v -> selectPaymentMethod(AppConstants.PAYMENT_CASH));
        binding.cardPaymentBank.setOnClickListener(v -> selectPaymentMethod(AppConstants.PAYMENT_BANK));
        binding.cardPaymentEWallet.setOnClickListener(v -> selectPaymentMethod(AppConstants.PAYMENT_EWALLET));

        binding.etReceiverName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilReceiverName.setError(null);
            }
        });

        binding.etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
            }
        });

        binding.etAddress.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilAddress.setError(null);
            }
        });

        binding.btnConfirmOrder.setOnClickListener(v -> performConfirmOrder());
    }

    private void performConfirmOrder() {
        String receiverName = binding.etReceiverName.getText() != null ? binding.etReceiverName.getText().toString() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString() : "";

        binding.btnConfirmOrder.setEnabled(false);

        long userId = sessionManager.getUserId();
        List<Long> selectedIdsList = selectedProductIds != null ? new ArrayList<>(selectedProductIds) : null;
        OrderController.OrderResult result = orderController.placeOrder(userId, receiverName, phone, address, selectedPaymentMethod, isPromoApplied, selectedIdsList);

        if (result.isSuccess()) {
            Intent intent = new Intent(this, OrderSuccessActivity.class);
            intent.putExtra(IntentKeys.EXTRA_ORDER_CODE, result.getOrderCode());
            intent.putExtra(IntentKeys.EXTRA_ORDER_TOTAL, result.getTotalAmount());
            intent.putExtra(IntentKeys.EXTRA_PAYMENT_METHOD, result.getPaymentMethod());
            startActivity(intent);
            finish();
        } else {
            binding.btnConfirmOrder.setEnabled(true);
            if (result.getErrorFieldId() == R.id.etReceiverName) {
                binding.tilReceiverName.setError(result.getErrorMessage());
                binding.etReceiverName.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etPhone) {
                binding.tilPhone.setError(result.getErrorMessage());
                binding.etPhone.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etAddress) {
                binding.tilAddress.setError(result.getErrorMessage());
                binding.etAddress.requestFocus();
            } else {
                Snackbar.make(binding.getRoot(), result.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
