package com.linh.snackgo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.linh.snackgo.R;
import com.linh.snackgo.controller.AuthController;
import com.linh.snackgo.databinding.ActivityLoginBinding;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;

/**
 * Login Activity supporting Email or Phone login.
 */
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthController authController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        authController = new AuthController(this);
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        if (!isTaskRoot()) {
            binding.btnBack.setVisibility(View.VISIBLE);
            binding.btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupListeners() {
        binding.etEmailOrPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmailOrPhone.setError(null);
            }
        });

        binding.etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
        });

        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String emailOrPhone = binding.etEmailOrPhone.getText() != null ? binding.etEmailOrPhone.getText().toString() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        binding.btnLogin.setEnabled(false);

        AuthController.AuthResult result = authController.login(emailOrPhone, password);
        if (result.isSuccess()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            binding.btnLogin.setEnabled(true);
            if (result.getErrorFieldId() == R.id.etEmailOrPhone) {
                binding.tilEmailOrPhone.setError(result.getErrorMessage());
                binding.etEmailOrPhone.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etPassword) {
                binding.tilPassword.setError(result.getErrorMessage());
                binding.etPassword.requestFocus();
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
