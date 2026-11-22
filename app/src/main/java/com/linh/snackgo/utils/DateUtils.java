package com.linh.snackgo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility for formatting date and timestamps, converting UTC SQLite timestamps to local device timezone.
 */
public class DateUtils {
    private static final String DEFAULT_PATTERN = "dd/MM/yyyy HH:mm";

    private DateUtils() {
        // Private constructor
    }

    public static String formatDateTime(String sqliteTimestamp) {
        if (sqliteTimestamp == null || sqliteTimestamp.trim().isEmpty()) {
            return "";
        }

        // Handle numeric epoch timestamps
        if (sqliteTimestamp.matches("^\\d+$")) {
            try {
                long epoch = Long.parseLong(sqliteTimestamp);
                SimpleDateFormat outputFormat = new SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault());
                outputFormat.setTimeZone(TimeZone.getDefault());
                return outputFormat.format(new Date(epoch));
            } catch (Exception ignored) {}
        }

        // Handle SQLite UTC timestamp "yyyy-MM-dd HH:mm:ss"
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(sqliteTimestamp);
            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault());
                outputFormat.setTimeZone(TimeZone.getDefault());
                return outputFormat.format(date);
            }
        } catch (Exception ignored) {
            // Fallback parsing if format differs
        }
        return sqliteTimestamp;
    }
}
