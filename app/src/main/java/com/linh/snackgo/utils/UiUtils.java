package com.linh.snackgo.utils;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility for UI interactions like hiding soft keyboard on outside tap.
 */
public class UiUtils {

    private UiUtils() {
        // Private constructor
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) return;
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void setupHideKeyboardOnTouch(View view, Activity activity) {
        if (view == null || activity == null) return;
        if (!(view instanceof com.google.android.material.textfield.TextInputEditText)
                && !(view instanceof android.widget.EditText)
                && !(view instanceof android.widget.Button)) {
            view.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity);
                }
                return false;
            });
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View innerView = group.getChildAt(i);
                setupHideKeyboardOnTouch(innerView, activity);
            }
        }
    }
}
