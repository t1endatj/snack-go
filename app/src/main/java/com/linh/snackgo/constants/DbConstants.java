package com.linh.snackgo.constants;

public final class DbConstants {
    private DbConstants() {
        // Private constructor to prevent instantiation
    }

    public static final String DB_NAME = "snackgo.db";
    public static final int DB_VERSION = 29;

    // Table Users: users(id PK, full_name, email UNIQUE, phone, password_hash, salt, address, avatar_path, role, created_at)
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_FULL_NAME = "full_name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_USER_SALT = "salt";
    public static final String COLUMN_USER_ADDRESS = "address";
    public static final String COLUMN_USER_AVATAR_PATH = "avatar_path";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_CREATED_AT = "created_at";

    // Table Products: products(id PK, name, description, category, price, original_price, tag, image, is_active, created_at, origin, made_in, size_info, ingredients, features, usage_guide, storage_guide, shelf_life, rating, sold_count)
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCT_ID = "id";
    public static final String COLUMN_PRODUCT_NAME = "name";
    public static final String COLUMN_PRODUCT_DESCRIPTION = "description";
    public static final String COLUMN_PRODUCT_CATEGORY = "category";
    public static final String COLUMN_PRODUCT_PRICE = "price";
    public static final String COLUMN_PRODUCT_ORIGINAL_PRICE = "original_price";
    public static final String COLUMN_PRODUCT_TAG = "tag";
    public static final String COLUMN_PRODUCT_IMAGE = "image";
    public static final String COLUMN_PRODUCT_IS_ACTIVE = "is_active";
    public static final String COLUMN_PRODUCT_CREATED_AT = "created_at";
    public static final String COLUMN_PRODUCT_ORIGIN = "origin";
    public static final String COLUMN_PRODUCT_MADE_IN = "made_in";
    public static final String COLUMN_PRODUCT_SIZE_INFO = "size_info";
    public static final String COLUMN_PRODUCT_INGREDIENTS = "ingredients";
    public static final String COLUMN_PRODUCT_FEATURES = "features";
    public static final String COLUMN_PRODUCT_USAGE_GUIDE = "usage_guide";
    public static final String COLUMN_PRODUCT_STORAGE_GUIDE = "storage_guide";
    public static final String COLUMN_PRODUCT_SHELF_LIFE = "shelf_life";
    public static final String COLUMN_PRODUCT_RATING = "rating";
    public static final String COLUMN_PRODUCT_SOLD_COUNT = "sold_count";

    // Table Product Images: product_images(id PK, product_id FK, path TEXT, sort_order INTEGER)
    public static final String TABLE_PRODUCT_IMAGES = "product_images";
    public static final String COLUMN_PRODUCT_IMAGE_ID = "id";
    public static final String COLUMN_PRODUCT_IMAGE_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_IMAGE_PATH = "path";
    public static final String COLUMN_PRODUCT_IMAGE_SORT_ORDER = "sort_order";
    public static final String ASSET_PREFIX = "asset:";

    // Table Cart Items: cart_items(id PK, user_id FK, product_id FK, quantity)
    public static final String TABLE_CART_ITEMS = "cart_items";
    public static final String COLUMN_CART_ID = "id";
    public static final String COLUMN_CART_USER_ID = "user_id";
    public static final String COLUMN_CART_PRODUCT_ID = "product_id";
    public static final String COLUMN_CART_QUANTITY = "quantity";

    // Table Orders: orders(id PK, user_id FK, order_code, total_amount, discount_amount, delivery_fee, payment_method, delivery_address, phone, status, created_at)
    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_ID = "id";
    public static final String COLUMN_ORDER_USER_ID = "user_id";
    public static final String COLUMN_ORDER_CODE = "order_code";
    public static final String COLUMN_ORDER_TOTAL_AMOUNT = "total_amount";
    public static final String COLUMN_ORDER_DISCOUNT_AMOUNT = "discount_amount";
    public static final String COLUMN_ORDER_DELIVERY_FEE = "delivery_fee";
    public static final String COLUMN_ORDER_PAYMENT_METHOD = "payment_method";
    public static final String COLUMN_ORDER_DELIVERY_ADDRESS = "delivery_address";
    public static final String COLUMN_ORDER_PHONE = "phone";
    public static final String COLUMN_ORDER_STATUS = "status";
    public static final String COLUMN_ORDER_CREATED_AT = "created_at";

    // Table Order Items: order_items(id PK, order_id FK, product_id, product_name, unit_price, quantity)
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COLUMN_ORDER_ITEM_ID = "id";
    public static final String COLUMN_ORDER_ITEM_ORDER_ID = "order_id";
    public static final String COLUMN_ORDER_ITEM_PRODUCT_ID = "product_id";
    public static final String COLUMN_ORDER_ITEM_PRODUCT_NAME = "product_name";
    public static final String COLUMN_ORDER_ITEM_UNIT_PRICE = "unit_price";
    public static final String COLUMN_ORDER_ITEM_QUANTITY = "quantity";
}
