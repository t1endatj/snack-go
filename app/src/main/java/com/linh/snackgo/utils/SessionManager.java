package com.linh.snackgo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.PrefConstants;
import com.linh.snackgo.data.UserDao;
import com.linh.snackgo.model.User;

public class SessionManager {
    private final SharedPreferences preferences;
    private final UserDao userDao;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PrefConstants.PREF_NAME, Context.MODE_PRIVATE);
        userDao = new UserDao(context);
        preferences.edit().remove(PrefConstants.KEY_USER_NAME).remove(PrefConstants.KEY_USER_EMAIL)
                .remove(PrefConstants.KEY_USER_PHONE).remove(PrefConstants.KEY_USER_ADDRESS).apply();
    }

    public void createLoginSession(long userId, String role) {
        preferences.edit().putBoolean(PrefConstants.KEY_IS_LOGGED_IN, true)
                .putLong(PrefConstants.KEY_USER_ID, userId).putString(PrefConstants.KEY_USER_ROLE, role).apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(PrefConstants.KEY_IS_LOGGED_IN, false) && getUserId() > 0;
    }

    public long getUserId() {
        return preferences.getLong(PrefConstants.KEY_USER_ID, -1);
    }

    public boolean isCurrentUser(long userId) {
        return isLoggedIn() && getUserId() == userId;
    }

    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && AppConstants.ROLE_ADMIN.equals(user.getRole());
    }

    public User getCurrentUser() {
        return isLoggedIn() ? userDao.getById(getUserId()) : null;
    }

    public String getUserName() {
        User user = getCurrentUser();
        return user != null ? user.getFullName() : "";
    }

    public String getUserPhone() {
        User user = getCurrentUser();
        return user != null ? user.getPhone() : "";
    }

    public String getUserAddress() {
        User user = getCurrentUser();
        return user != null ? user.getAddress() : "";
    }

    public void logout() {
        preferences.edit().remove(PrefConstants.KEY_IS_LOGGED_IN)
                .remove(PrefConstants.KEY_USER_ID).remove(PrefConstants.KEY_USER_ROLE).apply();
    }

    public void setOnboardingDone(boolean done) {
        preferences.edit().putBoolean(PrefConstants.KEY_ONBOARDING_DONE, done).apply();
    }

    public boolean isOnboardingDone() {
        return preferences.getBoolean(PrefConstants.KEY_ONBOARDING_DONE, false);
    }
}
