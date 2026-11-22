package com.linh.snackgo.view.activity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.AdminProductController;
import com.linh.snackgo.databinding.ActivityAdminProductFormBinding;
import com.linh.snackgo.model.Product;
import com.linh.snackgo.utils.ImageUtils;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.utils.UiUtils;
import com.linh.snackgo.view.adapter.AdminFormImageAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Product Form Activity handling both adding new products and editing existing products with multi-image support.
 */
public class AdminProductFormActivity extends AppCompatActivity implements AdminFormImageAdapter.OnFormImageActionListener {

    private static final String KEY_FORM_IMAGES = "key_form_images";

    private ActivityAdminProductFormBinding binding;
    private AdminProductController controller;
    private SessionManager sessionManager;
    private Product editingProduct;
    private boolean isEditMode = false;

    private final List<String> formImages = new ArrayList<>();
    private AdminFormImageAdapter formImageAdapter;

    private ActivityResultLauncher<Intent> multiImagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, R.string.error_admin_access_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding = ActivityAdminProductFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setupHideKeyboardOnTouch(binding.getRoot(), this);

        controller = new AdminProductController(this);

        setupImagePicker();
        setupCategoryDropdown();
        setupImageRecyclerView();

        if (savedInstanceState != null) {
            ArrayList<String> savedImages = savedInstanceState.getStringArrayList(KEY_FORM_IMAGES);
            if (savedImages != null) {
                formImages.clear();
                formImages.addAll(savedImages);
            }
        }

        long productId = getIntent().getLongExtra(IntentKeys.EXTRA_PRODUCT_ID, -1);
        if (productId != -1) {
            isEditMode = true;
            editingProduct = controller.getProduct(productId);
        }

        setupUI();
        setupListeners();
    }

    private void setupImagePicker() {
        multiImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        List<Uri> newUris = new ArrayList<>();

                        if (data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                newUris.add(clipData.getItemAt(i).getUri());
                            }
                        } else if (data.getData() != null) {
                            newUris.add(data.getData());
                        }

                        for (Uri uri : newUris) {
                            if (formImages.size() >= AppConstants.MAX_PRODUCT_IMAGES) {
                                Toast.makeText(this, getString(R.string.btn_pick_image), Toast.LENGTH_SHORT).show();
                                break;
                            }
                            formImages.add(uri.toString());
                        }

                        updateImageStripUI();
                    }
                }
        );
    }

    private void setupImageRecyclerView() {
        formImageAdapter = new AdminFormImageAdapter(formImages, this);
        binding.rvFormImageThumbnails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvFormImageThumbnails.setAdapter(formImageAdapter);
    }

    private void setupCategoryDropdown() {
        String[] categories = new String[]{
                getString(R.string.category_snack_label),
                getString(R.string.category_drink_label),
                getString(R.string.category_candy_label)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.actvCategory.setAdapter(adapter);
    }

    private void setupUI() {
        if (isEditMode && editingProduct != null) {
            binding.tvFormTitle.setText(R.string.title_edit_product);
            binding.etProductName.setText(editingProduct.getName());
            binding.etPrice.setText(String.valueOf(editingProduct.getPrice()));
            if (editingProduct.getOriginalPrice() > 0) {
                binding.etOriginalPrice.setText(String.valueOf(editingProduct.getOriginalPrice()));
            }
            binding.actvCategory.setText(getCategoryLabel(editingProduct.getCategory()), false);
            binding.etDescription.setText(editingProduct.getDescription() != null ? editingProduct.getDescription() : "");
            binding.etTag.setText(editingProduct.getTag() != null ? editingProduct.getTag() : "");

            if (editingProduct.getOrigin() != null) binding.etOrigin.setText(editingProduct.getOrigin());
            if (editingProduct.getMadeIn() != null) binding.etMadeIn.setText(editingProduct.getMadeIn());
            if (editingProduct.getSizeInfo() != null) binding.etSizeInfo.setText(editingProduct.getSizeInfo());
            if (editingProduct.getShelfLife() != null) binding.etShelfLife.setText(editingProduct.getShelfLife());
            if (editingProduct.getIngredients() != null) binding.etIngredients.setText(editingProduct.getIngredients());
            if (editingProduct.getFeatures() != null) binding.etFeatures.setText(editingProduct.getFeatures());
            if (editingProduct.getUsageGuide() != null) binding.etUsageGuide.setText(editingProduct.getUsageGuide());
            if (editingProduct.getStorageGuide() != null) binding.etStorageGuide.setText(editingProduct.getStorageGuide());

            if (formImages.isEmpty()) {
                if (editingProduct.getImages() != null && !editingProduct.getImages().isEmpty()) {
                    formImages.addAll(editingProduct.getImages());
                } else if (editingProduct.getImage() != null && !editingProduct.getImage().isEmpty()) {
                    formImages.add(editingProduct.getImage());
                }
            }
        } else {
            binding.tvFormTitle.setText(R.string.title_add_product);
        }

        updateImageStripUI();
    }

    private void updateImageStripUI() {
        if (!formImages.isEmpty()) {
            binding.rvFormImageThumbnails.setVisibility(View.VISIBLE);
            binding.btnRemoveImage.setVisibility(View.VISIBLE);
            ImageUtils.loadBlendImage(this, formImages.get(0), binding.ivProductPreview, 300);
        } else {
            binding.rvFormImageThumbnails.setVisibility(View.GONE);
            binding.btnRemoveImage.setVisibility(View.GONE);
            ImageUtils.loadBlendImage(this, null, binding.ivProductPreview, 300);
        }
        if (formImageAdapter != null) {
            formImageAdapter.notifyDataSetChanged();
        }
    }

    private String getCategoryLabel(String categoryKey) {
        if (AppConstants.CATEGORY_SNACK.equalsIgnoreCase(categoryKey)) {
            return getString(R.string.category_snack_label);
        } else if (AppConstants.CATEGORY_DRINK.equalsIgnoreCase(categoryKey)) {
            return getString(R.string.category_drink_label);
        } else if (AppConstants.CATEGORY_CANDY.equalsIgnoreCase(categoryKey)) {
            return getString(R.string.category_candy_label);
        }
        return getString(R.string.category_snack_label);
    }

    private String getCategoryKey(String label) {
        if (getString(R.string.category_drink_label).equalsIgnoreCase(label)) {
            return AppConstants.CATEGORY_DRINK;
        } else if (getString(R.string.category_candy_label).equalsIgnoreCase(label)) {
            return AppConstants.CATEGORY_CANDY;
        }
        return AppConstants.CATEGORY_SNACK;
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnPickImage.setOnClickListener(v -> {
            if (formImages.size() >= AppConstants.MAX_PRODUCT_IMAGES) {
                Toast.makeText(this, getString(R.string.btn_pick_image), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            multiImagePickerLauncher.launch(Intent.createChooser(intent, getString(R.string.btn_pick_image)));
        });

        binding.btnRemoveImage.setOnClickListener(v -> {
            formImages.clear();
            updateImageStripUI();
        });

        binding.etProductName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilProductName.setError(null);
            }
        });

        binding.etPrice.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPrice.setError(null);
            }
        });

        binding.btnSaveProduct.setOnClickListener(v -> performSaveProduct());
    }

    @Override
    public void onMakeCover(int position) {
        if (position > 0 && position < formImages.size()) {
            String cover = formImages.remove(position);
            formImages.add(0, cover);
            updateImageStripUI();
        }
    }

    @Override
    public void onRemoveImage(int position) {
        if (position >= 0 && position < formImages.size()) {
            String path = formImages.get(position);
            // Read-only asset images cannot be removed in form
            if (path != null && path.startsWith(DbConstants.ASSET_PREFIX)) {
                return;
            }
            formImages.remove(position);
            updateImageStripUI();
        }
    }

    private void performSaveProduct() {
        String name = binding.etProductName.getText() != null ? binding.etProductName.getText().toString() : "";
        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString() : "";
        String origPriceStr = binding.etOriginalPrice.getText() != null ? binding.etOriginalPrice.getText().toString() : "";
        String categoryLabel = binding.actvCategory.getText() != null ? binding.actvCategory.getText().toString() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString() : "";
        String tag = binding.etTag.getText() != null ? binding.etTag.getText().toString() : "";

        String categoryKey = getCategoryKey(categoryLabel);

        AdminProductController.ValidationResult val = controller.validate(name, priceStr, origPriceStr, categoryKey, description, tag);
        if (!val.isValid()) {
            if (val.getErrorFieldId() == R.id.etProductName) {
                binding.tilProductName.setError(val.getErrorMessage());
                binding.etProductName.requestFocus();
            } else if (val.getErrorFieldId() == R.id.etPrice) {
                binding.tilPrice.setError(val.getErrorMessage());
                binding.etPrice.requestFocus();
            } else if (val.getErrorFieldId() == R.id.etOriginalPrice) {
                binding.tilOriginalPrice.setError(val.getErrorMessage());
                binding.etOriginalPrice.requestFocus();
            } else if (val.getErrorFieldId() == R.id.actvCategory) {
                binding.tilCategory.setError(val.getErrorMessage());
                binding.actvCategory.requestFocus();
            } else if (val.getErrorFieldId() == R.id.etDescription) {
                binding.tilDescription.setError(val.getErrorMessage());
                binding.etDescription.requestFocus();
            } else if (val.getErrorFieldId() == R.id.etTag) {
                binding.tilTag.setError(val.getErrorMessage());
                binding.etTag.requestFocus();
            } else {
                Toast.makeText(this, val.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        binding.btnSaveProduct.setEnabled(false);

        // Process newly picked images (content://) and convert to local file paths
        List<String> finalImagePaths = new ArrayList<>();
        for (String path : formImages) {
            if (path == null) continue;
            if (path.startsWith("content://") || path.startsWith("file://")) {
                Uri uri = Uri.parse(path);
                String savedPath = controller.saveImage(this, uri);
                if (savedPath != null) {
                    finalImagePaths.add(savedPath);
                }
            } else {
                finalImagePaths.add(path);
            }
        }

        long price = Long.parseLong(priceStr.trim());
        long origPrice = 0;
        if (origPriceStr != null && !origPriceStr.trim().isEmpty()) {
            origPrice = Long.parseLong(origPriceStr.trim());
        }

        String coverImage = !finalImagePaths.isEmpty() ? finalImagePaths.get(0) : "ic_placeholder_product";

        Product product = isEditMode && editingProduct != null ? editingProduct : new Product();
        product.setName(name);
        product.setPrice(price);
        product.setOriginalPrice(origPrice);
        product.setCategory(categoryKey);
        product.setDescription(description);
        product.setTag(tag);
        // BUG FIX: Keep original image folder key for existing products; only set for new products
        if (!isEditMode || product.getImage() == null || product.getImage().isEmpty()) {
            product.setImage(coverImage);
        }
        product.setImages(finalImagePaths);
        product.setActive(true);

        String origin = binding.etOrigin.getText() != null ? binding.etOrigin.getText().toString().trim() : "";
        String madeIn = binding.etMadeIn.getText() != null ? binding.etMadeIn.getText().toString().trim() : "";
        String sizeInfo = binding.etSizeInfo.getText() != null ? binding.etSizeInfo.getText().toString().trim() : "";
        String shelfLife = binding.etShelfLife.getText() != null ? binding.etShelfLife.getText().toString().trim() : "";
        String ingredients = binding.etIngredients.getText() != null ? binding.etIngredients.getText().toString().trim() : "";
        String features = binding.etFeatures.getText() != null ? binding.etFeatures.getText().toString().trim() : "";
        String usageGuide = binding.etUsageGuide.getText() != null ? binding.etUsageGuide.getText().toString().trim() : "";
        String storageGuide = binding.etStorageGuide.getText() != null ? binding.etStorageGuide.getText().toString().trim() : "";

        product.setOrigin(origin);
        product.setMadeIn(madeIn);
        product.setSizeInfo(sizeInfo);
        product.setShelfLife(shelfLife);
        product.setIngredients(ingredients);
        product.setFeatures(features);
        product.setUsageGuide(usageGuide);
        product.setStorageGuide(storageGuide);

        boolean success;
        if (isEditMode) {
            success = controller.updateProduct(product);
            if (success) {
                Toast.makeText(this, R.string.msg_product_updated, Toast.LENGTH_SHORT).show();
            }
        } else {
            long newId = controller.addProduct(product);
            success = newId != -1;
            if (success) {
                Toast.makeText(this, R.string.msg_product_added, Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            binding.btnSaveProduct.setEnabled(true);
            Toast.makeText(this, R.string.error_order_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_FORM_IMAGES, new ArrayList<>(formImages));
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
