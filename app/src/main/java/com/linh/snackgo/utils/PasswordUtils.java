package com.linh.snackgo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for password hashing and verification using SHA-256 with random salt.
 */
public class PasswordUtils {

    private PasswordUtils() {
        // Private constructor
    }

    /**
     * Generates a 16-byte random salt as a hex string.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Hashes password with salt using SHA-256.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Verifies plain text password against salt and stored hash.
     */
    public static boolean verifyPassword(String password, String salt, String hash) {
        if (password == null || salt == null || hash == null) {
            return false;
        }
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
