package com.linh.snackgo.utils;

import com.linh.snackgo.constants.AppConstants;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PriceFormatter {

    private PriceFormatter() {
        // Private constructor
    }

    public static String formatPrice(long price) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(price) + AppConstants.CURRENCY_SYMBOL;
    }

    public static String formatPrice(double price) {
        return formatPrice((long) price);
    }
}
