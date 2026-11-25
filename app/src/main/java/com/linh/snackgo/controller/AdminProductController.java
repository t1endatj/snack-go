package com.linh.snackgo.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.NameUtils;
import com.linh.snackgo.utils.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Collections;
import java.util.Locale;

/**
 * quản lý sản phẩm và ảnh cho quản trị viên.
 */
public class AdminProductController {
    private final Context context;
    private final ProductDao productDao;
    private final SessionManager sessionManager;

    public AdminProductController(Context context) {
        this.context = context.getApplicationContext();
        this.productDao = new ProductDao(context);
        this.sessionManager = new SessionManager(context);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final int errorFieldId;

        public ValidationResult(boolean valid, String errorMessage, int errorFieldId) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.errorFieldId = errorFieldId;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null, 0);
        }

        public static ValidationResult error(String message, int errorFieldId) {
            return new ValidationResult(false, message, errorFieldId);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getErrorFieldId() {
            return errorFieldId;
        }
    }

    public List<Product> getActiveProducts(String category, String keyword) {
        return sessionManager.isAdmin() ? new ProductController(context).getActiveProducts(category, keyword)
                : Collections.emptyList();
    }

    public Product getProduct(long id) {
        return sessionManager.isAdmin() ? productDao.getById(id) : null;
    }

    public ValidationResult validate(String name, String priceStr, String originalPriceStr,
                                     String category, String description, String tag) {
        if (name == null || name.trim().isEmpty()) {
            return error(R.string.error_empty_product_name, R.id.etProductName);
        }

        if (name.trim().length() > AppConstants.MAX_PRODUCT_NAME_LENGTH) {
            return error(R.string.error_long_product_name, R.id.etProductName);
        }

        ValidationResult priceError = validatePrices(priceStr, originalPriceStr);
        if (priceError != null) return priceError;

        if (!AppConstants.CATEGORY_SNACK.equals(category) && !AppConstants.CATEGORY_DRINK.equals(category)
                && !AppConstants.CATEGORY_CANDY.equals(category)) {
            return error(R.string.error_empty_product_category, R.id.actvCategory);
        }

        if (description == null || description.trim().isEmpty()) {
            return error(R.string.error_empty_product_desc, R.id.etDescription);
        }
        if (description.trim().length() > AppConstants.MAX_PRODUCT_DESC_LENGTH) {
            return error(R.string.error_long_product_desc, R.id.etDescription);
        }

        if (tag != null && tag.trim().length() > AppConstants.MAX_PRODUCT_TAG_LENGTH) {
            return error(R.string.error_long_product_tag, R.id.etTag);
        }

        return ValidationResult.success();
    }

    private ValidationResult error(int messageId, int fieldId) {
        return ValidationResult.error(context.getString(messageId), fieldId);
    }

    private ValidationResult validatePrices(String priceStr, String originalPriceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return error(R.string.error_empty_product_price, R.id.etPrice);
        }

        long price;
        try {
            price = Long.parseLong(priceStr.trim());
        } catch (NumberFormatException e) {
            return error(R.string.error_invalid_product_price, R.id.etPrice);
        }

        if (price <= 0 || price > AppConstants.MAX_PRODUCT_PRICE) {
            return error(R.string.error_invalid_product_price, R.id.etPrice);
        }

        if (originalPriceStr != null && !originalPriceStr.trim().isEmpty()) {
            try {
                long origPrice = Long.parseLong(originalPriceStr.trim());
                if (origPrice < price || origPrice > AppConstants.MAX_PRODUCT_PRICE) {
                    return error(R.string.error_orig_price_less, R.id.etOriginalPrice);
                }
            } catch (NumberFormatException e) {
                return error(R.string.error_invalid_product_price, R.id.etOriginalPrice);
            }
        }

        return null;
    }

    public long addProduct(Product product) {
        if (!sessionManager.isAdmin() || product == null) return -1;
        normalizeProductText(product);
        return productDao.insert(product);
    }

    public boolean updateProduct(Product product) {
        if (!sessionManager.isAdmin() || product == null) return false;
        normalizeProductText(product);
        return productDao.update(product);
    }

    public boolean deleteProduct(long id) {
        return sessionManager.isAdmin() && productDao.softDelete(id);
    }

    public boolean restoreProduct(long id) {
        return sessionManager.isAdmin() && productDao.updateActiveStatus(id, 1);
    }

    public String saveImage(Context context, Uri uri) {
        if (!sessionManager.isAdmin()) return null;
        try {
            File productsDir = new File(context.getFilesDir(), AppConstants.IMAGE_FOLDER_PRODUCTS);
            if (!productsDir.exists()) {
                productsDir.mkdirs();
            }

            File destFile = new File(productsDir, "product_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".jpg");

            Bitmap bitmap;
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(in);
            }

            if (bitmap == null) return null;

            bitmap = resizeImage(bitmap);

            try (FileOutputStream out = new FileOutputStream(destFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, AppConstants.JPEG_QUALITY, out);
            }

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap resizeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) AppConstants.MAX_IMAGE_SIDE_PX / Math.max(width, height);
        if (ratio >= 1) return bitmap;
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(width * ratio)),
                Math.max(1, Math.round(height * ratio)), true);
        if (scaled != bitmap) bitmap.recycle();
        return scaled;
    }

    private void normalizeProductText(Product product) {
        if (product.getImage() != null && product.getImage().startsWith("/")) {
            product.setImage(product.getImages().isEmpty() ? null : product.getImages().get(0));
        }
        if (product.getName() != null) {
            product.setName(NameUtils.normalizeName(product.getName()));
        }
        if (product.getCategory() != null) {
            product.setCategory(product.getCategory().trim().toLowerCase(Locale.ROOT));
        }
        if (product.getDescription() != null) {
            product.setDescription(product.getDescription().trim());
        }
        if (product.getTag() != null) {
            product.setTag(product.getTag().trim());
        }
    }
}
