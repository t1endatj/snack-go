package com.linh.snackgo.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.data.CartDao;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.data.ProductImageDao;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.NameUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Controller managing Admin Product CRUD operations, business validations, multi-image persistence, and file handling.
 */
public class AdminProductController {
    private final Context context;
    private final ProductDao productDao;
    private final ProductImageDao productImageDao;
    private final CartDao cartDao;

    public AdminProductController(Context context) {
        this.context = context.getApplicationContext();
        this.productDao = new ProductDao(context);
        this.productImageDao = new ProductImageDao(context);
        this.cartDao = new CartDao(context);
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
        if (keyword != null && !keyword.trim().isEmpty()) {
            return productDao.searchActive(keyword, category);
        } else {
            return productDao.getActiveByCategory(category);
        }
    }

    public Product getProduct(long id) {
        return productDao.getById(id);
    }

    public ValidationResult validate(String name, String priceStr, String originalPriceStr,
                                     String category, String description, String tag) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.error(context.getString(R.string.error_empty_product_name), R.id.etProductName);
        }

        if (name.trim().length() > AppConstants.MAX_PRODUCT_NAME_LENGTH) {
            return ValidationResult.error(context.getString(R.string.error_long_product_name), R.id.etProductName);
        }

        if (priceStr == null || priceStr.trim().isEmpty()) {
            return ValidationResult.error(context.getString(R.string.error_empty_product_price), R.id.etPrice);
        }

        long price;
        try {
            price = Long.parseLong(priceStr.trim());
        } catch (NumberFormatException e) {
            return ValidationResult.error(context.getString(R.string.error_invalid_product_price), R.id.etPrice);
        }

        if (price <= 0 || price > AppConstants.MAX_PRODUCT_PRICE) {
            return ValidationResult.error(context.getString(R.string.error_invalid_product_price), R.id.etPrice);
        }

        if (originalPriceStr != null && !originalPriceStr.trim().isEmpty()) {
            try {
                long origPrice = Long.parseLong(originalPriceStr.trim());
                if (origPrice < price) {
                    return ValidationResult.error(context.getString(R.string.error_orig_price_less), R.id.etOriginalPrice);
                }
            } catch (NumberFormatException e) {
                return ValidationResult.error(context.getString(R.string.error_invalid_product_price), R.id.etOriginalPrice);
            }
        }

        if (category == null || category.trim().isEmpty() || AppConstants.CATEGORY_ALL.equalsIgnoreCase(category)) {
            return ValidationResult.error(context.getString(R.string.error_empty_product_category), R.id.actvCategory);
        }

        if (description != null && description.trim().length() > AppConstants.MAX_PRODUCT_DESC_LENGTH) {
            return ValidationResult.error(context.getString(R.string.error_long_product_desc), R.id.etDescription);
        }

        if (tag != null && tag.trim().length() > AppConstants.MAX_PRODUCT_TAG_LENGTH) {
            return ValidationResult.error(context.getString(R.string.error_long_product_tag), R.id.etTag);
        }

        return ValidationResult.success();
    }

    public long addProduct(Product product) {
        normalizeProductText(product);
        long id = productDao.insert(product);
        if (id != -1 && product.getImages() != null && !product.getImages().isEmpty()) {
            saveProductImages(id, product.getImages());
        }
        return id;
    }

    public boolean updateProduct(Product product) {
        normalizeProductText(product);
        boolean success = productDao.update(product);
        if (success && product.getImages() != null) {
            saveProductImages(product.getId(), product.getImages());
        }
        return success;
    }

    public void saveProductImages(long productId, List<String> imagePaths) {
        if (imagePaths == null) return;
        productImageDao.deleteNonAssetImagesByProduct(productId);
        int sortOrder = 100;
        for (String path : imagePaths) {
            if (path != null && !path.startsWith(DbConstants.ASSET_PREFIX)) {
                productImageDao.insert(productId, path, sortOrder++);
            }
        }
    }

    public boolean deleteProduct(long id) {
        Product oldProduct = productDao.getById(id);
        if (oldProduct != null && oldProduct.getImages() != null) {
            for (String img : oldProduct.getImages()) {
                if (img != null && img.startsWith("/")) {
                    File oldFile = new File(img);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
            }
        }
        cartDao.removeProductFromAllCarts(id);
        return productDao.softDelete(id);
    }

    public boolean restoreProduct(long id) {
        return productDao.updateActiveStatus(id, 1);
    }

    public String saveImage(Context context, Uri uri) {
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

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int maxSide = AppConstants.MAX_IMAGE_SIDE_PX;

            if (width > maxSide || height > maxSide) {
                float ratio = Math.min((float) maxSide / width, (float) maxSide / height);
                int newWidth = Math.round(ratio * width);
                int newHeight = Math.round(ratio * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            try (FileOutputStream out = new FileOutputStream(destFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, AppConstants.JPEG_QUALITY, out);
            }

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void normalizeProductText(Product product) {
        if (product.getName() != null) {
            product.setName(NameUtils.normalizeName(product.getName()));
        }
        if (product.getCategory() != null) {
            product.setCategory(product.getCategory().trim().toLowerCase());
        }
        if (product.getDescription() != null) {
            product.setDescription(product.getDescription().trim());
        }
        if (product.getTag() != null) {
            product.setTag(product.getTag().trim());
        }
    }
}
