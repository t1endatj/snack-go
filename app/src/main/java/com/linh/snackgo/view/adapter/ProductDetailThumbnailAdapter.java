package com.linh.snackgo.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.linh.snackgo.R;
import com.linh.snackgo.databinding.ItemProductDetailThumbnailBinding;
import com.linh.snackgo.utils.ImageUtils;
import java.util.List;

/**
 * Adapter for small product thumbnails under main gallery ViewPager2.
 */
public class ProductDetailThumbnailAdapter extends RecyclerView.Adapter<ProductDetailThumbnailAdapter.ThumbnailViewHolder> {

    private final List<String> images;
    private final OnThumbnailClickListener listener;
    private int selectedPosition = 0;

    public interface OnThumbnailClickListener {
        void onThumbnailClick(int position);
    }

    public ProductDetailThumbnailAdapter(List<String> images, OnThumbnailClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < getItemCount() && selectedPosition != position) {
            int oldPos = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
        }
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductDetailThumbnailBinding binding = ItemProductDetailThumbnailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ThumbnailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        holder.bind(images.get(position), position);
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductDetailThumbnailBinding binding;

        public ThumbnailViewHolder(@NonNull ItemProductDetailThumbnailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String imagePath, int position) {
            ImageUtils.loadBlendImage(itemView.getContext(), imagePath, binding.ivThumbnail, 120);

            if (position == selectedPosition) {
                binding.cardThumbnail.setStrokeWidth((int) (2 * itemView.getContext().getResources().getDisplayMetrics().density));
                binding.cardThumbnail.setStrokeColor(itemView.getContext().getColor(R.color.accent_orange));
            } else {
                binding.cardThumbnail.setStrokeWidth(0);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    setSelectedPosition(position);
                    listener.onThumbnailClick(position);
                }
            });
        }
    }
}
