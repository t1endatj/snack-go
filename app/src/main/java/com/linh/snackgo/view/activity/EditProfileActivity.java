package com.linh.snackgo.view.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.linh.snackgo.R;
import com.linh.snackgo.controller.UserController;
import com.linh.snackgo.databinding.ActivityEditProfileBinding;
import com.linh.snackgo.model.User;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;

/**
 * Edit Profile Activity for updating full name, phone number, and delivery address.
 */
public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private UserController userController;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        userController = new UserController(this);
        sessionManager = new SessionManager(this);
        currentUser = userController.getCurrentUser();

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        if (currentUser != null) {
            binding.etFullName.setText(currentUser.getFullName());
            binding.etEmail.setText(currentUser.getEmail());
            binding.etPhone.setText(currentUser.getPhone());
            binding.etAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.etFullName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }
        });

        binding.etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
            }
        });

        binding.btnSaveProfile.setOnClickListener(v -> performSaveProfile());
    }

    private void performSaveProfile() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString() : "";

        long userId = sessionManager.getUserId();
        String avatarPath = currentUser != null ? currentUser.getAvatarPath() : null;

        UserController.UserResult result = userController.updateProfile(userId, fullName, phone, address, avatarPath);

        if (result.isSuccess()) {
            Toast.makeText(this, R.string.msg_profile_update_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (result.getErrorFieldId() == R.id.etFullName) {
                binding.tilFullName.setError(result.getErrorMessage());
                binding.etFullName.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etPhone) {
                binding.tilPhone.setError(result.getErrorMessage());
                binding.etPhone.requestFocus();
            } else if (result.getErrorFieldId() == R.id.etAddress) {
                binding.tilAddress.setError(result.getErrorMessage());
                binding.etAddress.requestFocus();
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
