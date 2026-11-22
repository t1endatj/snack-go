package com.linh.snackgo.model;

import com.linh.snackgo.constants.AppConstants;

/**
 * Model class holding pricing summary calculations for Cart.
 */
public class CartSummary {
    private final long subtotal;
    private final long deliveryFee;
    private final long discount;
    private final long total;

    public CartSummary(long subtotal, long deliveryFee, long discount, long total) {
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.discount = discount;
        this.total = total;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public long getDeliveryFee() {
        return deliveryFee;
    }

    public long getDiscount() {
        return discount;
    }

    public long getTotal() {
        return total;
    }

    public boolean isFreeDelivery() {
        return subtotal >= AppConstants.FREE_DELIVERY_THRESHOLD && subtotal > 0;
    }
}
