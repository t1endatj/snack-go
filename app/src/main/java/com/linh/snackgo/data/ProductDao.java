package com.linh.snackgo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Product entities.
 */
public class ProductDao {
    private final DatabaseHelper dbHelper;
    private final ProductImageDao productImageDao;

    public ProductDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.productImageDao = new ProductImageDao(context);
    }

    public long insert(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapProductToValues(product);
        return db.insert(DbConstants.TABLE_PRODUCTS, null, values);
    }

    public boolean update(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = mapProductToValues(product);
        String whereClause = DbConstants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {String.valueOf(product.getId())};
        int rows = db.update(DbConstants.TABLE_PRODUCTS, values, whereClause, whereArgs);
        return rows > 0;
    }

    public boolean softDelete(long id) {
        return updateActiveStatus(id, 0);
    }

    public boolean updateActiveStatus(long id, int isActive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_PRODUCT_IS_ACTIVE, isActive);
        String whereClause = DbConstants.COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        int rows = db.update(DbConstants.TABLE_PRODUCTS, values, whereClause, whereArgs);
        return rows > 0;
    }

    public Product getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_PRODUCT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        try (Cursor cursor = db.query(DbConstants.TABLE_PRODUCTS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Product product = mapCursorToProduct(cursor);
                product.setImages(productImageDao.getByProduct(id));
                return product;
            }
        }
        return null;
    }

    public List<Product> getAllActive() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " = 1";

        try (Cursor cursor = db.query(DbConstants.TABLE_PRODUCTS, null, selection, null, null, null, DbConstants.COLUMN_PRODUCT_ID + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        }
        attachImagesToProducts(list);
        return list;
    }

    public List<Product> getActiveByCategory(String category) {
        if (category == null || AppConstants.CATEGORY_ALL.equalsIgnoreCase(category)) {
            return getAllActive();
        }
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " = 1 AND " + DbConstants.COLUMN_PRODUCT_CATEGORY + " = ?";
        String[] selectionArgs = {category};

        try (Cursor cursor = db.query(DbConstants.TABLE_PRODUCTS, null, selection, selectionArgs, null, null, DbConstants.COLUMN_PRODUCT_ID + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        }
        attachImagesToProducts(list);
        return list;
    }

    public List<Product> searchActive(String keyword, String category) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder(DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " = 1");
        List<String> argsList = new ArrayList<>();

        if (category != null && !category.isEmpty() && !AppConstants.CATEGORY_ALL.equalsIgnoreCase(category)) {
            selection.append(" AND ").append(DbConstants.COLUMN_PRODUCT_CATEGORY).append(" = ?");
            argsList.add(category);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            selection.append(" AND (").append(DbConstants.COLUMN_PRODUCT_NAME).append(" LIKE ? OR ")
                     .append(DbConstants.COLUMN_PRODUCT_TAG).append(" LIKE ?)");
            String likePattern = "%" + keyword.trim() + "%";
            argsList.add(likePattern);
            argsList.add(likePattern);
        }

        String[] selectionArgs = argsList.toArray(new String[0]);

        try (Cursor cursor = db.query(DbConstants.TABLE_PRODUCTS, null, selection.toString(), selectionArgs, null, null, DbConstants.COLUMN_PRODUCT_ID + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        }
        attachImagesToProducts(list);
        return list;
    }

    public List<Product> getRelatedProducts(long excludeProductId, String category, int limit) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " = 1 AND "
                + DbConstants.COLUMN_PRODUCT_CATEGORY + " = ? AND "
                + DbConstants.COLUMN_PRODUCT_ID + " != ?";
        String[] selectionArgs = {category, String.valueOf(excludeProductId)};
        String limitClause = limit > 0 ? String.valueOf(limit) : null;

        try (Cursor cursor = db.query(DbConstants.TABLE_PRODUCTS, null, selection, selectionArgs, null, null,
                DbConstants.COLUMN_PRODUCT_SOLD_COUNT + " DESC, " + DbConstants.COLUMN_PRODUCT_ID + " DESC", limitClause)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        }
        attachImagesToProducts(list);
        return list;
    }

    private void attachImagesToProducts(List<Product> products) {
        if (products == null || products.isEmpty()) return;
        Map<Long, List<String>> map = productImageDao.getAllImagesGroupedByProduct();
        for (Product p : products) {
            List<String> imgList = map.get(p.getId());
            if (imgList != null && !imgList.isEmpty()) {
                p.setImages(imgList);
            }
        }
    }

    private ContentValues mapProductToValues(Product product) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_PRODUCT_NAME, product.getName());
        values.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(DbConstants.COLUMN_PRODUCT_CATEGORY, product.getCategory());
        values.put(DbConstants.COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(DbConstants.COLUMN_PRODUCT_ORIGINAL_PRICE, product.getOriginalPrice());
        values.put(DbConstants.COLUMN_PRODUCT_TAG, product.getTag());
        values.put(DbConstants.COLUMN_PRODUCT_IMAGE, product.getImage());
        values.put(DbConstants.COLUMN_PRODUCT_IS_ACTIVE, product.isActive() ? 1 : 0);
        values.put(DbConstants.COLUMN_PRODUCT_ORIGIN, product.getOrigin());
        values.put(DbConstants.COLUMN_PRODUCT_MADE_IN, product.getMadeIn());
        values.put(DbConstants.COLUMN_PRODUCT_SIZE_INFO, product.getSizeInfo());
        values.put(DbConstants.COLUMN_PRODUCT_INGREDIENTS, product.getIngredients());
        values.put(DbConstants.COLUMN_PRODUCT_FEATURES, product.getFeatures());
        values.put(DbConstants.COLUMN_PRODUCT_USAGE_GUIDE, product.getUsageGuide());
        values.put(DbConstants.COLUMN_PRODUCT_STORAGE_GUIDE, product.getStorageGuide());
        values.put(DbConstants.COLUMN_PRODUCT_SHELF_LIFE, product.getShelfLife());
        values.put(DbConstants.COLUMN_PRODUCT_RATING, product.getRating());
        values.put(DbConstants.COLUMN_PRODUCT_SOLD_COUNT, product.getSoldCount());
        return values;
    }

    private Product mapCursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_ID)));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_NAME)));
        product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_DESCRIPTION)));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_CATEGORY)));
        product.setPrice(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_PRICE)));
        product.setOriginalPrice(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_ORIGINAL_PRICE)));
        product.setTag(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_TAG)));
        product.setImage(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_IMAGE)));
        product.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_IS_ACTIVE)) == 1);
        product.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_PRODUCT_CREATED_AT)));

        int originIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_ORIGIN);
        if (originIdx != -1) product.setOrigin(cursor.getString(originIdx));

        int madeInIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_MADE_IN);
        if (madeInIdx != -1) product.setMadeIn(cursor.getString(madeInIdx));

        int sizeIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_SIZE_INFO);
        if (sizeIdx != -1) product.setSizeInfo(cursor.getString(sizeIdx));

        int ingIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_INGREDIENTS);
        if (ingIdx != -1) product.setIngredients(cursor.getString(ingIdx));

        int featIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_FEATURES);
        if (featIdx != -1) product.setFeatures(cursor.getString(featIdx));

        int usageIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_USAGE_GUIDE);
        if (usageIdx != -1) product.setUsageGuide(cursor.getString(usageIdx));

        int storageIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_STORAGE_GUIDE);
        if (storageIdx != -1) product.setStorageGuide(cursor.getString(storageIdx));

        int shelfIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_SHELF_LIFE);
        if (shelfIdx != -1) product.setShelfLife(cursor.getString(shelfIdx));

        int ratingIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_RATING);
        if (ratingIdx != -1) product.setRating(cursor.getDouble(ratingIdx));

        int soldIdx = cursor.getColumnIndex(DbConstants.COLUMN_PRODUCT_SOLD_COUNT);
        if (soldIdx != -1) product.setSoldCount(cursor.getInt(soldIdx));

        return product;
    }
}
