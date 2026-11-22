package com.linh.snackgo.controller;

import android.content.Context;
import com.linh.snackgo.data.ProductDao;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.NameUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controller managing Product retrieval, accent-insensitive search, category filtering, and price sorting.
 */
public class ProductController {
    private final ProductDao productDao;

    public ProductController(Context context) {
        this.productDao = new ProductDao(context);
    }

    public List<Product> getActiveProducts(String category, String keyword) {
        return getActiveProducts(category, keyword, 0);
    }

    public List<Product> getActiveProducts(String category, String keyword, int sortOption) {
        List<Product> productsFromDb = productDao.getActiveByCategory(category);
        List<Product> filtered = new ArrayList<>();

        String cleanKeyword = (keyword != null) ? NameUtils.stripAccents(keyword.trim()) : "";

        if (productsFromDb != null) {
            for (Product p : productsFromDb) {
                if (p == null || !p.isActive()) continue;

                if (cleanKeyword.isEmpty()) {
                    filtered.add(p);
                } else {
                    String normName = NameUtils.stripAccents(p.getName());
                    String normDesc = NameUtils.stripAccents(p.getDescription());
                    String normTag = NameUtils.stripAccents(p.getTag());

                    if (normName.contains(cleanKeyword) || normDesc.contains(cleanKeyword) || normTag.contains(cleanKeyword)) {
                        filtered.add(p);
                    }
                }
            }
        }

        if (sortOption == 1) { // Price Low to High
            Collections.sort(filtered, Comparator.comparingLong(Product::getPrice));
        } else if (sortOption == 2) { // Price High to Low
            Collections.sort(filtered, (p1, p2) -> Long.compare(p2.getPrice(), p1.getPrice()));
        }

        return filtered;
    }

    public Product getProductById(long id) {
        return productDao.getById(id);
    }

    public List<Product> getRelatedProducts(long excludeProductId, String category, int limit) {
        return productDao.getRelatedProducts(excludeProductId, category, limit);
    }
}
