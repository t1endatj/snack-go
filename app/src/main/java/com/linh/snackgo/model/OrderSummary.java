package com.linh.snackgo.model;

/**
 * Model class holding order summary info for order cards.
 */
public class OrderSummary {
    private final Order order;
    private final String firstItemName;
    private final String firstItemImage;
    private final int firstItemQuantity;
    private final int totalItemTypes;

    public OrderSummary(Order order, String firstItemName, String firstItemImage, int firstItemQuantity, int totalItemTypes) {
        this.order = order;
        this.firstItemName = firstItemName;
        this.firstItemImage = firstItemImage;
        this.firstItemQuantity = firstItemQuantity;
        this.totalItemTypes = totalItemTypes;
    }

    public Order getOrder() {
        return order;
    }

    public String getFirstItemName() {
        return firstItemName;
    }

    public String getFirstItemImage() {
        return firstItemImage;
    }

    public int getFirstItemQuantity() {
        return firstItemQuantity;
    }

    public int getTotalItemTypes() {
        return totalItemTypes;
    }
}
