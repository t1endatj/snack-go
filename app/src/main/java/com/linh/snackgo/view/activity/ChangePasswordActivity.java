package com.linh.snackgo.view.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.linh.snackgo.R;
import com.linh.snackgo.controller.UserController;
import com.linh.snackgo.databinding.ActivityChangePasswordBinding;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;

/**
 * Change Password Activity for updating user's security credentials.
 */
public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private UserController userController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        userController = new UserController(this);
        sessionManager = new SessionManager(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.etOldPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilOldPassword.setError(null);
            }
        });

        binding.etNewPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilNewPassword.setError(null);
            }
        });

        binding.etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilConfirmPassword.setError(null);
            }
        });

        binding.btnUpdatePassword.setOnClickListener(v -> performChangePassword());
    }

    private void performChangePassword() {
        String oldPassword = binding.etOldPassword.getText() != null ? binding.etOldPassword.getText().toString() : "";
        String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

        long userId = sessionManager.getUserId();
        UserController.UserResult result = userController.changePassword(userId, oldPassword, newPassword, confirmPassword);

        if (result.isSuccess()) {
            Toast.makeText(this, R.string.msg_password_change_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (result.getErrorFieldId() == R.id.etOldPassword) {
                binding.tilOldPassword.setError(result.getErrorMessage());
                binding.etOldPassword.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etNewPassword) {
                binding.tilNewPassword.setError(result.getErrorMessage());
                binding.etNewPassword.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etConfirmPassword) {
                binding.tilConfirmPassword.setError(result.getErrorMessage());
                binding.etConfirmPassword.requestFocus();
            } else {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
