package com.linh.snackgo.controller;

import android.content.Context;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.data.UserDao;
import com.linh.snackgo.model.User;
import com.linh.snackgo.utils.NameUtils;
import com.linh.snackgo.utils.PasswordUtils;
import com.linh.snackgo.utils.SessionManager;

/**
 * Controller managing User profile retrieval, editing, password change, and session logout.
 */
public class UserController {
    private final Context context;
    private final UserDao userDao;
    private final SessionManager sessionManager;

    public UserController(Context context) {
        this.context = context.getApplicationContext();
        this.userDao = new UserDao(context);
        this.sessionManager = new SessionManager(context);
    }

    public static class UserResult {
        private final boolean success;
        private final String errorMessage;
        private final int errorFieldId;

        public UserResult(boolean success, String errorMessage, int errorFieldId) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.errorFieldId = errorFieldId;
        }

        public static UserResult success() {
            return new UserResult(true, null, 0);
        }

        public static UserResult error(String message, int errorFieldId) {
            return new UserResult(false, message, errorFieldId);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getErrorFieldId() {
            return errorFieldId;
        }
    }

    public User getCurrentUser() {
        long userId = sessionManager.getUserId();
        return userDao.getById(userId);
    }

    public UserResult updateProfile(long userId, String fullName, String phone, String address, String avatarPath) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return UserResult.error(context.getString(R.string.error_empty_full_name), R.id.etFullName);
        }

        if (phone == null || phone.trim().isEmpty()) {
            return UserResult.error(context.getString(R.string.error_empty_phone), R.id.etPhone);
        }

        String cleanPhone = phone.trim();
        if (!cleanPhone.matches(AppConstants.PHONE_REGEX)) {
            return UserResult.error(context.getString(R.string.error_invalid_phone), R.id.etPhone);
        }

        if (userDao.phoneExistsForOtherUser(cleanPhone, userId)) {
            return UserResult.error(context.getString(R.string.error_phone_exists), R.id.etPhone);
        }

        String normalizedName = NameUtils.normalizeName(fullName);
        String cleanAddress = NameUtils.normalizeAddress(address);

        boolean updated = userDao.update(userId, normalizedName, cleanPhone, cleanAddress, avatarPath);
        if (updated) {
            sessionManager.updateUserProfile(normalizedName, cleanPhone, cleanAddress);
            return UserResult.success();
        } else {
            return UserResult.error(context.getString(R.string.error_order_failed), 0);
        }
    }

    public UserResult changePassword(long userId, String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            return UserResult.error(context.getString(R.string.error_empty_old_password), R.id.etOldPassword);
        }

        User user = userDao.getById(userId);
        if (user == null || !PasswordUtils.verifyPassword(oldPassword, user.getSalt(), user.getPasswordHash())) {
            return UserResult.error(context.getString(R.string.error_incorrect_old_password), R.id.etOldPassword);
        }

        if (newPassword == null || newPassword.length() < AppConstants.MIN_PASSWORD_LENGTH) {
            return UserResult.error(context.getString(R.string.error_password_length), R.id.etNewPassword);
        }

        if (newPassword.equals(oldPassword)) {
            return UserResult.error(context.getString(R.string.error_new_password_same), R.id.etNewPassword);
        }

        if (confirmPassword == null || !confirmPassword.equals(newPassword)) {
            return UserResult.error(context.getString(R.string.error_password_mismatch), R.id.etConfirmPassword);
        }

        String newSalt = PasswordUtils.generateSalt();
        String newHash = PasswordUtils.hashPassword(newPassword, newSalt);

        boolean updated = userDao.updatePassword(userId, newHash, newSalt);
        if (updated) {
            return UserResult.success();
        } else {
            return UserResult.error(context.getString(R.string.error_order_failed), 0);
        }
    }

    public void logout() {
        sessionManager.logout();
    }
}
