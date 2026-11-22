package com.linh.snackgo.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.databinding.ItemAdminFormImageBinding;
import com.linh.snackgo.utils.ImageUtils;
import java.util.List;

/**
 * Adapter for Admin Form multi-image thumbnails with cover selection and removal.
 */
public class AdminFormImageAdapter extends RecyclerView.Adapter<AdminFormImageAdapter.FormImageViewHolder> {

    private final List<String> imagePaths;
    private final OnFormImageActionListener listener;

    public interface OnFormImageActionListener {
        void onMakeCover(int position);
        void onRemoveImage(int position);
    }

    public AdminFormImageAdapter(List<String> imagePaths, OnFormImageActionListener listener) {
        this.imagePaths = imagePaths;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FormImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminFormImageBinding binding = ItemAdminFormImageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FormImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FormImageViewHolder holder, int position) {
        holder.bind(imagePaths.get(position), position);
    }

    @Override
    public int getItemCount() {
        return imagePaths != null ? imagePaths.size() : 0;
    }

    class FormImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminFormImageBinding binding;

        public FormImageViewHolder(@NonNull ItemAdminFormImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String path, int position) {
            ImageUtils.loadBlendImage(itemView.getContext(), path, binding.ivFormThumb, 120);

            // Cover badge on position 0
            if (position == 0) {
                binding.tvCoverBadge.setVisibility(View.VISIBLE);
                binding.btnMakeCover.setVisibility(View.GONE);
            } else {
                binding.tvCoverBadge.setVisibility(View.GONE);
                binding.btnMakeCover.setVisibility(View.VISIBLE);
            }

            // Asset images are read-only in form (cannot be removed)
            boolean isAsset = path != null && path.startsWith(DbConstants.ASSET_PREFIX);
            if (isAsset) {
                binding.btnRemoveFormImage.setVisibility(View.GONE);
            } else {
                binding.btnRemoveFormImage.setVisibility(View.VISIBLE);
            }

            binding.btnMakeCover.setOnClickListener(v -> {
                if (listener != null) listener.onMakeCover(position);
            });

            binding.btnRemoveFormImage.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveImage(position);
            });
        }
    }
}
