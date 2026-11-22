package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.linh.snackgo.R;
import com.linh.snackgo.controller.AuthController;
import com.linh.snackgo.databinding.ActivityRegisterBinding;
import com.linh.snackgo.utils.UiUtils;

/**
 * Register Activity for new user account creation.
 */
public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        authController = new AuthController(this);
        setupListeners();
        setupFocusValidation();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLoginLink.setOnClickListener(v -> finish());
        binding.btnRegister.setOnClickListener(v -> performRegister());

        binding.etFullName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }
        });

        binding.etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }
        });

        binding.etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
            }
        });

        binding.etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
        });

        binding.etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilConfirmPassword.setError(null);
            }
        });
    }

    private void setupFocusValidation() {
        binding.etFullName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    binding.tilFullName.setError(getString(R.string.error_empty_full_name));
                }
            }
        });

        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    binding.tilEmail.setError(getString(R.string.error_empty_email));
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    binding.tilEmail.setError(getString(R.string.error_invalid_email));
                }
            }
        });

        binding.etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    binding.tilPhone.setError(getString(R.string.error_empty_phone));
                } else if (!text.matches("^[0-9]{9,10}$")) {
                    binding.tilPhone.setError(getString(R.string.error_invalid_phone));
                }
            }
        });

        binding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
                if (text.length() < 6) {
                    binding.tilPassword.setError(getString(R.string.error_password_length));
                }
            }
        });

        binding.etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String pwd = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
                String confirm = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";
                if (!confirm.equals(pwd)) {
                    binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
                }
            }
        });
    }

    private void performRegister() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";
        boolean termsAccepted = binding.cbTerms.isChecked();

        binding.btnRegister.setEnabled(false);

        AuthController.AuthResult result = authController.register(fullName, email, phone, password, confirmPassword, termsAccepted);

        if (result.isSuccess()) {
            Toast.makeText(this, R.string.msg_register_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            binding.btnRegister.setEnabled(true);
            if (result.getErrorFieldId() == R.id.etFullName) {
                binding.tilFullName.setError(result.getErrorMessage());
                binding.etFullName.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etEmail) {
                binding.tilEmail.setError(result.getErrorMessage());
                binding.etEmail.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etPhone) {
                binding.tilPhone.setError(result.getErrorMessage());
                binding.etPhone.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etPassword) {
                binding.tilPassword.setError(result.getErrorMessage());
                binding.etPassword.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etConfirmPassword) {
                binding.tilConfirmPassword.setError(result.getErrorMessage());
                binding.etConfirmPassword.requestFocus();
            } else {
                Snackbar.make(binding.getRoot(), result.getErrorMessage(), Snackbar.LENGTH_LONG).show();
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
