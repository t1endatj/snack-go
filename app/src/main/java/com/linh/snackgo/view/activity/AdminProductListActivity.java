package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.AdminProductController;
import com.linh.snackgo.databinding.ActivityAdminProductListBinding;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;
import com.linh.snackgo.view.adapter.AdminProductAdapter;

import java.util.List;

/**
 * Admin Product Management List Activity featuring search, category filters, product CRUD actions, and undo delete.
 */
public class AdminProductListActivity extends AppCompatActivity implements AdminProductAdapter.OnAdminProductClickListener {

    private ActivityAdminProductListBinding binding;
    private AdminProductController controller;
    private SessionManager sessionManager;
    private AdminProductAdapter adapter;

    private String selectedCategory = AppConstants.CATEGORY_ALL;
    private String currentKeyword = "";

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, R.string.error_admin_access_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding = ActivityAdminProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        controller = new AdminProductController(this);

        setupRecyclerView();
        setupSearchAndFilter();
        setupListeners();
        loadProducts();
    }

    private void setupRecyclerView() {
        adapter = new AdminProductAdapter(this);
        binding.rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminProducts.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.etAdminSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentKeyword = s != null ? s.toString().trim() : "";
                    loadProducts();
                };
                searchHandler.postDelayed(searchRunnable, 250);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupAdminCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipSnack)) {
                selectedCategory = AppConstants.CATEGORY_SNACK;
            } else if (checkedIds.contains(R.id.chipDrink)) {
                selectedCategory = AppConstants.CATEGORY_DRINK;
            } else if (checkedIds.contains(R.id.chipCandy)) {
                selectedCategory = AppConstants.CATEGORY_CANDY;
            } else {
                selectedCategory = AppConstants.CATEGORY_ALL;
            }
            updateChipStyles(checkedIds);
            loadProducts();
        });
    }

    private void updateChipStyles(List<Integer> checkedIds) {
        int activeText = getColor(R.color.surface);
        int inactiveText = getColor(R.color.primary_ink);

        binding.chipAll.setChipBackgroundColorResource(checkedIds.contains(R.id.chipAll) ? R.color.primary_ink : R.color.surface);
        binding.chipAll.setTextColor(checkedIds.contains(R.id.chipAll) ? activeText : inactiveText);

        binding.chipSnack.setChipBackgroundColorResource(checkedIds.contains(R.id.chipSnack) ? R.color.primary_ink : R.color.surface);
        binding.chipSnack.setTextColor(checkedIds.contains(R.id.chipSnack) ? activeText : inactiveText);

        binding.chipDrink.setChipBackgroundColorResource(checkedIds.contains(R.id.chipDrink) ? R.color.primary_ink : R.color.surface);
        binding.chipDrink.setTextColor(checkedIds.contains(R.id.chipDrink) ? activeText : inactiveText);

        binding.chipCandy.setChipBackgroundColorResource(checkedIds.contains(R.id.chipCandy) ? R.color.primary_ink : R.color.surface);
        binding.chipCandy.setTextColor(checkedIds.contains(R.id.chipCandy) ? activeText : inactiveText);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        View.OnClickListener openAddListener = v -> {
            Intent intent = new Intent(this, AdminProductFormActivity.class);
            startActivity(intent);
        };

        binding.fabAddProduct.setOnClickListener(openAddListener);
        binding.btnAddProductEmpty.setOnClickListener(openAddListener);
    }

    private void loadProducts() {
        List<Product> products = controller.getActiveProducts(selectedCategory, currentKeyword);
        adapter.submitList(products);

        if (products.isEmpty()) {
            binding.layoutEmptyAdminProducts.setVisibility(View.VISIBLE);
            binding.rvAdminProducts.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyAdminProducts.setVisibility(View.GONE);
            binding.rvAdminProducts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditProduct(Product product) {
        if (product != null) {
            Intent intent = new Intent(this, AdminProductFormActivity.class);
            intent.putExtra(IntentKeys.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDeleteProduct(Product product) {
        if (product == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_confirm_delete_product)
                .setMessage(R.string.msg_confirm_delete_product)
                .setPositiveButton(R.string.label_confirm, (dialog, which) -> performDeleteProduct(product))
                .setNegativeButton(R.string.label_cancel, null)
                .show();
    }

    private void performDeleteProduct(Product product) {
        boolean success = controller.deleteProduct(product.getId());
        if (success) {
            loadProducts();
            Snackbar.make(binding.getRoot(), R.string.msg_product_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.label_undo, v -> {
                        controller.restoreProduct(product.getId());
                        loadProducts();
                    })
                    .show();
        } else {
            Toast.makeText(this, R.string.error_order_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}
