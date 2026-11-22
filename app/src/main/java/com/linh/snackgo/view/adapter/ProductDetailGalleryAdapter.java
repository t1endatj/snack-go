package com.linh.snackgo.view.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.linh.snackgo.databinding.ItemGalleryPageBinding;
import com.linh.snackgo.utils.ImageUtils;
import java.util.List;

/**
 * Adapter for swipeable product image gallery in ViewPager2.
 */
public class ProductDetailGalleryAdapter extends RecyclerView.Adapter<ProductDetailGalleryAdapter.GalleryViewHolder> {

    private final List<String> images;
    private final OnImageClickListener listener;
    private final int targetSizeDp;
    private int heroBackgroundColor = Color.TRANSPARENT;
    private boolean enableBlend = true;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public ProductDetailGalleryAdapter(List<String> images, OnImageClickListener listener) {
        this(images, listener, 400, Color.TRANSPARENT, true);
    }

    public ProductDetailGalleryAdapter(List<String> images, OnImageClickListener listener, int targetSizeDp) {
        this(images, listener, targetSizeDp, Color.TRANSPARENT, true);
    }

    public ProductDetailGalleryAdapter(List<String> images, OnImageClickListener listener, int targetSizeDp, int heroBgColor, boolean enableBlend) {
        this.images = images;
        this.listener = listener;
        this.targetSizeDp = targetSizeDp;
        this.heroBackgroundColor = heroBgColor;
        this.enableBlend = enableBlend;
    }

    public void setHeroBackgroundColor(int color) {
        this.heroBackgroundColor = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGalleryPageBinding binding = ItemGalleryPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GalleryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        holder.bind(images.get(position), position);
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        private final ItemGalleryPageBinding binding;

        public GalleryViewHolder(@NonNull ItemGalleryPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String imagePath, int position) {
            binding.ivGalleryImage.setEnableBlend(enableBlend);
            ImageUtils.loadBlendImage(itemView.getContext(), imagePath, binding.ivGalleryImage, targetSizeDp, heroBackgroundColor);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(position);
                }
            });
        }
    }
}
