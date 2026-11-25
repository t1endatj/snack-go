package com.linh.snackgo.controller;

import android.content.Context;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.data.CartDao;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.CartSummary;
import com.linh.snackgo.utils.SessionManager;
import java.util.Collections;
import java.util.List;

/**
 * xử lý giỏ hàng và tính tổng tiền.
 */
public class CartController {
    private final CartDao cartDao;
    private final SessionManager sessionManager;

    public CartController(Context context) {
        this.cartDao = new CartDao(context);
        this.sessionManager = new SessionManager(context);
    }

    public boolean addToCart(long userId, long productId) {
        return addToCart(userId, productId, 1);
    }

    public boolean addToCart(long userId, long productId, int quantity) {
        return sessionManager.isCurrentUser(userId) && cartDao.addOrIncrease(userId, productId, quantity);
    }

    public boolean updateQuantity(long userId, long productId, int quantity) {
        return sessionManager.isCurrentUser(userId) && cartDao.setQuantity(userId, productId, quantity);
    }

    public boolean removeItem(long userId, long productId) {
        return sessionManager.isCurrentUser(userId) && cartDao.remove(userId, productId);
    }

    public boolean restoreItem(long userId, long productId, int quantity) {
        return addToCart(userId, productId, quantity);
    }

    public boolean validatePromo(String code) {
        if (code == null) return false;
        return AppConstants.PROMO_CODE.equalsIgnoreCase(code.trim());
    }

    public CartSummary computeSummary(List<CartItem> items, boolean promoApplied) {
        long subtotal = 0;
        if (items != null) {
            for (CartItem item : items) {
                subtotal += item.getItemTotalPrice();
            }
        }

        long deliveryFee = 0;
        if (subtotal > 0) {
            deliveryFee = (subtotal >= AppConstants.FREE_DELIVERY_THRESHOLD) ? 0L : AppConstants.DEFAULT_DELIVERY_FEE;
        }

        long discount = 0;
        if (promoApplied && subtotal > 0) {
            discount = subtotal * AppConstants.PROMO_PERCENT / 100;
        }

        long total = Math.max(0L, subtotal + deliveryFee - discount);
        return new CartSummary(subtotal, deliveryFee, discount, total);
    }

    public int getCartCount(long userId) {
        return sessionManager.isCurrentUser(userId) ? cartDao.getTotalCount(userId) : 0;
    }

    public long getCartTotal(long userId) {
        List<CartItem> items = getCartItems(userId);
        return computeSummary(items, false).getTotal();
    }

    public List<CartItem> getCartItems(long userId) {
        return sessionManager.isCurrentUser(userId) ? cartDao.getItems(userId) : Collections.emptyList();
    }

    public CartItem getLastAddedItem(long userId) {
        List<CartItem> items = getCartItems(userId);
        if (items != null && !items.isEmpty()) {
            return items.get(items.size() - 1);
        }
        return null;
    }
}
