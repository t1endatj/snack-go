package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.OrderController;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.databinding.ActivityOrderDetailBinding;
import com.linh.snackgo.model.Order;
import com.linh.snackgo.model.OrderItem;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.DateUtils;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.PriceFormatter;
import com.linh.snackgo.utils.SessionManager;

import java.util.List;

/**
 * Order Detail Activity displaying snapshot items, status timeline, cancellation, and reorder action.
 */
public class OrderDetailActivity extends AppCompatActivity {
    private ActivityOrderDetailBinding binding;
    private OrderController orderController;
    private ProductDao productDao;
    private SessionManager sessionManager;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderController = new OrderController(this);
        productDao = new ProductDao(this);
        sessionManager = new SessionManager(this);

        long orderId = getIntent().getLongExtra(IntentKeys.EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            Toast.makeText(this, R.string.error_product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetail(orderId);
        setupListeners();
    }

    private void loadOrderDetail(long orderId) {
        order = orderController.getOrderDetail(orderId);
        if (order == null) {
            Toast.makeText(this, R.string.error_product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvOrderCode.setText(order.getOrderCode());
        binding.tvOrderDate.setText(DateUtils.formatDateTime(order.getCreatedAt()));

        setupStatusChipAndTimeline(order.getStatus());

        binding.tvReceiverName.setText(sessionManager.getUserName());
        binding.tvReceiverPhone.setText(order.getPhone() != null ? order.getPhone() : "");
        binding.tvReceiverAddress.setText(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");

        String methodLabel = getString(R.string.payment_cash_title);
        if (AppConstants.PAYMENT_BANK.equalsIgnoreCase(order.getPaymentMethod())) {
            methodLabel = getString(R.string.payment_bank_title);
        } else if (AppConstants.PAYMENT_EWALLET.equalsIgnoreCase(order.getPaymentMethod())) {
            methodLabel = getString(R.string.payment_wallet_title);
        }
        binding.tvPaymentMethod.setText(methodLabel);

        // Snapshot Items List
        populateSnapshotItems(order.getOrderItems());

        // Summary
        binding.tvSubtotal.setText(PriceFormatter.formatPrice(order.getTotalAmount() + order.getDiscountAmount() - order.getDeliveryFee()));
        if (order.getDeliveryFee() == 0) {
            binding.tvDeliveryFee.setText(R.string.label_free);
            binding.tvDeliveryFee.setTextColor(getColor(R.color.accent_orange));
        } else {
            binding.tvDeliveryFee.setText(PriceFormatter.formatPrice(order.getDeliveryFee()));
            binding.tvDeliveryFee.setTextColor(getColor(R.color.primary_ink));
        }

        if (order.getDiscountAmount() > 0) {
            binding.tvDiscount.setText("-" + PriceFormatter.formatPrice(order.getDiscountAmount()));
            binding.layoutDiscountRow.setVisibility(View.VISIBLE);
        } else {
            binding.layoutDiscountRow.setVisibility(View.GONE);
        }

        binding.tvTotal.setText(PriceFormatter.formatPrice(order.getTotalAmount()));

        // Cancel button visible only when PENDING
        if (AppConstants.ORDER_STATUS_PENDING.equalsIgnoreCase(order.getStatus())) {
            binding.btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            binding.btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void setupStatusChipAndTimeline(String status) {
        int bgRes = R.drawable.bg_chip_logo;
        int textColor;
        String text;

        int activeColor = getColor(R.color.primary_ink);
        int inactiveColor = getColor(R.color.hint_grey);

        if (AppConstants.ORDER_STATUS_DELIVERING.equalsIgnoreCase(status)) {
            textColor = getColor(R.color.accent_orange);
            text = getString(R.string.label_order_status_shipping);
            binding.tvStepPending.setTextColor(activeColor);
            binding.tvStepDelivering.setTextColor(activeColor);
            binding.tvStepCompleted.setTextColor(inactiveColor);
        } else if (AppConstants.ORDER_STATUS_COMPLETED.equalsIgnoreCase(status)) {
            textColor = getColor(R.color.green_completed);
            text = getString(R.string.label_order_status_completed);
            binding.tvStepPending.setTextColor(activeColor);
            binding.tvStepDelivering.setTextColor(activeColor);
            binding.tvStepCompleted.setTextColor(activeColor);
        } else if (AppConstants.ORDER_STATUS_CANCELLED.equalsIgnoreCase(status)) {
            textColor = getColor(R.color.red);
            text = getString(R.string.label_order_status_cancelled);
            binding.layoutTimeline.setVisibility(View.GONE);
        } else { // PENDING
            textColor = getColor(R.color.amber);
            text = getString(R.string.label_order_status_pending);
            binding.tvStepPending.setTextColor(activeColor);
            binding.tvStepDelivering.setTextColor(inactiveColor);
            binding.tvStepCompleted.setTextColor(inactiveColor);
        }

        binding.tvStatusChip.setBackgroundResource(bgRes);
        binding.tvStatusChip.setTextColor(textColor);
        binding.tvStatusChip.setText(text);
    }

    private void populateSnapshotItems(List<OrderItem> items) {
        binding.layoutSnapshotItems.removeAllViews();
        if (items == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (OrderItem item : items) {
            View itemView = inflater.inflate(R.layout.item_cart, binding.layoutSnapshotItems, false);
            View ivThumb = itemView.findViewById(R.id.ivCartThumb);
            TextView tvName = itemView.findViewById(R.id.tvCartProductName);
            TextView tvPrice = itemView.findViewById(R.id.tvCartUnitPrice);
            TextView tvQty = itemView.findViewById(R.id.tvQuantity);
            View btnRemove = itemView.findViewById(R.id.btnRemove);
            View layoutStepper = itemView.findViewById(R.id.layoutStepper);

            btnRemove.setVisibility(View.GONE);
            layoutStepper.setClickable(false);

            tvName.setText(item.getProductName());
            tvPrice.setText(PriceFormatter.formatPrice(item.getUnitPrice()));
            tvQty.setText("x" + item.getQuantity());

            Product p = productDao.getById(item.getProductId());
            if (ivThumb instanceof com.linh.snackgo.view.widget.BlendImageView) {
                ImageUtils.loadBlendImage(this, p != null ? p.getCoverImage() : null, (com.linh.snackgo.view.widget.BlendImageView) ivThumb, 120);
            } else if (ivThumb instanceof ImageView) {
                ImageUtils.loadProductImage(this, p != null ? p.getCoverImage() : null, (ImageView) ivThumb, 120);
            }

            binding.layoutSnapshotItems.addView(itemView);
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCancelOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_confirm_cancel_order)
                    .setMessage(R.string.msg_confirm_cancel_order)
                    .setPositiveButton(R.string.label_confirm, (dialog, which) -> performCancelOrder())
                    .setNegativeButton(R.string.label_cancel, null)
                    .show();
        });

        binding.btnReorder.setOnClickListener(v -> performReorder());
    }

    private void performCancelOrder() {
        long userId = sessionManager.getUserId();
        boolean success = orderController.cancelOrder(order.getId(), userId);
        if (success) {
            Snackbar.make(binding.getRoot(), R.string.msg_order_cancelled_success, Snackbar.LENGTH_SHORT).show();
            loadOrderDetail(order.getId());
        } else {
            Snackbar.make(binding.getRoot(), R.string.error_order_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void performReorder() {
        long userId = sessionManager.getUserId();
        boolean success = orderController.reorder(order.getId(), userId);
        if (success) {
            Toast.makeText(this, R.string.msg_reorder_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(IntentKeys.EXTRA_OPEN_TAB, R.id.nav_cart);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.error_product_not_found, Toast.LENGTH_SHORT).show();
        }
    }
}
