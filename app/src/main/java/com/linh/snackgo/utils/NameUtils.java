package com.linh.snackgo.utils;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utility for normalizing Vietnamese full names, addresses, and search text.
 */
public class NameUtils {

    private NameUtils() {
        // Private constructor
    }

    /**
     * Normalizes a full name: trims leading/trailing spaces, collapses multiple internal spaces,
     * and capitalizes the first letter of each word (Vietnamese-aware Title Case).
     */
    public static String normalizeName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }

        String trimmed = fullName.trim().replaceAll("\\s+", " ");
        String[] words = trimmed.split(" ");
        StringBuilder titleCaseName = new StringBuilder();
        Locale locale = new Locale("vi", "VN");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                String capitalized = word.substring(0, 1).toUpperCase(locale) + word.substring(1).toLowerCase(locale);
                titleCaseName.append(capitalized);
                if (i < words.length - 1) {
                    titleCaseName.append(" ");
                }
            }
        }
        return titleCaseName.toString();
    }

    /**
     * Normalizes a Vietnamese address: trims leading/trailing spaces, collapses multiple internal spaces,
     * removes trailing commas and dots, and capitalizes each word (Vietnamese-aware Title Case).
     */
    public static String normalizeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }

        String cleaned = address.trim().replaceAll("\\s+", " ").replaceAll("[,.\\s]+$", "");
        String[] words = cleaned.split(" ");
        StringBuilder titleCaseAddress = new StringBuilder();
        Locale locale = new Locale("vi", "VN");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                String capitalized = word.substring(0, 1).toUpperCase(locale) + word.substring(1).toLowerCase(locale);
                titleCaseAddress.append(capitalized);
                if (i < words.length - 1) {
                    titleCaseAddress.append(" ");
                }
            }
        }
        return titleCaseAddress.toString();
    }

    /**
     * Strips Vietnamese diacritics and converts to lowercase for accent-insensitive search.
     * e.g., "Trà đào cam sả" -> "tra dao cam sa"
     */
    public static String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        String result = normalized.replaceAll("\\p{M}", "");
        return result.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.getDefault());
    }

    /**
     * Extracts the first name (calling name / last word in Vietnamese full name) for greetings.
     */
    public static String getFirstName(String fullName) {
        String normalized = normalizeName(fullName);
        if (normalized.isEmpty()) {
            return "Bạn";
        }
        String[] words = normalized.split(" ");
        return words[words.length - 1];
    }
}
