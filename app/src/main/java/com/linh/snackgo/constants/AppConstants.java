package com.linh.snackgo.constants;

public final class AppConstants {
    private AppConstants() {
        // Private constructor to prevent instantiation
    }

    // Business rules & defaults
    public static final long DEFAULT_DELIVERY_FEE = 15000L;
    public static final long FREE_DELIVERY_THRESHOLD = 100000L;
    public static final String PROMO_CODE = "SNACK10";
    public static final String PROMO_CODE_SNACK10 = "SNACK10";
    public static final int PROMO_PERCENT = 10;
    public static final int PROMO_DISCOUNT_PERCENT = 10;
    public static final int MAX_QUANTITY_PER_ITEM = 99;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_ADDRESS_LENGTH = 5;
    public static final String PHONE_PREFIX = "+84";
    public static final String PHONE_REGEX = "^(0[35789][0-9]{8}|[35789][0-9]{8})$";
    public static final String CURRENCY_SYMBOL = "đ";
    public static final int SPLASH_DELAY_MS = 2000;
    public static final int SEARCH_DEBOUNCE_MS = 250;
    public static final int GRID_SPAN_COUNT = 2;

    // BlendImageView constants
    public static final float DEFAULT_BLEND_PADDING_RATIO = 0.10f;
    public static final float BLEND_BRIGHTNESS_SCALE = 1.06f;
    public static final int WHITE_SAMPLE_THRESHOLD = 235;
    public static final float SHADOW_ALPHA_RATIO = 0.08f;
    public static final float SHADOW_WIDTH_RATIO = 0.60f;
    public static final float SHADOW_HEIGHT_RATIO = 0.10f;
    public static final String DELIVERY_TIME_ESTIMATE = "15 phút";
    public static final int MAX_LEN_SHORT_FIELD = 100;
    public static final int MAX_RELATED_PRODUCTS = 6;

    // Admin product constraints
    public static final int MAX_PRODUCT_NAME_LENGTH = 100;
    public static final long MAX_PRODUCT_PRICE = 100000000L;
    public static final int MAX_PRODUCT_DESC_LENGTH = 1000;
    public static final int MAX_PRODUCT_TAG_LENGTH = 30;
    public static final int MAX_PRODUCT_IMAGES = 6;
    public static final String IMAGE_FOLDER_PRODUCTS = "products";
    public static final int MAX_IMAGE_SIDE_PX = 1024;
    public static final int JPEG_QUALITY = 85;

    // Seeded Admin Credentials
    public static final String SEED_ADMIN_EMAIL = "admin@snackgo.com";
    public static final String SEED_ADMIN_PASSWORD = "Admin@123";
    public static final String SEED_ADMIN_NAME = "Quản trị viên";
    public static final String SEED_ADMIN_PHONE = "0900000000";

    // Roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Categories
    public static final String CATEGORY_ALL = "all";
    public static final String CATEGORY_SNACK = "snack";
    public static final String CATEGORY_DRINK = "drink";
    public static final String CATEGORY_CANDY = "candy";

    // Order Statuses
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_DELIVERING = "DELIVERING";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    // Payment Methods
    public static final String PAYMENT_CASH = "CASH";
    public static final String PAYMENT_BANK = "BANK";
    public static final String PAYMENT_EWALLET = "EWALLET";
}
