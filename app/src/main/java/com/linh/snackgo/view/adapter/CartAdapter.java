package com.linh.snackgo.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.databinding.ItemCartBinding;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.PriceFormatter;

/**
 * ListAdapter for Cart items.
 */
public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartViewHolder> {

    private final OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(OnCartItemChangeListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getQuantity() == newItem.getQuantity()
                    && oldItem.getProductId() == newItem.getProductId()
                    && (oldItem.getProduct() != null && newItem.getProduct() != null
                    && oldItem.getProduct().getPrice() == newItem.getProduct().getPrice());
        }
    };

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        public CartViewHolder(@NonNull ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem item) {
            if (item.getProduct() != null) {
                binding.tvCartProductName.setText(item.getProduct().getName());
                binding.tvCartUnitPrice.setText(PriceFormatter.formatPrice(item.getProduct().getPrice()));

                ImageUtils.loadBlendImage(itemView.getContext(), item.getProduct().getCoverImage(), binding.ivCartThumb, 120);
            }

            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            binding.btnMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1 && listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() - 1);
                }
            });

            binding.btnPlus.setOnClickListener(v -> {
                if (item.getQuantity() < AppConstants.MAX_QUANTITY_PER_ITEM && listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() + 1);
                }
            });

            binding.btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item);
                }
            });
        }
    }
}
