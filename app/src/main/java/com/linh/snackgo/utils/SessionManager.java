package com.linh.snackgo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.PrefConstants;

public class SessionManager {
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PrefConstants.PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void createLoginSession(long userId, String role, String userName, String email) {
        editor.putBoolean(PrefConstants.KEY_IS_LOGGED_IN, true);
        editor.putLong(PrefConstants.KEY_USER_ID, userId);
        editor.putString(PrefConstants.KEY_USER_ROLE, role);
        editor.putString(PrefConstants.KEY_USER_NAME, userName);
        editor.putString(PrefConstants.KEY_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(PrefConstants.KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return preferences.getLong(PrefConstants.KEY_USER_ID, -1);
    }

    public String getUserRole() {
        return preferences.getString(PrefConstants.KEY_USER_ROLE, AppConstants.ROLE_USER);
    }

    public boolean isAdmin() {
        return AppConstants.ROLE_ADMIN.equalsIgnoreCase(getUserRole());
    }

    public String getUserName() {
        return preferences.getString(PrefConstants.KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return preferences.getString(PrefConstants.KEY_USER_EMAIL, "");
    }

    public String getUserPhone() {
        return preferences.getString(PrefConstants.KEY_USER_PHONE, "");
    }

    public String getUserAddress() {
        return preferences.getString(PrefConstants.KEY_USER_ADDRESS, "");
    }

    public void updateUserProfile(String name, String phone, String address) {
        editor.putString(PrefConstants.KEY_USER_NAME, name);
        editor.putString(PrefConstants.KEY_USER_PHONE, phone);
        editor.putString(PrefConstants.KEY_USER_ADDRESS, address);
        editor.apply();
    }

    public void logout() {
        boolean onboardingDone = isOnboardingDone();
        editor.clear();
        editor.putBoolean(PrefConstants.KEY_ONBOARDING_DONE, onboardingDone);
        editor.apply();
    }

    public void setOnboardingDone(boolean done) {
        editor.putBoolean(PrefConstants.KEY_ONBOARDING_DONE, done);
        editor.apply();
    }

    public boolean isOnboardingDone() {
        return preferences.getBoolean(PrefConstants.KEY_ONBOARDING_DONE, false);
    }
}
