package com.linh.snackgo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.data.ProductImageDao;
import com.linh.snackgo.model.Product;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * đồng bộ ảnh từ thư mục tài nguyên vào cơ sở dữ liệu.
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
        for (Product product : products) {
            String folderName = product.getImage();
            if (!product.getImages().isEmpty() || folderName == null || folderName.trim().isEmpty() || folderName.startsWith("/")) {
                continue;
            }

            try {
                List<String> paths = readImagePaths(assetManager, folderName.trim());
                if (!paths.isEmpty()) productImageDao.replaceAll(product.getId(), paths);
            } catch (IOException ignored) {
                // bỏ qua thư mục không đọc được
            }
        }
    }

    private List<String> readImagePaths(AssetManager assets, String folderName) throws IOException {
        String assetPath = "products/" + folderName;
        String[] files = assets.list(assetPath);
        List<String> paths = new ArrayList<>();
        if (files == null) return paths;
        for (String file : files) {
            String lower = file.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                paths.add(DbConstants.ASSET_PREFIX + assetPath + "/" + file);
            }
        }
        Collections.sort(paths, ProductImageSyncer::compareNatural);
        return paths;
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
