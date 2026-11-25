package com.linh.snackgo.controller;

import android.content.Context;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.data.CartDao;
import com.linh.snackgo.data.OrderDao;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.data.UserDao;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.CartSummary;
import com.linh.snackgo.model.Order;
import com.linh.snackgo.model.OrderItem;
import com.linh.snackgo.model.OrderSummary;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.model.User;
import com.linh.snackgo.utils.NameUtils;
import com.linh.snackgo.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller handling Order placement, business validation, status updates, and reorder execution.
 */
public class OrderController {
    private final Context context;
    private final OrderDao orderDao;
    private final CartDao cartDao;
    private final UserDao userDao;
    private final CartController cartController;
    private final SessionManager sessionManager;

    public OrderController(Context context) {
        this.context = context.getApplicationContext();
        this.orderDao = new OrderDao(context);
        this.cartDao = new CartDao(context);
        this.userDao = new UserDao(context);
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
                                   String paymentMethod, boolean promoApplied) {
        return placeOrder(userId, receiverName, phone, address, paymentMethod, promoApplied, null);
    }

    public OrderResult placeOrder(long userId, String receiverName, String phone, String address,
                                   String paymentMethod, boolean promoApplied, List<Long> selectedProductIds) {
        // 1. Validations
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return OrderResult.error(context.getString(R.string.error_empty_full_name), R.id.etReceiverName);
        }

        if (phone == null || phone.trim().isEmpty()) {
            return OrderResult.error(context.getString(R.string.error_empty_phone), R.id.etPhone);
        }

        String cleanPhone = phone.trim();
        if (!cleanPhone.matches(AppConstants.PHONE_REGEX)) {
            return OrderResult.error(context.getString(R.string.error_invalid_phone), R.id.etPhone);
        }

        if (address == null || address.trim().isEmpty()) {
            return OrderResult.error(context.getString(R.string.error_empty_address), R.id.etAddress);
        }

        String cleanAddress = NameUtils.normalizeAddress(address);
        if (cleanAddress.length() < AppConstants.MIN_ADDRESS_LENGTH) {
            return OrderResult.error(context.getString(R.string.error_short_address), R.id.etAddress);
        }

        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return OrderResult.error(context.getString(R.string.error_order_failed), 0);
        }

        // 2. Re-query cart items from DB (Never trust UI passed items) and filter by selected IDs
        List<CartItem> allCartItems = cartDao.getItems(userId);
        List<CartItem> cartItems = new ArrayList<>();
        if (allCartItems != null) {
            for (CartItem item : allCartItems) {
                if (selectedProductIds == null || selectedProductIds.contains(item.getProductId())) {
                    cartItems.add(item);
                }
            }
        }
        if (cartItems.isEmpty()) {
            return OrderResult.error(context.getString(R.string.label_empty_cart_title), 0);
        }

        // 3. Recompute summary from DB
        CartSummary summary = cartController.computeSummary(cartItems, promoApplied);

        // 4. Build Order & Snapshot OrderItems
        String orderCode = orderDao.generateOrderCode();
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode(orderCode);
        order.setTotalAmount(summary.getTotal());
        order.setDiscountAmount(summary.getDiscount());
        order.setDeliveryFee(summary.getDeliveryFee());
        order.setPaymentMethod(paymentMethod);
        order.setDeliveryAddress(cleanAddress);
        order.setPhone(cleanPhone);
        order.setStatus(AppConstants.ORDER_STATUS_PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct() != null) {
                OrderItem item = new OrderItem(
                        cartItem.getProductId(),
                        cartItem.getProduct().getName(),
                        cartItem.getProduct().getPrice(),
                        cartItem.getQuantity()
                );
                orderItems.add(item);
            }
        }

        // 5. Atomic DB Transaction (Insert Order, Insert OrderItems, Delete Cart)
        boolean dbSuccess = orderDao.createOrder(order, orderItems);
        if (!dbSuccess) {
            return OrderResult.error(context.getString(R.string.error_order_failed), 0);
        }

        // 6. Update user profile address if empty or updated
        User user = userDao.getById(userId);
        if (user != null) {
            userDao.update(userId, receiverName.trim(), cleanPhone, cleanAddress, user.getAvatarPath());
            sessionManager.updateUserProfile(receiverName.trim(), cleanPhone, cleanAddress);
        }

        return OrderResult.success(order.getId(), orderCode, summary.getTotal(), paymentMethod);
    }

    public List<Order> getOrders(long userId) {
        return orderDao.getOrdersByUser(userId);
    }

    public List<OrderSummary> getOrderSummaries(long userId) {
        return orderDao.getOrderSummariesByUser(userId);
    }

    public Order getOrderDetail(long orderId) {
        return orderDao.getById(orderId);
    }

    public boolean cancelOrder(long orderId, long userId) {
        Order order = orderDao.getById(orderId);
        if (order != null && order.getUserId() == userId && AppConstants.ORDER_STATUS_PENDING.equalsIgnoreCase(order.getStatus())) {
            return orderDao.updateStatus(orderId, AppConstants.ORDER_STATUS_CANCELLED);
        }
        return false;
    }

    public boolean reorder(long orderId, long userId) {
        Order order = orderDao.getById(orderId);
        if (order == null || order.getUserId() != userId || order.getOrderItems() == null) {
            return false;
        }

        ProductDao productDao = new ProductDao(context);
        boolean anyAdded = false;
        for (OrderItem item : order.getOrderItems()) {
            Product product = productDao.getById(item.getProductId());
            if (product != null && product.isActive()) {
                cartDao.addOrIncrease(userId, product.getId(), item.getQuantity());
                anyAdded = true;
            }
        }
        return anyAdded;
    }
}
