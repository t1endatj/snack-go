package com.linh.snackgo.view.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.snackbar.Snackbar;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.CartController;
import com.linh.snackgo.controller.ProductController;
import com.linh.snackgo.databinding.ActivityProductDetailBinding;
import com.linh.snackgo.databinding.DialogFullScreenImageBinding;
import com.linh.snackgo.databinding.ItemProductSpecRowBinding;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.FavoriteManager;
import com.linh.snackgo.utils.PriceFormatter;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;
import com.linh.snackgo.view.adapter.ProductDetailGalleryAdapter;
import com.linh.snackgo.view.adapter.ProductDetailThumbnailAdapter;
import com.linh.snackgo.view.adapter.RelatedProductAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * màn hình chi tiết sản phẩm với nền hero theo danh mục, thư viện ảnh,
 * thông tin chi tiết, cam kết, sản phẩm liên quan và thanh nút cố định.
 */
public class ProductDetailActivity extends AppCompatActivity {
    private static final String KEY_SELECTED_PAGE = "key_selected_page";
    private static final String KEY_QUANTITY = "key_quantity";
    private static final String KEY_DESC_EXPANDED = "key_desc_expanded";

    private ActivityProductDetailBinding binding;
    private ProductController productController;
    private CartController cartController;
    private SessionManager sessionManager;
    private FavoriteManager favoriteManager;

    private Product product;
    private int quantity = 1;
    private int selectedPage = 0;
    private boolean isDescExpanded = false;
    private boolean isFavorite = false;
    private int heroBackgroundColor = Color.TRANSPARENT;

    private final List<String> displayImages = new ArrayList<>();
    private ProductDetailThumbnailAdapter thumbnailAdapter;
    private ObjectAnimator floatingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        productController = new ProductController(this);
        cartController = new CartController(this);
        sessionManager = new SessionManager(this);
        favoriteManager = new FavoriteManager(this);

        if (savedInstanceState != null) {
            quantity = savedInstanceState.getInt(KEY_QUANTITY, 1);
            selectedPage = savedInstanceState.getInt(KEY_SELECTED_PAGE, 0);
            isDescExpanded = savedInstanceState.getBoolean(KEY_DESC_EXPANDED, false);
        }

        long productId = getIntent().getLongExtra(IntentKeys.EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, R.string.error_product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        product = productController.getProductById(productId);
        if (product == null || !product.isActive()) {
            Toast.makeText(this, R.string.error_product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        playOpenAnimation();
        setupHeroCategoryGradient();
        setupGalleryImages();
        setupUI();
        setupFloatingAnimation();
        setupListeners();
        updateCartBadge();
    }

    private void playOpenAnimation() {
        binding.rootDetail.setAlpha(0f);
        binding.rootDetail.setScaleX(0.96f);
        binding.rootDetail.setScaleY(0.96f);
        binding.rootDetail.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void setupHeroCategoryGradient() {
        String category = product.getCategory() != null ? product.getCategory().toLowerCase(Locale.ROOT) : "";
        int gradientRes;
        if (AppConstants.CATEGORY_DRINK.equals(category)) {
            gradientRes = R.drawable.bg_hero_gradient_drink;
            heroBackgroundColor = getColor(R.color.hero_drink_background);
        } else if (AppConstants.CATEGORY_CANDY.equals(category)) {
            gradientRes = R.drawable.bg_hero_gradient_candy;
            heroBackgroundColor = getColor(R.color.hero_candy_background);
        } else {
            gradientRes = R.drawable.bg_hero_gradient_snack;
            heroBackgroundColor = getColor(R.color.hero_snack_background);
        }
        binding.layoutHero.setBackgroundResource(gradientRes);
    }

    private void setupFloatingAnimation() {
        float distance = -6f * getResources().getDisplayMetrics().density;
        floatingAnimator = ObjectAnimator.ofFloat(binding.vpProductImages, "translationY", 0f, distance);
        floatingAnimator.setDuration(2500);
        floatingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        floatingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatingAnimator.start();
    }

    private void setupGalleryImages() {
        displayImages.clear();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            displayImages.addAll(product.getImages());
        } else if (product.getImage() != null && !product.getImage().trim().isEmpty()) {
            displayImages.add(product.getImage().trim());
        } else {
            displayImages.add("ic_placeholder_product");
        }
    }

    private void setupUI() {
        long userId = sessionManager.getUserId();
        isFavorite = favoriteManager.isFavorite(userId, product.getId());
        binding.ivFavoriteIcon.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);

        binding.tvProductName.setText(product.getName());
        String categoryLabel;
        if (AppConstants.CATEGORY_DRINK.equalsIgnoreCase(product.getCategory())) {
            categoryLabel = getString(R.string.category_drink_label);
        } else if (AppConstants.CATEGORY_CANDY.equalsIgnoreCase(product.getCategory())) {
            categoryLabel = getString(R.string.category_candy_label);
        } else {
            categoryLabel = getString(R.string.category_snack_label);
        }
        binding.tvCategory.setText(categoryLabel);

        if (product.getTag() != null && !product.getTag().trim().isEmpty()) {
            binding.tvTagBadge.setText(product.getTag().trim());
            binding.tvTagBadge.setVisibility(View.VISIBLE);
        } else {
            binding.tvTagBadge.setVisibility(View.GONE);
        }

        // các chip thông tin
        if (product.getRating() > 0) {
            binding.chipRating.setVisibility(View.VISIBLE);
            binding.tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));
        } else {
            binding.chipRating.setVisibility(View.GONE);
        }

        if (product.getSoldCount() > 0) {
            binding.chipSold.setVisibility(View.VISIBLE);
            if (product.getSoldCount() >= 1000) {
                binding.tvSoldValue.setText(getString(R.string.label_sold_count_k_format, product.getSoldCount() / 1000.0f));
            } else {
                binding.tvSoldValue.setText(getString(R.string.label_sold_count_format, product.getSoldCount()));
            }
        } else {
            binding.chipSold.setVisibility(View.GONE);
        }

        binding.chipDelivery.setVisibility(View.VISIBLE);

        if (product.getSizeInfo() != null && !product.getSizeInfo().trim().isEmpty()) {
            binding.chipSize.setVisibility(View.VISIBLE);
            binding.tvSizeValue.setText(product.getSizeInfo().trim());
        } else {
            binding.chipSize.setVisibility(View.GONE);
        }

        // giá và giảm giá
        binding.tvProductPrice.setText(PriceFormatter.formatPrice(product.getPrice()));
        if (product.getOriginalPrice() > product.getPrice()) {
            binding.tvOriginalPrice.setText(PriceFormatter.formatPrice(product.getOriginalPrice()));
            binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvOriginalPrice.setVisibility(View.VISIBLE);

            int discount = (int) Math.round((product.getOriginalPrice() - product.getPrice()) * 100.0 / product.getOriginalPrice());
            if (discount > 0) {
                binding.tvDiscountBadge.setText(getString(R.string.label_discount_percent_format, discount));
                binding.tvDiscountBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvDiscountBadge.setVisibility(View.GONE);
            }
        } else {
            binding.tvOriginalPrice.setVisibility(View.GONE);
            binding.tvDiscountBadge.setVisibility(View.GONE);
        }

        // mô tả sản phẩm
        String desc = product.getDescription() != null ? product.getDescription().trim() : "";
        binding.tvDescription.setText(desc);
        if (desc.length() > 90) {
            binding.tvDescToggle.setVisibility(View.VISIBLE);
            updateDescriptionExpanded(isDescExpanded);
        } else {
            binding.tvDescToggle.setVisibility(View.GONE);
            binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
            binding.tvDescription.setEllipsize(null);
        }

        // thông tin chi tiết
        setupSpecificationsCard();

        // thiết lập thư viện ảnh bằng ViewPager2
        ProductDetailGalleryAdapter galleryAdapter = new ProductDetailGalleryAdapter(
                displayImages,
                this::openFullScreenImageDialog,
                400,
                heroBackgroundColor,
                true
        );
        binding.vpProductImages.setAdapter(galleryAdapter);

        if (displayImages.size() > 1) {
            binding.layoutDotsContainer.setVisibility(View.VISIBLE);
            binding.rvProductThumbnails.setVisibility(View.VISIBLE);
            setupDotsIndicator(displayImages.size());

            thumbnailAdapter = new ProductDetailThumbnailAdapter(displayImages, position -> {
                binding.vpProductImages.setCurrentItem(position, true);
            });
            binding.rvProductThumbnails.setAdapter(thumbnailAdapter);

            binding.vpProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    selectedPage = position;
                    updateDots(position);
                    if (thumbnailAdapter != null) {
                        thumbnailAdapter.setSelectedPosition(position);
                        binding.rvProductThumbnails.smoothScrollToPosition(position);
                    }
                }
            });

            if (selectedPage < displayImages.size()) {
                binding.vpProductImages.setCurrentItem(selectedPage, false);
            }
        } else {
            binding.layoutDotsContainer.setVisibility(View.GONE);
            binding.rvProductThumbnails.setVisibility(View.GONE);
        }

        // sản phẩm liên quan
        setupRelatedProducts();

        // số lượng và tổng tiền cập nhật theo thời gian thực
        updateQuantityUI();
    }

    private void updateDescriptionExpanded(boolean expanded) {
        if (expanded) {
            binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
            binding.tvDescription.setEllipsize(null);
            binding.tvDescToggle.setText(R.string.label_collapse);
        } else {
            binding.tvDescription.setMaxLines(3);
            binding.tvDescription.setEllipsize(TextUtils.TruncateAt.END);
            binding.tvDescToggle.setText(R.string.label_see_more);
        }
    }

    private void setupSpecificationsCard() {
        binding.layoutSpecsContainer.removeAllViews();
        List<SpecItem> specs = new ArrayList<>();

        addSpecIfNotEmpty(specs, getString(R.string.label_origin), product.getOrigin());
        addSpecIfNotEmpty(specs, getString(R.string.label_made_in), product.getMadeIn());
        addSpecIfNotEmpty(specs, getString(R.string.label_size_info), product.getSizeInfo());
        addSpecIfNotEmpty(specs, getString(R.string.label_ingredients), product.getIngredients());
        addSpecIfNotEmpty(specs, getString(R.string.label_features), product.getFeatures());
        addSpecIfNotEmpty(specs, getString(R.string.label_usage_guide), product.getUsageGuide());
        addSpecIfNotEmpty(specs, getString(R.string.label_storage_guide), product.getStorageGuide());
        addSpecIfNotEmpty(specs, getString(R.string.label_shelf_life), product.getShelfLife());

        if (specs.isEmpty()) {
            binding.cardProductSpecs.setVisibility(View.GONE);
            return;
        }

        binding.cardProductSpecs.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < specs.size(); i++) {
            SpecItem spec = specs.get(i);
            ItemProductSpecRowBinding rowBinding = ItemProductSpecRowBinding.inflate(inflater, binding.layoutSpecsContainer, false);
            rowBinding.tvSpecLabel.setText(spec.label);
            rowBinding.tvSpecValue.setText(spec.value);

            if (spec.value.length() > 80 || spec.value.contains("\n")) {
                rowBinding.tvSpecValue.setMaxLines(3);
                rowBinding.tvSpecValue.setEllipsize(TextUtils.TruncateAt.END);
                rowBinding.tvSpecToggle.setVisibility(View.VISIBLE);
                final boolean[] rowExpanded = {false};
                rowBinding.tvSpecToggle.setOnClickListener(v -> {
                    rowExpanded[0] = !rowExpanded[0];
                    if (rowExpanded[0]) {
                        rowBinding.tvSpecValue.setMaxLines(Integer.MAX_VALUE);
                        rowBinding.tvSpecValue.setEllipsize(null);
                        rowBinding.tvSpecToggle.setText(R.string.label_collapse);
                    } else {
                        rowBinding.tvSpecValue.setMaxLines(3);
                        rowBinding.tvSpecValue.setEllipsize(TextUtils.TruncateAt.END);
                        rowBinding.tvSpecToggle.setText(R.string.label_see_more);
                    }
                });
            } else {
                rowBinding.tvSpecToggle.setVisibility(View.GONE);
            }

            if (i == specs.size() - 1) {
                rowBinding.specDivider.setVisibility(View.GONE);
            }

            binding.layoutSpecsContainer.addView(rowBinding.getRoot());
        }
    }

    private void addSpecIfNotEmpty(List<SpecItem> list, String label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            list.add(new SpecItem(label, value.trim()));
        }
    }

    private static class SpecItem {
        final String label;
        final String value;
        SpecItem(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    private void setupRelatedProducts() {
        List<Product> related = productController.getRelatedProducts(product.getId(), product.getCategory(), AppConstants.MAX_RELATED_PRODUCTS);
        if (related == null || related.isEmpty()) {
            binding.layoutRelatedProducts.setVisibility(View.GONE);
            return;
        }

        binding.layoutRelatedProducts.setVisibility(View.VISIBLE);
        RelatedProductAdapter adapter = new RelatedProductAdapter(related, new RelatedProductAdapter.OnRelatedProductClickListener() {
            @Override
            public void onProductClick(Product p) {
                Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra(IntentKeys.EXTRA_PRODUCT_ID, p.getId());
                startActivity(intent);
                finish();
            }

            @Override
            public void onAddToCartClick(Product p) {
                long userId = sessionManager.getUserId();
                boolean success = cartController.addToCart(userId, p.getId(), 1);
                if (success) {
                    updateCartBadge();
                    bounceCartBadge();
                    Snackbar.make(binding.getRoot(), R.string.label_added_to_cart, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.label_view_cart_action, v -> openCartTab())
                            .show();
                }
            }
        });
        binding.rvRelatedProducts.setAdapter(adapter);
    }

    private void setupDotsIndicator(int count) {
        binding.layoutDotsContainer.removeAllViews();
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (8 * density), (int) (8 * density)
            );
            params.setMargins((int) (4 * density), 0, (int) (4 * density), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_indicator_inactive);
            binding.layoutDotsContainer.addView(dot);
        }
        updateDots(selectedPage);
    }

    private void updateDots(int activeIndex) {
        int count = binding.layoutDotsContainer.getChildCount();
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < count; i++) {
            View dot = binding.layoutDotsContainer.getChildAt(i);
            if (dot != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                if (i == activeIndex) {
                    params.width = (int) (18 * density);
                    dot.setBackgroundResource(R.drawable.bg_indicator_active);
                } else {
                    params.width = (int) (8 * density);
                    dot.setBackgroundResource(R.drawable.bg_indicator_inactive);
                }
                dot.setLayoutParams(params);
            }
        }
    }

    private void openFullScreenImageDialog(int startPosition) {
        DialogFullScreenImageBinding dialogBinding = DialogFullScreenImageBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setView(dialogBinding.getRoot())
                .create();

        ProductDetailGalleryAdapter fullAdapter = new ProductDetailGalleryAdapter(
                displayImages,
                null,
                1000,
                Color.WHITE,
                false
        );
        dialogBinding.vpFullScreenImages.setAdapter(fullAdapter);
        if (startPosition < displayImages.size()) {
            dialogBinding.vpFullScreenImages.setCurrentItem(startPosition, false);
        }

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateQuantityUI() {
        binding.tvQuantity.setText(String.valueOf(quantity));
        long total = product.getPrice() * quantity;
        binding.btnAddToCart.setText(getString(R.string.label_add_with_price, PriceFormatter.formatPrice(total)));
    }

    private void updateCartBadge() {
        long userId = sessionManager.getUserId();
        int cartCount = cartController.getCartCount(userId);
        if (cartCount > 0) {
            binding.tvCartBadge.setText(String.valueOf(cartCount));
            binding.tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            binding.tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void bounceCartBadge() {
        binding.tvCartBadge.setScaleX(0.5f);
        binding.tvCartBadge.setScaleY(0.5f);
        binding.tvCartBadge.animate()
                .scaleX(1.25f).scaleY(1.25f)
                .setDuration(150)
                .withEndAction(() -> binding.tvCartBadge.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCart.setOnClickListener(v -> openCartTab());

        binding.btnFavorite.setOnClickListener(v -> {
            long userId = sessionManager.getUserId();
            isFavorite = favoriteManager.toggleFavorite(userId, product.getId());
            binding.ivFavoriteIcon.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        });

        binding.tvDescToggle.setOnClickListener(v -> {
            isDescExpanded = !isDescExpanded;
            updateDescriptionExpanded(isDescExpanded);
        });

        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityUI();
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            if (quantity < AppConstants.MAX_QUANTITY_PER_ITEM) {
                quantity++;
                updateQuantityUI();
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> performAddToCart());

        binding.btnBuyNow.setOnClickListener(v -> performBuyNow());
    }

    private void performAddToCart() {
        long userId = sessionManager.getUserId();
        boolean success = cartController.addToCart(userId, product.getId(), quantity);
        if (success) {
            updateCartBadge();
            bounceCartBadge();
            Snackbar.make(binding.getRoot(), R.string.label_added_to_cart, Snackbar.LENGTH_LONG)
                    .setAction(R.string.label_view_cart_action, v -> openCartTab())
                    .show();
        }
    }

    private void performBuyNow() {
        long userId = sessionManager.getUserId();
        boolean success = cartController.addToCart(userId, product.getId(), quantity);
        if (success) {
            updateCartBadge();
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        }
    }

    private void openCartTab() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(IntentKeys.EXTRA_OPEN_TAB, R.id.nav_cart);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_QUANTITY, quantity);
        outState.putInt(KEY_SELECTED_PAGE, selectedPage);
        outState.putBoolean(KEY_DESC_EXPANDED, isDescExpanded);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatingAnimator != null) {
            floatingAnimator.cancel();
        }
    }
}
