package com.linh.snackgo.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.databinding.ItemOrderBinding;
import com.linh.snackgo.model.Order;
import com.linh.snackgo.model.OrderSummary;
import com.linh.snackgo.utils.DateUtils;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.PriceFormatter;
import java.io.File;

/**
 * ListAdapter for Order history list using OrderSummary.
 */
public class OrderAdapter extends ListAdapter<OrderSummary, OrderAdapter.OrderViewHolder> {

    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(OnOrderClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<OrderSummary> DIFF_CALLBACK = new DiffUtil.ItemCallback<OrderSummary>() {
        @Override
        public boolean areItemsTheSame(@NonNull OrderSummary oldItem, @NonNull OrderSummary newItem) {
            return oldItem.getOrder().getId() == newItem.getOrder().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull OrderSummary oldItem, @NonNull OrderSummary newItem) {
            return oldItem.getOrder().getStatus().equals(newItem.getOrder().getStatus())
                    && oldItem.getOrder().getTotalAmount() == newItem.getOrder().getTotalAmount()
                    && oldItem.getTotalItemTypes() == newItem.getTotalItemTypes();
        }
    };

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(@NonNull ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(OrderSummary summary) {
            Order order = summary.getOrder();
            if (order == null) return;

            binding.tvOrderCode.setText(order.getOrderCode());
            binding.tvOrderDate.setText(DateUtils.formatDateTime(order.getCreatedAt()));

            // Status chip contrast & payment method label
            setupStatusChip(order.getStatus());
            setupPaymentMethodLabel(order.getPaymentMethod());

            // First item title & count
            String title = summary.getFirstItemName();
            if (summary.getTotalItemTypes() > 1) {
                title += " +" + (summary.getTotalItemTypes() - 1) + " " + itemView.getContext().getString(R.string.label_other_items);
            }
            binding.tvOrderItemsTitle.setText(title);

            int totalQty = 0;
            if (order.getOrderItems() != null) {
                for (com.linh.snackgo.model.OrderItem item : order.getOrderItems()) {
                    totalQty += item.getQuantity();
                }
            }
            binding.tvOrderItemCount.setText(itemView.getContext().getString(R.string.label_mini_cart_format, totalQty));
            binding.tvOrderTotal.setText(PriceFormatter.formatPrice(order.getTotalAmount()));

            // Thumbnail loading
            String img = summary.getFirstItemImage();
            if (img != null && img.startsWith("/")) {
                File imgFile = new File(img);
                if (imgFile.exists()) {
                    Glide.with(itemView.getContext()).load(imgFile).into(binding.ivOrderFirstThumb);
                } else {
                    binding.ivOrderFirstThumb.setImageResource(R.drawable.ic_placeholder_product);
                }
            } else if (img != null && !img.isEmpty()) {
                int resId = ImageUtils.getDrawableResId(itemView.getContext(), img);
                binding.ivOrderFirstThumb.setImageResource(resId);
            } else {
                binding.ivOrderFirstThumb.setImageResource(R.drawable.ic_placeholder_product);
            }

            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            };

            itemView.setOnClickListener(clickListener);
            binding.btnViewDetail.setOnClickListener(clickListener);
        }

        private void setupPaymentMethodLabel(String method) {
            String label = itemView.getContext().getString(R.string.payment_cash_title);
            if (AppConstants.PAYMENT_BANK.equalsIgnoreCase(method)) {
                label = itemView.getContext().getString(R.string.payment_bank_title);
            } else if (AppConstants.PAYMENT_EWALLET.equalsIgnoreCase(method)) {
                label = itemView.getContext().getString(R.string.payment_wallet_title);
            }
            binding.tvPaymentMethodLabel.setText(label);
        }

        private void setupStatusChip(String status) {
            int bgRes;
            int textColor;
            String text;

            if (AppConstants.ORDER_STATUS_DELIVERING.equalsIgnoreCase(status)) {
                bgRes = R.drawable.bg_status_delivering;
                textColor = itemView.getContext().getColor(R.color.status_delivering_text);
                text = itemView.getContext().getString(R.string.label_order_status_shipping);
            } else if (AppConstants.ORDER_STATUS_COMPLETED.equalsIgnoreCase(status)) {
                bgRes = R.drawable.bg_status_completed;
                textColor = itemView.getContext().getColor(R.color.status_completed_text);
                text = itemView.getContext().getString(R.string.label_order_status_completed);
            } else if (AppConstants.ORDER_STATUS_CANCELLED.equalsIgnoreCase(status)) {
                bgRes = R.drawable.bg_status_cancelled;
                textColor = itemView.getContext().getColor(R.color.status_cancelled_text);
                text = itemView.getContext().getString(R.string.label_order_status_cancelled);
            } else { // PENDING
                bgRes = R.drawable.bg_status_pending;
                textColor = itemView.getContext().getColor(R.color.status_pending_text);
                text = itemView.getContext().getString(R.string.label_order_status_pending);
            }

            binding.tvOrderStatusChip.setBackgroundResource(bgRes);
            binding.tvOrderStatusChip.setTextColor(textColor);
            binding.tvOrderStatusChip.setText(text);
        }
    }
}
