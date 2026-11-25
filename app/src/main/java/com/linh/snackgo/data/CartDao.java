package com.linh.snackgo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.model.CartItem;
import com.linh.snackgo.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * truy xuất dữ liệu giỏ hàng.
 */
public class CartDao {
    private final DatabaseHelper dbHelper;
    private final Context context;

    public CartDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean addOrIncrease(long userId, long productId, int qty) {
        if (userId <= 0 || qty < 1) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Product product = new ProductDao(context).getById(productId);
            if (product == null || !product.isActive()) return false;
            int currentQty = getQuantity(db, userId, productId);
            int newQty = (int) Math.min((long) currentQty + qty, AppConstants.MAX_QUANTITY_PER_ITEM);
            if (newQty == currentQty) return false;
            ContentValues values = new ContentValues();
            values.put(DbConstants.COLUMN_CART_USER_ID, userId);
            values.put(DbConstants.COLUMN_CART_PRODUCT_ID, productId);
            values.put(DbConstants.COLUMN_CART_QUANTITY, newQty);
            boolean saved = currentQty == 0 ? db.insert(DbConstants.TABLE_CART_ITEMS, null, values) != -1
                    : setQuantity(userId, productId, newQty);
            if (!saved) return false;
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private int getQuantity(SQLiteDatabase db, long userId, long productId) {
        try (Cursor cursor = db.query(DbConstants.TABLE_CART_ITEMS,
                new String[]{DbConstants.COLUMN_CART_QUANTITY},
                DbConstants.COLUMN_CART_USER_ID + " = ? AND " + DbConstants.COLUMN_CART_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public boolean setQuantity(long userId, long productId, int qty) {
        if (qty < 1) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int finalQty = Math.min(qty, AppConstants.MAX_QUANTITY_PER_ITEM);
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_CART_QUANTITY, finalQty);

        String whereClause = DbConstants.COLUMN_CART_USER_ID + " = ? AND " + DbConstants.COLUMN_CART_PRODUCT_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId), String.valueOf(productId)};
        int rows = db.update(DbConstants.TABLE_CART_ITEMS, values, whereClause, whereArgs);
        return rows > 0;
    }

    public boolean remove(long userId, long productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DbConstants.COLUMN_CART_USER_ID + " = ? AND " + DbConstants.COLUMN_CART_PRODUCT_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId), String.valueOf(productId)};
        int rows = db.delete(DbConstants.TABLE_CART_ITEMS, whereClause, whereArgs);
        return rows > 0;
    }

    public List<CartItem> getItems(long userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT c." + DbConstants.COLUMN_CART_ID + " AS cart_id, "
                + "c." + DbConstants.COLUMN_CART_USER_ID + " AS cart_user_id, "
                + "c." + DbConstants.COLUMN_CART_PRODUCT_ID + " AS cart_product_id, "
                + "c." + DbConstants.COLUMN_CART_QUANTITY + " AS cart_quantity, "
                + "p." + DbConstants.COLUMN_PRODUCT_NAME + " AS p_name, "
                + "p." + DbConstants.COLUMN_PRODUCT_DESCRIPTION + " AS p_desc, "
                + "p." + DbConstants.COLUMN_PRODUCT_CATEGORY + " AS p_category, "
                + "p." + DbConstants.COLUMN_PRODUCT_PRICE + " AS p_price, "
                + "p." + DbConstants.COLUMN_PRODUCT_ORIGINAL_PRICE + " AS p_orig_price, "
                + "p." + DbConstants.COLUMN_PRODUCT_TAG + " AS p_tag, "
                + "p." + DbConstants.COLUMN_PRODUCT_IMAGE + " AS p_image, "
                + "p." + DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " AS p_is_active "
                + "FROM " + DbConstants.TABLE_CART_ITEMS + " c "
                + "INNER JOIN " + DbConstants.TABLE_PRODUCTS + " p ON c." + DbConstants.COLUMN_CART_PRODUCT_ID + " = p." + DbConstants.COLUMN_PRODUCT_ID + " "
                + "WHERE c." + DbConstants.COLUMN_CART_USER_ID + " = ? AND p." + DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " = 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCartItem(cursor));
                } while (cursor.moveToNext());
            }
        }
        attachImagesToCartProducts(list);
        return list;
    }

    private CartItem mapCartItem(Cursor cursor) {
        long cartId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_id"));
        long cartUserId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_user_id"));
        long productId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_product_id"));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("cart_quantity"));

        Product product = new Product();
        product.setId(productId);
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow("p_name")));
        product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("p_desc")));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("p_category")));
        product.setPrice(cursor.getLong(cursor.getColumnIndexOrThrow("p_price")));
        product.setOriginalPrice(cursor.getLong(cursor.getColumnIndexOrThrow("p_orig_price")));
        product.setTag(cursor.getString(cursor.getColumnIndexOrThrow("p_tag")));
        product.setImage(cursor.getString(cursor.getColumnIndexOrThrow("p_image")));
        product.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("p_is_active")) == 1);

        return new CartItem(cartId, cartUserId, productId, quantity, product);
    }

    private void attachImagesToCartProducts(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) return;
        ProductImageDao productImageDao = new ProductImageDao(context);
        Map<Long, List<String>> map = productImageDao.getAllImagesGroupedByProduct();
        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            if (p != null) {
                List<String> imgList = map.get(p.getId());
                if (imgList != null && !imgList.isEmpty()) {
                    p.setImages(imgList);
                }
            }
        }
    }

    public int getTotalCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + DbConstants.COLUMN_CART_QUANTITY + ") FROM " + DbConstants.TABLE_CART_ITEMS
                + " WHERE " + DbConstants.COLUMN_CART_USER_ID + " = ? AND " + DbConstants.COLUMN_CART_PRODUCT_ID
                + " IN (SELECT " + DbConstants.COLUMN_PRODUCT_ID + " FROM " + DbConstants.TABLE_PRODUCTS
                + " WHERE " + DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " = 1)";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

}
