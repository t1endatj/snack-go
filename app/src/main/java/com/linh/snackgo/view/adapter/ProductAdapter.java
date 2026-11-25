package com.linh.snackgo.view.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.databinding.ItemProductBinding;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.FavoriteManager;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.PriceFormatter;
import com.linh.snackgo.utils.SessionManager;
import java.util.Objects;

/**
 * adapter hiển thị lưới sản phẩm trên trang chủ.
 */
public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    private final OnProductClickListener listener;
    private FavoriteManager favoriteManager;
    private long userId;

    private static final int[] PASTEL_COLOR_RESOURCES = new int[]{
            R.color.pastel_peach,
            R.color.pastel_yellow,
            R.color.pastel_blue,
            R.color.pastel_pink
    };

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product, View addBtn);
        default void onFavoriteToggled(Product product, boolean isFavorite) {}
    }

    public ProductAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getPrice() == newItem.getPrice()
                    && oldItem.getOriginalPrice() == newItem.getOriginalPrice()
                    && oldItem.isActive() == newItem.isActive()
                    && Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getCoverImage(), newItem.getCoverImage());
        }
    };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product, int position) {
            // xoay vòng màu nền pastel
            int pastelColor = itemView.getContext().getColor(
                    PASTEL_COLOR_RESOURCES[position % PASTEL_COLOR_RESOURCES.length]);
            binding.cardProduct.setCardBackgroundColor(pastelColor);

            // nhãn sản phẩm
            if (product.getTag() != null && !product.getTag().trim().isEmpty()) {
                binding.tvTagBadge.setText(product.getTag().trim());
                binding.tvTagBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvTagBadge.setVisibility(View.GONE);
            }

            // tên sản phẩm và danh mục, giữ nội dung ngắn để dễ quét.
            binding.tvProductName.setText(product.getName());
            String categoryLabel;
            if (AppConstants.CATEGORY_DRINK.equalsIgnoreCase(product.getCategory())) {
                categoryLabel = itemView.getContext().getString(R.string.category_drink_label);
            } else if (AppConstants.CATEGORY_CANDY.equalsIgnoreCase(product.getCategory())) {
                categoryLabel = itemView.getContext().getString(R.string.category_candy_label);
            } else {
                categoryLabel = itemView.getContext().getString(R.string.category_snack_label);
            }
            binding.tvProductCategory.setText(categoryLabel);

            // giá bán và giá gốc
            binding.tvProductPrice.setText(PriceFormatter.formatPrice(product.getPrice()));
            if (product.getOriginalPrice() > product.getPrice()) {
                binding.tvOriginalPrice.setText(PriceFormatter.formatPrice(product.getOriginalPrice()));
                binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                binding.tvOriginalPrice.setVisibility(View.GONE);
            }

            // đồng bộ trạng thái yêu thích với FavoriteManager
            if (favoriteManager == null) {
                favoriteManager = new FavoriteManager(itemView.getContext());
                userId = new SessionManager(itemView.getContext()).getUserId();
            }
            boolean isFavorite = favoriteManager.isFavorite(userId, product.getId());
            binding.btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);

            binding.btnFavorite.setOnClickListener(v -> {
                boolean newFav = favoriteManager.toggleFavorite(userId, product.getId());
                binding.btnFavorite.setImageResource(newFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
                if (listener != null) {
                    listener.onFavoriteToggled(product, newFav);
                }
            });

            // tải ảnh bìa bằng BlendImageView trên nền pastel của card
            ImageUtils.loadBlendImage(itemView.getContext(), product.getCoverImage(), binding.ivProductImage, 300, pastelColor);

            // xử lý sự kiện chạm
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            binding.btnAddToCart.setOnClickListener(v -> {
                // hiệu ứng phóng to nhẹ
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() ->
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                ).start();

                if (listener != null) {
                    listener.onAddToCartClick(product, v);
                }
            });
        }
    }
}
