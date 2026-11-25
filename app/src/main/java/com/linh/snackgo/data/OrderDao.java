package com.linh.snackgo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.model.Order;
import com.linh.snackgo.model.OrderItem;
import com.linh.snackgo.model.OrderSummary;
import com.linh.snackgo.model.Product;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * Data Access Object for Order and OrderItem entities.
 */
public class OrderDao {
    private final DatabaseHelper dbHelper;
    private final Context context;

    public OrderDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean createOrder(Order order, List<OrderItem> items) {
        if (order == null || items == null || items.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Generate Order Code if empty
            if (order.getOrderCode() == null || order.getOrderCode().trim().isEmpty()) {
                order.setOrderCode(generateOrderCodeInternal(db));
            }

            // 2. Insert Order
            ContentValues orderValues = new ContentValues();
            orderValues.put(DbConstants.COLUMN_ORDER_USER_ID, order.getUserId());
            orderValues.put(DbConstants.COLUMN_ORDER_CODE, order.getOrderCode());
            orderValues.put(DbConstants.COLUMN_ORDER_TOTAL_AMOUNT, order.getTotalAmount());
            orderValues.put(DbConstants.COLUMN_ORDER_DISCOUNT_AMOUNT, order.getDiscountAmount());
            orderValues.put(DbConstants.COLUMN_ORDER_DELIVERY_FEE, order.getDeliveryFee());
            orderValues.put(DbConstants.COLUMN_ORDER_PAYMENT_METHOD, order.getPaymentMethod());
            orderValues.put(DbConstants.COLUMN_ORDER_DELIVERY_ADDRESS, order.getDeliveryAddress());
            orderValues.put(DbConstants.COLUMN_ORDER_PHONE, order.getPhone());
            orderValues.put(DbConstants.COLUMN_ORDER_STATUS, order.getStatus());

            long orderId = db.insert(DbConstants.TABLE_ORDERS, null, orderValues);
            if (orderId == -1) {
                return false;
            }
            order.setId(orderId);

            // 3. Insert Order Items (Snapshot)
            for (OrderItem item : items) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(DbConstants.COLUMN_ORDER_ITEM_ORDER_ID, orderId);
                itemValues.put(DbConstants.COLUMN_ORDER_ITEM_PRODUCT_ID, item.getProductId());
                itemValues.put(DbConstants.COLUMN_ORDER_ITEM_PRODUCT_NAME, item.getProductName());
                itemValues.put(DbConstants.COLUMN_ORDER_ITEM_UNIT_PRICE, item.getUnitPrice());
                itemValues.put(DbConstants.COLUMN_ORDER_ITEM_QUANTITY, item.getQuantity());

                long itemId = db.insert(DbConstants.TABLE_ORDER_ITEMS, null, itemValues);
                if (itemId == -1) {
                    return false;
                }

                // Increment sold_count inside the transaction
                db.execSQL("UPDATE " + DbConstants.TABLE_PRODUCTS + " SET "
                        + DbConstants.COLUMN_PRODUCT_SOLD_COUNT + " = " + DbConstants.COLUMN_PRODUCT_SOLD_COUNT + " + ? "
                        + "WHERE " + DbConstants.COLUMN_PRODUCT_ID + " = ?",
                        new Object[]{item.getQuantity(), item.getProductId()});
            }

            // 4. Remove only ordered items from User's Cart
            for (OrderItem item : items) {
                db.delete(DbConstants.TABLE_CART_ITEMS,
                        DbConstants.COLUMN_CART_USER_ID + " = ? AND " + DbConstants.COLUMN_CART_PRODUCT_ID + " = ?",
                        new String[]{String.valueOf(order.getUserId()), String.valueOf(item.getProductId())});
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public List<Order> getOrdersByUser(long userId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_ORDER_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(DbConstants.TABLE_ORDERS, null, selection, selectionArgs, null, null, DbConstants.COLUMN_ORDER_ID + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = mapCursorToOrder(cursor);
                    order.setOrderItems(getItems(order.getId()));
                    list.add(order);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<OrderSummary> getOrderSummariesByUser(long userId) {
        List<OrderSummary> list = new ArrayList<>();
        List<Order> orders = getOrdersByUser(userId);

        ProductDao productDao = new ProductDao(context);

        for (Order order : orders) {
            List<OrderItem> items = order.getOrderItems();
            String firstName = "";
            String firstImage = "";
            int firstQty = 0;
            int typesCount = 0;

            if (items != null && !items.isEmpty()) {
                typesCount = items.size();
                OrderItem first = items.get(0);
                firstName = first.getProductName();
                firstQty = first.getQuantity();

                Product product = productDao.getById(first.getProductId());
                if (product != null) {
                    firstImage = product.getCoverImage();
                }
            }

            OrderSummary summary = new OrderSummary(order, firstName, firstImage, firstQty, typesCount);
            list.add(summary);
        }
        return list;
    }

    public Order getById(long orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_ORDER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(orderId)};

        try (Cursor cursor = db.query(DbConstants.TABLE_ORDERS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Order order = mapCursorToOrder(cursor);
                order.setOrderItems(getItems(orderId));
                return order;
            }
        }
        return null;
    }

    public List<OrderItem> getItems(long orderId) {
        List<OrderItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_ORDER_ITEM_ORDER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(orderId)};

        try (Cursor cursor = db.query(DbConstants.TABLE_ORDER_ITEMS, null, selection, selectionArgs, null, null, DbConstants.COLUMN_ORDER_ITEM_ID + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    OrderItem item = new OrderItem();
                    item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_ITEM_ID)));
                    item.setOrderId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_ITEM_ORDER_ID)));
                    item.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_ITEM_PRODUCT_ID)));
                    item.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_ITEM_PRODUCT_NAME)));
                    item.setUnitPrice(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_ITEM_UNIT_PRICE)));
                    item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_ITEM_QUANTITY)));
                    list.add(item);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public boolean updateStatus(long orderId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_ORDER_STATUS, newStatus);

        String whereClause = DbConstants.COLUMN_ORDER_ID + " = ?";
        String[] whereArgs = {String.valueOf(orderId)};

        int rows = db.update(DbConstants.TABLE_ORDERS, values, whereClause, whereArgs);
        return rows > 0;
    }

    public String generateOrderCode() {
        return generateOrderCodeInternal(dbHelper.getReadableDatabase());
    }

    private String generateOrderCodeInternal(SQLiteDatabase db) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String dateStr = sdf.format(new Date());

        Random random = new Random();
        String code;
        boolean exists;

        do {
            int randomNum = 1000 + random.nextInt(9000);
            code = "SG" + dateStr + randomNum;

            String selection = DbConstants.COLUMN_ORDER_CODE + " = ?";
            String[] selectionArgs = {code};
            try (Cursor cursor = db.query(DbConstants.TABLE_ORDERS, new String[]{DbConstants.COLUMN_ORDER_ID},
                    selection, selectionArgs, null, null, null)) {
                exists = (cursor != null && cursor.getCount() > 0);
            }
        } while (exists);

        return code;
    }

    private Order mapCursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_ID)));
        order.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_USER_ID)));
        order.setOrderCode(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_CODE)));
        order.setTotalAmount(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_TOTAL_AMOUNT)));
        order.setDiscountAmount(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_DISCOUNT_AMOUNT)));
        order.setDeliveryFee(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_DELIVERY_FEE)));
        order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_PAYMENT_METHOD)));
        order.setDeliveryAddress(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_DELIVERY_ADDRESS)));
        order.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_PHONE)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_STATUS)));
        order.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_ORDER_CREATED_AT)));
        return order;
    }
}
