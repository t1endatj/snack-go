package com.linh.snackgo.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.CartController;
import com.linh.snackgo.controller.ProductController;
import com.linh.snackgo.databinding.DialogSortFilterBinding;
import com.linh.snackgo.databinding.FragmentHomeBinding;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.FavoriteManager;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.NameUtils;
import com.linh.snackgo.utils.PriceFormatter;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;
import com.linh.snackgo.view.activity.MainActivity;
import com.linh.snackgo.view.activity.ProductDetailActivity;
import com.linh.snackgo.view.adapter.ProductAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * fragment trang chủ với tìm kiếm, chip danh mục, lưới sản phẩm và giỏ hàng nổi.
 */
public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentHomeBinding binding;
    private ProductController productController;
    private CartController cartController;
    private SessionManager sessionManager;
    private ProductAdapter adapter;
    private ExecutorService productExecutor;

    private String selectedCategory = AppConstants.CATEGORY_ALL;
    private String currentKeyword = "";
    private int sortOption = 0; // 0: mặc định, 1: tăng dần, 2: giảm dần

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ContextInitialization();
        setupStickyHomeHeader();
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), getActivity());

        setupHeader();
        setupRecyclerView();
        setupSearchAndFilter();
        setupCategoryChips();
        setupMiniCart();

        loadProducts();
    }

    private void setupStickyHomeHeader() {
        NestedScrollView scrollView = binding.scrollViewHome;
        if (scrollView.getChildCount() == 0 || !(scrollView.getChildAt(0) instanceof LinearLayout)) {
            return;
        }

        LinearLayout content = (LinearLayout) scrollView.getChildAt(0);
        if (content.getChildCount() < 2 || !(content.getChildAt(0) instanceof LinearLayout)) {
            return;
        }

        LinearLayout header = (LinearLayout) content.getChildAt(0);
        View categoryBar = content.getChildAt(1);
        if (header.getChildCount() < 2) {
            return;
        }

        View searchRow = header.getChildAt(1);
        header.removeView(searchRow);
        content.removeView(categoryBar);
        content.removeView(header);

        AppBarLayout appBar = new AppBarLayout(requireContext());
        appBar.setId(View.generateViewId());
        int sharedBackground = requireContext().getColor(R.color.background);
        appBar.setBackgroundColor(sharedBackground);
        appBar.setLiftOnScroll(true);
        appBar.setElevation(dp(2));

        CoordinatorLayout.LayoutParams appBarParams = new CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        AppBarLayout.LayoutParams headerParams = new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.setScrollFlags(
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        );
        header.setLayoutParams(headerParams);
        header.setBackgroundColor(sharedBackground);
        appBar.addView(header);

        LinearLayout stickySearch = new LinearLayout(requireContext());
        stickySearch.setOrientation(LinearLayout.VERTICAL);
        stickySearch.setBackgroundColor(sharedBackground);
        int horizontalPadding = dp(20);
        stickySearch.setPadding(horizontalPadding, 0, horizontalPadding, dp(8));
        stickySearch.addView(searchRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        appBar.addView(stickySearch, new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        categoryBar.setBackgroundColor(sharedBackground);
        appBar.addView(categoryBar, new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        appBar.addOnOffsetChangedListener((layout, verticalOffset) -> {
            boolean isSticky = verticalOffset != 0;
            layout.setElevation(isSticky ? dp(6) : 0f);
            stickySearch.setElevation(isSticky ? dp(2) : 0f);
        });

        CoordinatorLayout root = binding.getRoot();
        root.addView(appBar, 0, appBarParams);

        CoordinatorLayout.LayoutParams scrollParams =
                (CoordinatorLayout.LayoutParams) scrollView.getLayoutParams();
        scrollParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        scrollView.setLayoutParams(scrollParams);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void ContextInitialization() {
        productController = new ProductController(requireContext());
        cartController = new CartController(requireContext());
        sessionManager = new SessionManager(requireContext());
        productExecutor = Executors.newSingleThreadExecutor();
    }

    private void setupHeader() {
        String fullName = sessionManager.getUserName();
        String firstName = NameUtils.getFirstName(fullName);
        binding.tvGreeting.setText(getString(R.string.label_greeting_format, firstName));

        binding.btnBell.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.label_coming_soon, Toast.LENGTH_SHORT).show()
        );

        binding.cardAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_profile);
            }
        });

        binding.tvSeeAll.setOnClickListener(v -> {
            binding.etSearch.setText("");
            binding.chipAll.setChecked(true);
            selectedCategory = AppConstants.CATEGORY_ALL;
            currentKeyword = "";
            sortOption = 0;
            loadProducts();
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this);
        binding.rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
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

        binding.btnFilter.setOnClickListener(v -> showSortBottomSheetDialog());

        binding.btnClearFilter.setOnClickListener(v -> {
            binding.etSearch.setText("");
            binding.chipAll.setChecked(true);
            selectedCategory = AppConstants.CATEGORY_ALL;
            currentKeyword = "";
            sortOption = 0;
            loadProducts();
        });
    }

    private void showSortBottomSheetDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogSortFilterBinding dialogBinding = DialogSortFilterBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (sortOption == 1) {
            dialogBinding.rbSortLowHigh.setChecked(true);
        } else if (sortOption == 2) {
            dialogBinding.rbSortHighLow.setChecked(true);
        } else {
            dialogBinding.rbSortDefault.setChecked(true);
        }

        dialogBinding.rgSortOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSortLowHigh) {
                sortOption = 1;
            } else if (checkedId == R.id.rbSortHighLow) {
                sortOption = 2;
            } else {
                sortOption = 0;
            }
            dialog.dismiss();
            loadProducts();
        });

        dialog.show();
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipSnack)) {
                selectedCategory = AppConstants.CATEGORY_SNACK;
            } else if (checkedIds.contains(R.id.chipDrink)) {
                selectedCategory = AppConstants.CATEGORY_DRINK;
            } else if (checkedIds.contains(R.id.chipCandy)) {
                selectedCategory = AppConstants.CATEGORY_CANDY;
            } else if (checkedIds.contains(R.id.chipFavorite)) {
                selectedCategory = AppConstants.CATEGORY_FAVORITE;
            } else {
                selectedCategory = AppConstants.CATEGORY_ALL;
            }

            updateChipStyles(checkedIds);
            loadProducts();
        });
    }

    private void updateChipStyles(List<Integer> checkedIds) {
        int activeText = requireContext().getColor(R.color.surface);
        int inactiveText = requireContext().getColor(R.color.primary_ink);

        binding.chipAll.setChipBackgroundColorResource(checkedIds.contains(R.id.chipAll) ? R.color.primary_ink : R.color.surface);
        binding.chipAll.setTextColor(checkedIds.contains(R.id.chipAll) ? activeText : inactiveText);

        binding.chipSnack.setChipBackgroundColorResource(checkedIds.contains(R.id.chipSnack) ? R.color.primary_ink : R.color.surface);
        binding.chipSnack.setTextColor(checkedIds.contains(R.id.chipSnack) ? activeText : inactiveText);

        binding.chipDrink.setChipBackgroundColorResource(checkedIds.contains(R.id.chipDrink) ? R.color.primary_ink : R.color.surface);
        binding.chipDrink.setTextColor(checkedIds.contains(R.id.chipDrink) ? activeText : inactiveText);

        binding.chipCandy.setChipBackgroundColorResource(checkedIds.contains(R.id.chipCandy) ? R.color.primary_ink : R.color.surface);
        binding.chipCandy.setTextColor(checkedIds.contains(R.id.chipCandy) ? activeText : inactiveText);

        binding.chipFavorite.setChipBackgroundColorResource(checkedIds.contains(R.id.chipFavorite) ? R.color.primary_ink : R.color.surface);
        binding.chipFavorite.setTextColor(checkedIds.contains(R.id.chipFavorite) ? activeText : inactiveText);
    }

    private void loadProducts() {
        if (binding == null || productExecutor == null) {
            return;
        }

        binding.progressHome.setVisibility(View.VISIBLE);
        productExecutor.execute(() -> {
            try {
                boolean isFavoriteFilter = AppConstants.CATEGORY_FAVORITE.equals(selectedCategory);
                String queryCategory = isFavoriteFilter ? AppConstants.CATEGORY_ALL : selectedCategory;
                List<Product> products = productController.getActiveProducts(
                        queryCategory,
                        currentKeyword,
                        sortOption
                );
                if (isFavoriteFilter && isAdded()) {
                    FavoriteManager favManager = new FavoriteManager(requireContext());
                    long uid = sessionManager.getUserId();
                    List<Product> favOnly = new ArrayList<>();
                    for (Product p : products) {
                        if (favManager.isFavorite(uid, p.getId())) {
                            favOnly.add(p);
                        }
                    }
                    products = favOnly;
                }
                final List<Product> finalProducts = products;
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) {
                        return;
                    }
                    binding.progressHome.setVisibility(View.GONE);
                    binding.tvEmptyStateMessage.setText(isFavoriteFilter ? R.string.msg_empty_favorite : R.string.msg_empty_search);
                    binding.btnClearFilter.setText(R.string.label_clear_filter);
                    adapter.submitList(new ArrayList<>(finalProducts));
                    if (finalProducts.isEmpty()) {
                        binding.layoutEmptyState.setVisibility(View.VISIBLE);
                        binding.rvProducts.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyState.setVisibility(View.GONE);
                        binding.rvProducts.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception ignored) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(this::showProductLoadError);
            }
        });
    }

    private void showProductLoadError() {
        if (binding == null) {
            return;
        }
        binding.progressHome.setVisibility(View.GONE);
        binding.rvProducts.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.tvEmptyStateMessage.setText(R.string.error_products_load_failed);
        binding.btnClearFilter.setText(R.string.label_try_again);
    }

    private void setupMiniCart() {
        binding.btnViewCart.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_cart);
            }
        });
    }

    private void updateMiniCartUI() {
        long userId = sessionManager.getUserId();
        int totalCount = cartController.getCartCount(userId);
        long totalAmount = cartController.getCartTotal(userId);

        if (totalCount > 0) {
            binding.tvMiniCartCount.setText(getString(R.string.label_mini_cart_format, totalCount));
            binding.tvMiniCartTotal.setText(PriceFormatter.formatPrice(totalAmount));

            CartItem lastItem = cartController.getLastAddedItem(userId);
            if (lastItem != null && lastItem.getProduct() != null) {
                ImageUtils.loadBlendImage(requireContext(), lastItem.getProduct().getCoverImage(), binding.ivMiniCartThumb, 120);
            }

            if (binding.cardMiniCart.getVisibility() != View.VISIBLE) {
                binding.cardMiniCart.setAlpha(0f);
                binding.cardMiniCart.setVisibility(View.VISIBLE);
                binding.cardMiniCart.animate().alpha(1f).setDuration(200).start();
            }
        } else {
            if (binding.cardMiniCart.getVisibility() == View.VISIBLE) {
                binding.cardMiniCart.animate().alpha(0f).setDuration(200).withEndAction(() ->
                        binding.cardMiniCart.setVisibility(View.GONE)
                ).start();
            }
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateCartBadge();
        }
    }

    @Override
    public void onProductClick(Product product) {
        if (product != null) {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(IntentKeys.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onAddToCartClick(Product product, View addBtn) {
        long userId = sessionManager.getUserId();
        boolean success = cartController.addToCart(userId, product.getId());
        if (success) {
            Toast.makeText(requireContext(), R.string.label_added_to_cart, Toast.LENGTH_SHORT).show();
            updateMiniCartUI();
        }
    }

    @Override
    public void onFavoriteToggled(Product product, boolean isFavorite) {
        if (AppConstants.CATEGORY_FAVORITE.equals(selectedCategory) && !isFavorite) {
            loadProducts();
        }
    }

    @Override
    public void onDestroyView() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (productExecutor != null) {
            productExecutor.shutdownNow();
            productExecutor = null;
        }
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
        updateMiniCartUI();
    }
}
