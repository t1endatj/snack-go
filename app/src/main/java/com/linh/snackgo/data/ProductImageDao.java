package com.linh.snackgo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.linh.snackgo.constants.DbConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for ProductImage entities.
 */
public class ProductImageDao {
    private final DatabaseHelper dbHelper;

    public ProductImageDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(long productId, String path, int sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID, productId);
        values.put(DbConstants.COLUMN_PRODUCT_IMAGE_PATH, path);
        values.put(DbConstants.COLUMN_PRODUCT_IMAGE_SORT_ORDER, sortOrder);
        return db.insert(DbConstants.TABLE_PRODUCT_IMAGES, null, values);
    }

    public boolean deleteByProduct(long productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + " = ?";
        String[] whereArgs = {String.valueOf(productId)};
        int rows = db.delete(DbConstants.TABLE_PRODUCT_IMAGES, whereClause, whereArgs);
        return rows > 0;
    }

    public boolean deleteAssetImagesByProduct(long productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + " = ? AND " + DbConstants.COLUMN_PRODUCT_IMAGE_PATH + " LIKE ?";
        String[] whereArgs = {String.valueOf(productId), DbConstants.ASSET_PREFIX + "%"};
        int rows = db.delete(DbConstants.TABLE_PRODUCT_IMAGES, whereClause, whereArgs);
        return rows > 0;
    }

    public boolean deleteNonAssetImagesByProduct(long productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + " = ? AND " + DbConstants.COLUMN_PRODUCT_IMAGE_PATH + " NOT LIKE ?";
        String[] whereArgs = {String.valueOf(productId), DbConstants.ASSET_PREFIX + "%"};
        int rows = db.delete(DbConstants.TABLE_PRODUCT_IMAGES, whereClause, whereArgs);
        return rows > 0;
    }

    public List<String> getByProduct(long productId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(productId)};
        String orderBy = DbConstants.COLUMN_PRODUCT_IMAGE_SORT_ORDER + " ASC, " + DbConstants.COLUMN_PRODUCT_IMAGE_ID + " ASC";

        try (Cursor cursor = db.query(DbConstants.TABLE_PRODUCT_IMAGES, new String[]{DbConstants.COLUMN_PRODUCT_IMAGE_PATH},
                selection, selectionArgs, null, null, orderBy)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public Map<Long, List<String>> getAllImagesGroupedByProduct() {
        Map<Long, List<String>> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String orderBy = DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + " ASC, " + DbConstants.COLUMN_PRODUCT_IMAGE_SORT_ORDER + " ASC, " + DbConstants.COLUMN_PRODUCT_IMAGE_ID + " ASC";

        try (Cursor cursor = db.query(DbConstants.TABLE_PRODUCT_IMAGES,
                new String[]{DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID, DbConstants.COLUMN_PRODUCT_IMAGE_PATH},
                null, null, null, null, orderBy)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long productId = cursor.getLong(0);
                    String path = cursor.getString(1);
                    List<String> list = map.get(productId);
                    if (list == null) {
                        list = new ArrayList<>();
                        map.put(productId, list);
                    }
                    list.add(path);
                } while (cursor.moveToNext());
            }
        }
        return map;
    }

    public Map<Long, String> getCoverImagesForAllProducts() {
        Map<Long, String> map = new HashMap<>();
        Map<Long, List<String>> allGrouped = getAllImagesGroupedByProduct();
        for (Map.Entry<Long, List<String>> entry : allGrouped.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                map.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return map;
    }
}
