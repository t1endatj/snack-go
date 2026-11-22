package com.linh.snackgo.controller;

import android.content.Context;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.data.CartDao;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.CartSummary;
import java.util.List;

/**
 * Controller managing Cart operations, validation, and price summary calculations.
 */
public class CartController {
    private final CartDao cartDao;

    public CartController(Context context) {
        this.cartDao = new CartDao(context);
    }

    public boolean addToCart(long userId, long productId) {
        return addToCart(userId, productId, 1);
    }

    public boolean addToCart(long userId, long productId, int quantity) {
        return cartDao.addOrIncrease(userId, productId, quantity);
    }

    public boolean updateQuantity(long userId, long productId, int quantity) {
        return cartDao.setQuantity(userId, productId, quantity);
    }

    public boolean removeItem(long userId, long productId) {
        return cartDao.remove(userId, productId);
    }

    public boolean restoreItem(long userId, long productId, int quantity) {
        return cartDao.addOrIncrease(userId, productId, quantity);
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
            discount = (long) (subtotal * AppConstants.PROMO_PERCENT / 100.0);
        }

        long total = Math.max(0L, subtotal + deliveryFee - discount);
        return new CartSummary(subtotal, deliveryFee, discount, total);
    }

    public int getCartCount(long userId) {
        return cartDao.getTotalCount(userId);
    }

    public long getCartTotal(long userId) {
        List<CartItem> items = cartDao.getItems(userId);
        return computeSummary(items, false).getTotal();
    }

    public List<CartItem> getCartItems(long userId) {
        return cartDao.getItems(userId);
    }

    public CartItem getLastAddedItem(long userId) {
        List<CartItem> items = cartDao.getItems(userId);
        if (items != null && !items.isEmpty()) {
            return items.get(items.size() - 1);
        }
        return null;
    }
}
