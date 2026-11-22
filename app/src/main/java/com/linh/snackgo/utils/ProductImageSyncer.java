package com.linh.snackgo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.data.ProductImageDao;
import com.linh.snackgo.model.Product;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Background syncer that scans assets/products/<folder_name> and populates product_images table idempotently.
 */
public class ProductImageSyncer {

    private final Context context;
    private final ProductDao productDao;
    private final ProductImageDao productImageDao;

    public ProductImageSyncer(Context context) {
        this.context = context.getApplicationContext();
        this.productDao = new ProductDao(context);
        this.productImageDao = new ProductImageDao(context);
    }

    public void syncAssetImages() {
        AssetManager assetManager = context.getAssets();
        List<Product> products = productDao.getAllActive();
        if (products == null || products.isEmpty()) return;

        for (Product product : products) {
            String folderName = product.getImage();
            if (folderName == null || folderName.trim().isEmpty() || folderName.startsWith("/")) {
                continue;
            }

            try {
                String assetPath = "products/" + folderName.trim();
                String[] files = assetManager.list(assetPath);
                if (files == null || files.length == 0) {
                    continue;
                }

                List<String> validFiles = new ArrayList<>();
                for (String file : files) {
                    String lower = file.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                        validFiles.add(file);
                    }
                }

                if (validFiles.isEmpty()) {
                    continue;
                }

                Collections.sort(validFiles, (f1, f2) -> compareNatural(f1, f2));

                // Delete existing asset rows for this product and re-insert
                productImageDao.deleteAssetImagesByProduct(product.getId());

                for (int i = 0; i < validFiles.size(); i++) {
                    String path = DbConstants.ASSET_PREFIX + assetPath + "/" + validFiles.get(i);
                    productImageDao.insert(product.getId(), path, i);
                }
            } catch (IOException ignored) {
                // Folder missing or inaccessible, ignore safely
            }
        }
    }

    private static int compareNatural(String s1, String s2) {
        String n1 = s1.replaceAll("\\D+", "");
        String n2 = s2.replaceAll("\\D+", "");
        if (!n1.isEmpty() && !n2.isEmpty()) {
            try {
                int num1 = Integer.parseInt(n1);
                int num2 = Integer.parseInt(n2);
                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } catch (NumberFormatException ignored) {}
        }
        return s1.compareToIgnoreCase(s2);
    }
}
