package com.linh.snackgo.controller;

import android.content.Context;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.data.CartDao;
import com.linh.snackgo.data.OrderDao;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.CartSummary;
import com.linh.snackgo.model.Order;
import com.linh.snackgo.model.OrderItem;
import com.linh.snackgo.model.OrderSummary;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.NameUtils;
import com.linh.snackgo.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * xử lý đặt hàng, hủy đơn và mua lại.
 */
public class OrderController {
    private final Context context;
    private final OrderDao orderDao;
    private final CartDao cartDao;
    private final CartController cartController;
    private final SessionManager sessionManager;

    public OrderController(Context context) {
        this.context = context.getApplicationContext();
        this.orderDao = new OrderDao(context);
        this.cartDao = new CartDao(context);
        this.cartController = new CartController(context);
        this.sessionManager = new SessionManager(context);
    }

    public static class OrderResult {
        private final boolean success;
        private final String errorMessage;
        private final int errorFieldId;
        private final long orderId;
        private final String orderCode;
        private final long totalAmount;
        private final String paymentMethod;

        public OrderResult(boolean success, String errorMessage, int errorFieldId,
                           long orderId, String orderCode, long totalAmount, String paymentMethod) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.errorFieldId = errorFieldId;
            this.orderId = orderId;
            this.orderCode = orderCode;
            this.totalAmount = totalAmount;
            this.paymentMethod = paymentMethod;
        }

        public static OrderResult success(long orderId, String orderCode, long totalAmount, String paymentMethod) {
            return new OrderResult(true, null, 0, orderId, orderCode, totalAmount, paymentMethod);
        }

        public static OrderResult error(String message, int errorFieldId) {
            return new OrderResult(false, message, errorFieldId, -1, null, 0, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getErrorFieldId() {
            return errorFieldId;
        }

        public long getOrderId() {
            return orderId;
        }

        public String getOrderCode() {
            return orderCode;
        }

        public long getTotalAmount() {
            return totalAmount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }
    }

    public OrderResult placeOrder(long userId, String receiverName, String phone, String address,
                                   String paymentMethod, boolean promoApplied, List<Long> selectedProductIds) {
        if (!sessionManager.isCurrentUser(userId)) return error(R.string.error_invalid_credentials, 0);
        OrderResult validation = validateDelivery(receiverName, phone, address, paymentMethod);
        if (validation != null) return validation;
        List<CartItem> items = getSelectedItems(userId, selectedProductIds);
        if (items.isEmpty()) return error(R.string.label_empty_cart_title, 0);
        if (selectedProductIds != null && new HashSet<>(selectedProductIds).size() != items.size()) {
            return error(R.string.error_order_failed, 0);
        }
        CartSummary summary = cartController.computeSummary(items, promoApplied);
        Order order = buildOrder(userId, phone.trim(), address, paymentMethod, summary);
        if (!orderDao.createOrder(order, snapshotItems(items))) return error(R.string.error_order_failed, 0);
        return OrderResult.success(order.getId(), order.getOrderCode(), summary.getTotal(), paymentMethod);
    }

    private OrderResult error(int messageId, int fieldId) {
        return OrderResult.error(context.getString(messageId), fieldId);
    }

    private OrderResult validateDelivery(String receiverName, String phone, String address, String paymentMethod) {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return error(R.string.error_empty_full_name, R.id.etReceiverName);
        }

        if (phone == null || phone.trim().isEmpty()) {
            return error(R.string.error_empty_phone, R.id.etPhone);
        }

        if (!phone.trim().matches(AppConstants.PHONE_REGEX)) {
            return error(R.string.error_invalid_phone, R.id.etPhone);
        }

        if (address == null || address.trim().isEmpty()) {
            return error(R.string.error_empty_address, R.id.etAddress);
        }

        if (NameUtils.normalizeAddress(address).length() < AppConstants.MIN_ADDRESS_LENGTH) {
            return error(R.string.error_short_address, R.id.etAddress);
        }

        if (!AppConstants.PAYMENT_CASH.equals(paymentMethod) && !AppConstants.PAYMENT_BANK.equals(paymentMethod)
                && !AppConstants.PAYMENT_EWALLET.equals(paymentMethod)) {
            return error(R.string.error_order_failed, 0);
        }
        return null;
    }

    private List<CartItem> getSelectedItems(long userId, List<Long> selectedProductIds) {
        Set<Long> selected = selectedProductIds == null ? null : new HashSet<>(selectedProductIds);
        List<CartItem> cartItems = new ArrayList<>();
        for (CartItem item : cartDao.getItems(userId)) {
            if (selected == null || selected.contains(item.getProductId())) cartItems.add(item);
        }
        return cartItems;
    }

    private Order buildOrder(long userId, String phone, String address, String paymentMethod, CartSummary summary) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(summary.getTotal());
        order.setDiscountAmount(summary.getDiscount());
        order.setDeliveryFee(summary.getDeliveryFee());
        order.setPaymentMethod(paymentMethod);
        order.setDeliveryAddress(NameUtils.normalizeAddress(address));
        order.setPhone(phone);
        order.setStatus(AppConstants.ORDER_STATUS_PENDING);
        return order;
    }

    private List<OrderItem> snapshotItems(List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            orderItems.add(new OrderItem(cartItem.getProductId(), product.getName(),
                    product.getPrice(), cartItem.getQuantity()));
        }
        return orderItems;
    }

    public List<OrderSummary> getOrderSummaries(long userId) {
        return sessionManager.isCurrentUser(userId) ? orderDao.getOrderSummariesByUser(userId) : Collections.emptyList();
    }

    public Order getOrderDetail(long orderId) {
        Order order = orderDao.getById(orderId);
        return order != null && sessionManager.isCurrentUser(order.getUserId()) ? order : null;
    }

    public boolean cancelOrder(long orderId, long userId) {
        return sessionManager.isCurrentUser(userId) && orderDao.cancelPendingOrder(orderId, userId);
    }

    public boolean reorder(long orderId, long userId) {
        if (!sessionManager.isCurrentUser(userId)) return false;
        Order order = orderDao.getById(orderId);
        if (order == null || order.getUserId() != userId || order.getOrderItems() == null) {
            return false;
        }

        ProductDao productDao = new ProductDao(context);
        boolean anyAdded = false;
        for (OrderItem item : order.getOrderItems()) {
            Product product = productDao.getById(item.getProductId());
            if (product != null && product.isActive()) {
                anyAdded |= cartDao.addOrIncrease(userId, product.getId(), item.getQuantity());
            }
        }
        return anyAdded;
    }
}
