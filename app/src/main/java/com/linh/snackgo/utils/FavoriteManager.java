package com.linh.snackgo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.linh.snackgo.constants.PrefConstants;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages per-user favorite products persisted in SharedPreferences.
 */
public class FavoriteManager {
    private final SharedPreferences prefs;

    public FavoriteManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PrefConstants.PREF_NAME, Context.MODE_PRIVATE);
    }

    private String getKey(long userId) {
        return PrefConstants.KEY_FAVORITES_PREFIX + userId;
    }

    public synchronized boolean isFavorite(long userId, long productId) {
        if (userId <= 0 || productId <= 0) return false;
        Set<String> set = prefs.getStringSet(getKey(userId), null);
        return set != null && set.contains(String.valueOf(productId));
    }

    public synchronized void setFavorite(long userId, long productId, boolean isFav) {
        if (userId <= 0 || productId <= 0) return;
        Set<String> current = prefs.getStringSet(getKey(userId), null);
        Set<String> updated = current != null ? new HashSet<>(current) : new HashSet<>();
        if (isFav) {
            updated.add(String.valueOf(productId));
        } else {
            updated.remove(String.valueOf(productId));
        }
        prefs.edit().putStringSet(getKey(userId), updated).apply();
    }

    public synchronized boolean toggleFavorite(long userId, long productId) {
        boolean newState = !isFavorite(userId, productId);
        setFavorite(userId, productId, newState);
        return newState;
    }

    public synchronized Set<Long> getFavoriteIds(long userId) {
        Set<Long> result = new HashSet<>();
        if (userId <= 0) return result;
        Set<String> set = prefs.getStringSet(getKey(userId), null);
        if (set != null) {
            for (String s : set) {
                try {
                    result.add(Long.parseLong(s));
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }
}
