package com.linh.snackgo.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.linh.snackgo.R;
import com.linh.snackgo.controller.UserController;
import com.linh.snackgo.databinding.FragmentProfileBinding;
import com.linh.snackgo.model.User;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.view.activity.AdminProductListActivity;
import com.linh.snackgo.view.activity.ChangePasswordActivity;
import com.linh.snackgo.view.activity.EditProfileActivity;
import com.linh.snackgo.view.activity.LoginActivity;
import com.linh.snackgo.view.activity.MainActivity;
import com.linh.snackgo.view.activity.OnboardingActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Profile tab fragment displaying user profile header, photo picker avatar update, information card, menu options, and logout.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserController userController;
    private SessionManager sessionManager;
    private User currentUser;

    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userController = new UserController(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupPhotoPicker();
        setupListeners();
        loadUserData();
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            saveAvatarImage(selectedUri);
                        }
                    }
                }
        );
    }

    private void saveAvatarImage(Uri uri) {
        try {
            File avatarsDir = new File(requireContext().getFilesDir(), "avatars");
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs();
            }

            File destFile = new File(avatarsDir, "avatar_" + sessionManager.getUserId() + "_" + System.currentTimeMillis() + ".jpg");

            try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            String newPath = destFile.getAbsolutePath();
            if (currentUser != null) {
                userController.updateProfile(currentUser.getId(), currentUser.getFullName(), currentUser.getPhone(), currentUser.getAddress(), newPath);
                loadUserData();
                Toast.makeText(requireContext(), R.string.msg_avatar_updated, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), R.string.error_order_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        currentUser = userController.getCurrentUser();
        if (currentUser != null) {
            binding.tvFullName.setText(currentUser.getFullName());
            binding.tvEmail.setText(currentUser.getEmail());
            binding.tvPhone.setText(currentUser.getPhone() != null && !currentUser.getPhone().isEmpty() ? currentUser.getPhone() : getString(R.string.label_no_phone));
            binding.tvAddress.setText(currentUser.getAddress() != null && !currentUser.getAddress().isEmpty() ? currentUser.getAddress() : getString(R.string.label_no_address));

            String avatar = currentUser.getAvatarPath();
            if (avatar != null && !avatar.trim().isEmpty()) {
                File imgFile = new File(avatar);
                if (imgFile.exists()) {
                    binding.ivAvatar.setPadding(0, 0, 0, 0);
                    binding.ivAvatar.clearColorFilter();
                    Glide.with(this).load(imgFile).into(binding.ivAvatar);
                } else {
                    showDefaultAvatar();
                }
            } else {
                showDefaultAvatar();
            }

            // Admin menu row visibility
            if (sessionManager.isAdmin()) {
                binding.layoutAdminContainer.setVisibility(View.VISIBLE);
            } else {
                binding.layoutAdminContainer.setVisibility(View.GONE);
            }
        }
    }

    private void showDefaultAvatar() {
        int pad = (int) (24 * requireContext().getResources().getDisplayMetrics().density);
        binding.ivAvatar.setPadding(pad, pad, pad, pad);
        binding.ivAvatar.setImageResource(R.drawable.ic_person);
        binding.ivAvatar.setColorFilter(requireContext().getColor(R.color.avatar_tint));
    }

    private void setupListeners() {
        View.OnClickListener pickAvatarListener = v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            photoPickerLauncher.launch(Intent.createChooser(intent, getString(R.string.label_select_avatar)));
        };

        binding.ivAvatar.setOnClickListener(pickAvatarListener);
        binding.btnEditAvatar.setOnClickListener(pickAvatarListener);
        binding.ivCameraBadge.setOnClickListener(pickAvatarListener);

        binding.rowEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class))
        );

        binding.rowMyOrders.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_orders);
            }
        });

        binding.rowChangePassword.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class))
        );

        binding.rowOnboarding.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), OnboardingActivity.class))
        );

        binding.rowAdminProduct.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AdminProductListActivity.class))
        );

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.label_logout)
                .setMessage(R.string.label_confirm_logout)
                .setPositiveButton(R.string.label_confirm, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.label_cancel, null)
                .show();
    }

    private void performLogout() {
        userController.logout();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
