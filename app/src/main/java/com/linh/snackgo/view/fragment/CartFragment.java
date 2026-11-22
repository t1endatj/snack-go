package com.linh.snackgo.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.CartController;
import com.linh.snackgo.databinding.FragmentCartBinding;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.CartSummary;
import com.linh.snackgo.utils.PriceFormatter;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;
import com.linh.snackgo.view.activity.CheckoutActivity;
import com.linh.snackgo.view.activity.MainActivity;
import com.linh.snackgo.view.adapter.CartAdapter;

import java.util.List;

/**
 * Cart tab fragment displaying user's cart items, promo code application, price summary, and checkout affordance.
 */
public class CartFragment extends Fragment implements CartAdapter.OnCartItemChangeListener {

    private static final String KEY_PROMO_APPLIED = "key_promo_applied";

    private FragmentCartBinding binding;
    private CartController cartController;
    private SessionManager sessionManager;
    private CartAdapter adapter;
    private boolean isPromoApplied = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cartController = new CartController(requireContext());
        sessionManager = new SessionManager(requireContext());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), getActivity());

        if (savedInstanceState != null) {
            isPromoApplied = savedInstanceState.getBoolean(KEY_PROMO_APPLIED, false);
        }

        setupRecyclerView();
        setupListeners();
        updateCartData();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCartItems.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnExplore.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_home);
            }
        });

        binding.etPromoCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPromoCode.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnApplyPromo.setOnClickListener(v -> {
            String code = binding.etPromoCode.getText() != null ? binding.etPromoCode.getText().toString() : "";
            if (cartController.validatePromo(code)) {
                isPromoApplied = true;
                binding.etPromoCode.setText("");
                binding.tilPromoCode.setError(null);
                UiUtils.hideKeyboard(getActivity());
                updateCartData();
            } else {
                binding.tilPromoCode.setError(getString(R.string.label_promo_invalid));
            }
        });

        binding.btnRemovePromo.setOnClickListener(v -> {
            isPromoApplied = false;
            updateCartData();
        });

        binding.btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CheckoutActivity.class);
            intent.putExtra(IntentKeys.EXTRA_PROMO_APPLIED, isPromoApplied);
            startActivity(intent);
        });
    }

    private void updateCartData() {
        long userId = sessionManager.getUserId();
        List<CartItem> items = cartController.getCartItems(userId);
        int totalCount = cartController.getCartCount(userId);

        binding.tvCartCountSubtitle.setText(getString(R.string.label_cart_items_count_format, totalCount));

        if (items.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutCartContent.setVisibility(View.GONE);
            binding.rvCartItems.setVisibility(View.GONE);
            binding.btnCheckout.setEnabled(false);
            isPromoApplied = false;
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.layoutCartContent.setVisibility(View.VISIBLE);
            binding.rvCartItems.setVisibility(View.VISIBLE);
            binding.btnCheckout.setEnabled(true);
        }

        adapter.submitList(items);

        // Update Promo UI
        if (isPromoApplied && !items.isEmpty()) {
            binding.layoutPromoApplied.setVisibility(View.VISIBLE);
            binding.layoutDiscountRow.setVisibility(View.VISIBLE);
        } else {
            binding.layoutPromoApplied.setVisibility(View.GONE);
            binding.layoutDiscountRow.setVisibility(View.GONE);
        }

        // Summary Calculations
        CartSummary summary = cartController.computeSummary(items, isPromoApplied);
        binding.tvSubtotal.setText(PriceFormatter.formatPrice(summary.getSubtotal()));

        if (summary.isFreeDelivery()) {
            binding.tvDeliveryFee.setText(R.string.label_free);
            binding.tvDeliveryFee.setTextColor(requireContext().getColor(R.color.accent_orange));
        } else {
            binding.tvDeliveryFee.setText(PriceFormatter.formatPrice(summary.getDeliveryFee()));
            binding.tvDeliveryFee.setTextColor(requireContext().getColor(R.color.primary_ink));
        }

        if (isPromoApplied) {
            binding.tvDiscount.setText("-" + PriceFormatter.formatPrice(summary.getDiscount()));
        }

        binding.tvTotal.setText(PriceFormatter.formatPrice(summary.getTotal()));

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateCartBadge();
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        long userId = sessionManager.getUserId();
        cartController.updateQuantity(userId, item.getProductId(), newQuantity);
        updateCartData();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        long userId = sessionManager.getUserId();
        cartController.removeItem(userId, item.getProductId());
        updateCartData();

        Snackbar.make(binding.getRoot(), R.string.label_item_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.label_undo, v -> {
                    cartController.restoreItem(userId, item.getProductId(), item.getQuantity());
                    updateCartData();
                })
                .show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PROMO_APPLIED, isPromoApplied);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
