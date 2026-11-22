package com.linh.snackgo.model;

/**
 * Cart item data model, holding user ID, product ID, quantity, and joined Product details.
 */
public class CartItem {
    private long id;
    private long userId;
    private long productId;
    private int quantity;
    private Product product;

    public CartItem() {
    }

    public CartItem(long id, long userId, long productId, int quantity) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartItem(long id, long userId, long productId, int quantity, Product product) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.product = product;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public long getItemTotalPrice() {
        if (product != null) {
            return product.getPrice() * quantity;
        }
        return 0L;
    }
}
