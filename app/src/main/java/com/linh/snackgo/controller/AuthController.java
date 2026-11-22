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
 * Controller handling authentication business rules and logic.
 */
public class AuthController {
    private final Context context;
    private final UserDao userDao;
    private final SessionManager sessionManager;

    public AuthController(Context context) {
        this.context = context.getApplicationContext();
        this.userDao = new UserDao(context);
        this.sessionManager = new SessionManager(context);
    }

    public static class AuthResult {
        private final boolean success;
        private final String errorMessage;
        private final int errorFieldId;
        private final User user;

        public AuthResult(boolean success, String errorMessage, int errorFieldId, User user) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.errorFieldId = errorFieldId;
            this.user = user;
        }

        public static AuthResult success(User user) {
            return new AuthResult(true, null, 0, user);
        }

        public static AuthResult error(String message, int errorFieldId) {
            return new AuthResult(false, message, errorFieldId, null);
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

        public User getUser() {
            return user;
        }
    }

    public AuthResult login(String emailOrPhone, String password) {
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            return AuthResult.error(context.getString(R.string.error_empty_email_or_phone), R.id.etEmailOrPhone);
        }
        if (password == null || password.isEmpty()) {
            return AuthResult.error(context.getString(R.string.error_empty_password), R.id.etPassword);
        }

        String input = emailOrPhone.trim();
        User user = userDao.getByEmail(input);
        if (user == null) {
            user = userDao.getByPhone(input);
        }

        if (user == null) {
            return AuthResult.error(context.getString(R.string.error_invalid_credentials), 0);
        }

        boolean validPassword = PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!validPassword) {
            return AuthResult.error(context.getString(R.string.error_invalid_credentials), 0);
        }

        sessionManager.createLoginSession(user.getId(), user.getRole(), user.getFullName(), user.getEmail());
        return AuthResult.success(user);
    }

    public AuthResult register(String fullName, String email, String phone, String password,
                                String confirmPassword, boolean termsAccepted) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return AuthResult.error(context.getString(R.string.error_empty_full_name), R.id.etFullName);
        }

        if (email == null || email.trim().isEmpty()) {
            return AuthResult.error(context.getString(R.string.error_empty_email), R.id.etEmail);
        }

        String cleanEmail = email.trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return AuthResult.error(context.getString(R.string.error_invalid_email), R.id.etEmail);
        }

        if (userDao.emailExists(cleanEmail)) {
            return AuthResult.error(context.getString(R.string.error_email_exists), R.id.etEmail);
        }

        if (phone == null || phone.trim().isEmpty()) {
            return AuthResult.error(context.getString(R.string.error_empty_phone), R.id.etPhone);
        }

        String cleanPhone = phone.trim();
        if (!cleanPhone.matches("^[0-9]{9,10}$")) {
            return AuthResult.error(context.getString(R.string.error_invalid_phone), R.id.etPhone);
        }

        if (userDao.phoneExists(cleanPhone)) {
            return AuthResult.error(context.getString(R.string.error_phone_exists), R.id.etPhone);
        }

        if (password == null || password.length() < AppConstants.MIN_PASSWORD_LENGTH) {
            return AuthResult.error(context.getString(R.string.error_password_length), R.id.etPassword);
        }

        if (confirmPassword == null || !confirmPassword.equals(password)) {
            return AuthResult.error(context.getString(R.string.error_password_mismatch), R.id.etConfirmPassword);
        }

        if (!termsAccepted) {
            return AuthResult.error(context.getString(R.string.error_terms_required), R.id.cbTerms);
        }

        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);
        String normalizedFullName = NameUtils.normalizeName(fullName);

        User newUser = new User(normalizedFullName, cleanEmail, cleanPhone, passwordHash, salt, "", "", AppConstants.ROLE_USER);
        long userId = userDao.insert(newUser);

        if (userId == -1) {
            return AuthResult.error(context.getString(R.string.error_invalid_credentials), 0);
        }

        newUser.setId(userId);
        sessionManager.createLoginSession(userId, newUser.getRole(), newUser.getFullName(), newUser.getEmail());
        return AuthResult.success(newUser);
    }
}
