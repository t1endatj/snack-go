package com.linh.snackgo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.model.User;

/**
 * Data Access Object for User entities.
 */
public class UserDao {
    private final DatabaseHelper dbHelper;

    public UserDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER_FULL_NAME, user.getFullName());
        values.put(DbConstants.COLUMN_USER_EMAIL, user.getEmail());
        values.put(DbConstants.COLUMN_USER_PHONE, user.getPhone());
        values.put(DbConstants.COLUMN_USER_PASSWORD_HASH, user.getPasswordHash());
        values.put(DbConstants.COLUMN_USER_SALT, user.getSalt());
        values.put(DbConstants.COLUMN_USER_ADDRESS, user.getAddress());
        values.put(DbConstants.COLUMN_USER_AVATAR_PATH, user.getAvatarPath());
        values.put(DbConstants.COLUMN_USER_ROLE, user.getRole());

        return db.insert(DbConstants.TABLE_USERS, null, values);
    }

    public User getByEmail(String email) {
        if (email == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email.trim()};

        try (Cursor cursor = db.query(DbConstants.TABLE_USERS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToUser(cursor);
            }
        }
        return null;
    }

    public User getByPhone(String phone) {
        if (phone == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_USER_PHONE + " = ?";
        String[] selectionArgs = {phone.trim()};

        try (Cursor cursor = db.query(DbConstants.TABLE_USERS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToUser(cursor);
            }
        }
        return null;
    }

    public User getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        try (Cursor cursor = db.query(DbConstants.TABLE_USERS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToUser(cursor);
            }
        }
        return null;
    }

    public boolean update(long userId, String fullName, String phone, String address, String avatarPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER_FULL_NAME, fullName);
        values.put(DbConstants.COLUMN_USER_PHONE, phone);
        values.put(DbConstants.COLUMN_USER_ADDRESS, address);
        if (avatarPath != null) {
            values.put(DbConstants.COLUMN_USER_AVATAR_PATH, avatarPath);
        }

        String whereClause = DbConstants.COLUMN_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rows = db.update(DbConstants.TABLE_USERS, values, whereClause, whereArgs);
        return rows > 0;
    }

    public boolean updatePassword(long userId, String passwordHash, String salt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER_PASSWORD_HASH, passwordHash);
        values.put(DbConstants.COLUMN_USER_SALT, salt);

        String whereClause = DbConstants.COLUMN_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rows = db.update(DbConstants.TABLE_USERS, values, whereClause, whereArgs);
        return rows > 0;
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email.trim()};

        try (Cursor cursor = db.query(DbConstants.TABLE_USERS, new String[]{DbConstants.COLUMN_USER_ID},
                selection, selectionArgs, null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public boolean phoneExists(String phone) {
        if (phone == null) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_USER_PHONE + " = ?";
        String[] selectionArgs = {phone.trim()};

        try (Cursor cursor = db.query(DbConstants.TABLE_USERS, new String[]{DbConstants.COLUMN_USER_ID},
                selection, selectionArgs, null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public boolean phoneExistsForOtherUser(String phone, long userId) {
        if (phone == null) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.COLUMN_USER_PHONE + " = ? AND " + DbConstants.COLUMN_USER_ID + " != ?";
        String[] selectionArgs = {phone.trim(), String.valueOf(userId)};

        try (Cursor cursor = db.query(DbConstants.TABLE_USERS, new String[]{DbConstants.COLUMN_USER_ID},
                selection, selectionArgs, null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    private User mapCursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_ID)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_FULL_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_PHONE)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_PASSWORD_HASH)));
        user.setSalt(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_SALT)));
        user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_ADDRESS)));
        user.setAvatarPath(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_AVATAR_PATH)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_ROLE)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.COLUMN_USER_CREATED_AT)));
        return user;
    }
}
