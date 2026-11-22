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
import com.linh.snackgo.databinding.ItemAdminProductBinding;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.PriceFormatter;

/**
 * ListAdapter for Admin Product Management list.
 */
public class AdminProductAdapter extends ListAdapter<Product, AdminProductAdapter.AdminProductViewHolder> {

    private final OnAdminProductClickListener listener;

    public interface OnAdminProductClickListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }

    public AdminProductAdapter(OnAdminProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getPrice() == newItem.getPrice()
                    && oldItem.isActive() == newItem.isActive()
                    && String.valueOf(oldItem.getCoverImage()).equals(newItem.getCoverImage());
        }
    };

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProductBinding binding = ItemAdminProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class AdminProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminProductBinding binding;

        public AdminProductViewHolder(@NonNull ItemAdminProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvCategoryLabel.setText(product.getCategory());
            binding.tvProductPrice.setText(PriceFormatter.formatPrice(product.getPrice()));

            if (product.getTag() != null && !product.getTag().trim().isEmpty()) {
                binding.tvTagBadge.setText(product.getTag().trim());
                binding.tvTagBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvTagBadge.setVisibility(View.GONE);
            }

            if (product.getOriginalPrice() > product.getPrice()) {
                binding.tvOriginalPrice.setText(PriceFormatter.formatPrice(product.getOriginalPrice()));
                binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                binding.tvOriginalPrice.setVisibility(View.GONE);
            }

            // Cover image loading via BlendImageView with #F3F1EE neutral background
            ImageUtils.loadBlendImage(itemView.getContext(), product.getCoverImage(), binding.ivProductThumb, 120, itemView.getContext().getColor(R.color.promo_input_bg));

            binding.btnEditProduct.setOnClickListener(v -> {
                if (listener != null) listener.onEditProduct(product);
            });

            binding.btnDeleteProduct.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteProduct(product);
            });
        }
    }
}
