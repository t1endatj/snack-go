package com.linh.snackgo.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.linh.snackgo.databinding.ItemOnboardingPageBinding;

import java.util.List;

/**
 * RecyclerView Adapter for ViewPager2 onboarding illustration slides.
 */
public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<Integer> imageResIds;

    public OnboardingAdapter(List<Integer> imageResIds) {
        this.imageResIds = imageResIds;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOnboardingPageBinding binding = ItemOnboardingPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OnboardingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(imageResIds.get(position));
    }

    @Override
    public int getItemCount() {
        return imageResIds != null ? imageResIds.size() : 0;
    }

    public static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final ItemOnboardingPageBinding binding;

        public OnboardingViewHolder(@NonNull ItemOnboardingPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int imageResId) {
            binding.ivOnboardingImage.setImageResource(imageResId);
        }
    }
}
