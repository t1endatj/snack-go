package com.linh.snackgo.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.DbConstants;
import java.io.File;

/**
 * Utility for resolving asset, file, or drawable product images safely and downsampling with Glide.
 */
public class ImageUtils {

    private ImageUtils() {
        // Private constructor
    }

    public static int getDrawableResId(Context context, String imageName) {
        if (context == null || imageName == null || imageName.trim().isEmpty()) {
            return R.drawable.ic_placeholder_product;
        }
        int resId = context.getResources().getIdentifier(imageName.trim(), "drawable", context.getPackageName());
        if (resId != 0) {
            return resId;
        }
        return R.drawable.ic_placeholder_product;
    }

    public static void loadProductImage(Context context, String imageStr, ImageView imageView) {
        loadProductImage(context, imageStr, imageView, 300);
    }

    public static void loadProductImage(Context context, String imageStr, ImageView imageView, int targetSizeDp) {
        if (context == null || imageView == null) {
            return;
        }

        if (imageStr == null || imageStr.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_placeholder_product);
            return;
        }

        int sizePx = (int) (targetSizeDp * context.getResources().getDisplayMetrics().density);

        if (imageStr.startsWith(DbConstants.ASSET_PREFIX)) {
            String assetPath = "file:///android_asset/" + imageStr.substring(DbConstants.ASSET_PREFIX.length());
            Glide.with(context)
                    .load(assetPath)
                    .override(sizePx, sizePx)
                    .placeholder(R.drawable.ic_placeholder_product)
                    .error(R.drawable.ic_placeholder_product)
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(imageView);
        } else if (imageStr.startsWith("/")) {
            File imgFile = new File(imageStr);
            if (imgFile.exists()) {
                Glide.with(context)
                        .load(imgFile)
                        .override(sizePx, sizePx)
                        .placeholder(R.drawable.ic_placeholder_product)
                        .error(R.drawable.ic_placeholder_product)
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder_product);
            }
        } else {
            int resId = getDrawableResId(context, imageStr);
            if (resId != 0 && resId != R.drawable.ic_placeholder_product) {
                Glide.with(context)
                        .load(resId)
                        .override(sizePx, sizePx)
                        .placeholder(R.drawable.ic_placeholder_product)
                        .error(R.drawable.ic_placeholder_product)
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder_product);
            }
        }
    }

    public static void loadBlendImage(Context context, String imageStr, com.linh.snackgo.view.widget.BlendImageView blendImageView, int targetSizeDp, int cardBgColor) {
        if (blendImageView == null) return;
        blendImageView.setCardBackgroundColor(cardBgColor);
        blendImageView.loadImage(imageStr, targetSizeDp);
    }

    public static void loadBlendImage(Context context, String imageStr, com.linh.snackgo.view.widget.BlendImageView blendImageView, int targetSizeDp) {
        if (blendImageView == null) return;
        blendImageView.loadImage(imageStr, targetSizeDp);
    }
}
