package com.linh.snackgo.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.linh.snackgo.R;
import com.linh.snackgo.databinding.ItemRelatedProductBinding;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.PriceFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * adapter hiển thị danh sách sản phẩm liên quan theo chiều ngang.
 */
public class RelatedProductAdapter extends RecyclerView.Adapter<RelatedProductAdapter.RelatedViewHolder> {

    public interface OnRelatedProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
    }

    private static final int[] PASTEL_COLOR_RESOURCES = new int[]{
            R.color.pastel_peach,
            R.color.pastel_yellow,
            R.color.pastel_blue,
            R.color.pastel_pink
    };

    private final List<Product> items = new ArrayList<>();
    private final OnRelatedProductClickListener listener;

    public RelatedProductAdapter(List<Product> products, OnRelatedProductClickListener listener) {
        if (products != null) {
            this.items.addAll(products);
        }
        this.listener = listener;
    }

    public void setItems(List<Product> products) {
        items.clear();
        if (products != null) {
            items.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRelatedProductBinding binding = ItemRelatedProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RelatedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class RelatedViewHolder extends RecyclerView.ViewHolder {
        private final ItemRelatedProductBinding binding;

        public RelatedViewHolder(@NonNull ItemRelatedProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product, int position) {
            int pastelColor = itemView.getContext().getColor(
                    PASTEL_COLOR_RESOURCES[position % PASTEL_COLOR_RESOURCES.length]);
            binding.cardRelatedProduct.setCardBackgroundColor(pastelColor);
            binding.ivRelatedImage.setCardBackgroundColor(pastelColor);
            binding.ivRelatedImage.setEnableBlend(true);

            ImageUtils.loadBlendImage(itemView.getContext(), product.getCoverImage(), binding.ivRelatedImage, 180, pastelColor);

            binding.tvRelatedName.setText(product.getName());
            binding.tvRelatedPrice.setText(PriceFormatter.formatPrice(product.getPrice()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            binding.btnAddRelated.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                }
            });
        }
    }
}
