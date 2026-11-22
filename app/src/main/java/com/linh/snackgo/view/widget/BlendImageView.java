package com.linh.snackgo.view.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.utils.ImageUtils;
import java.io.File;

/**
 * Custom ImageView that seamlessly blends product photos with white backgrounds into card colors
 * using PorterDuff.Mode.MULTIPLY, 1.06x brightness scaling, and an under-product soft shadow ellipse.
 */
public class BlendImageView extends View {

    private static final LruCache<String, Boolean> sWhiteBackgroundCache = new LruCache<>(200);

    private int cardBackgroundColor = Color.TRANSPARENT;
    private float cornerRadius = 0f;
    private float paddingRatio = AppConstants.DEFAULT_BLEND_PADDING_RATIO;
    private boolean enableBlend = true;

    private Bitmap currentBitmap;
    private boolean isWhiteBackground = false;
    private String currentImagePath;
    private float displayAlpha = 1.0f;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blendPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final Path clipPath = new Path();
    private final RectF clipRect = new RectF();
    private final RectF dstRect = new RectF();
    private final RectF shadowRect = new RectF();

    public BlendImageView(Context context) {
        this(context, null);
    }

    public BlendImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlendImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlendImageView);
            cardBackgroundColor = a.getColor(R.styleable.BlendImageView_blendCardBackgroundColor, Color.TRANSPARENT);
            cornerRadius = a.getDimension(R.styleable.BlendImageView_blendCornerRadius, 0f);
            paddingRatio = a.getFloat(R.styleable.BlendImageView_blendPaddingRatio, AppConstants.DEFAULT_BLEND_PADDING_RATIO);
            enableBlend = a.getBoolean(R.styleable.BlendImageView_enableBlend, true);
            a.recycle();
        }

        bgPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb((int) (255 * AppConstants.SHADOW_ALPHA_RATIO), 0, 0, 0));

        // Brightness scale ~1.06 matrix so near-whites vanish into background
        ColorMatrix cm = new ColorMatrix(new float[]{
                AppConstants.BLEND_BRIGHTNESS_SCALE, 0, 0, 0, 0,
                0, AppConstants.BLEND_BRIGHTNESS_SCALE, 0, 0, 0,
                0, 0, AppConstants.BLEND_BRIGHTNESS_SCALE, 0, 0,
                0, 0, 0, 1, 0
        });
        blendPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        blendPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    public void setCardBackgroundColor(int color) {
        this.cardBackgroundColor = color;
        invalidate();
    }

    public void setHeroBackgroundColor(int color) {
        setCardBackgroundColor(color);
    }

    public void setCornerRadius(float radiusPx) {
        this.cornerRadius = radiusPx;
        invalidate();
    }

    public void setPaddingRatio(float ratio) {
        this.paddingRatio = ratio;
        invalidate();
    }

    public void setEnableBlend(boolean enable) {
        this.enableBlend = enable;
        invalidate();
    }

    public void setBitmap(Bitmap bitmap, boolean isWhite, boolean animate) {
        this.currentBitmap = bitmap;
        this.isWhiteBackground = isWhite;
        if (animate && bitmap != null) {
            displayAlpha = 0f;
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(150);
            anim.addUpdateListener(animation -> {
                displayAlpha = (float) animation.getAnimatedValue();
                invalidate();
            });
            anim.start();
        } else {
            displayAlpha = 1.0f;
            invalidate();
        }
    }

    public void loadImage(String imagePath, int targetSizeDp) {
        this.currentImagePath = imagePath;
        if (imagePath == null || imagePath.trim().isEmpty()) {
            loadPlaceholder(targetSizeDp);
            return;
        }

        // Cache lookup
        Boolean cachedWhite = sWhiteBackgroundCache.get(imagePath);
        if (cachedWhite != null) {
            this.isWhiteBackground = cachedWhite;
        }

        Object model;
        if (imagePath.startsWith(DbConstants.ASSET_PREFIX)) {
            model = "file:///android_asset/" + imagePath.substring(DbConstants.ASSET_PREFIX.length());
        } else if (imagePath.startsWith("/")) {
            model = new File(imagePath);
        } else {
            int resId = ImageUtils.getDrawableResId(getContext(), imagePath);
            model = resId != 0 ? resId : R.drawable.ic_placeholder_product;
        }

        int sizePx = (int) (targetSizeDp * getResources().getDisplayMetrics().density);
        final String expectedPath = imagePath;

        Glide.with(getContext())
                .asBitmap()
                .load(model)
                .override(sizePx, sizePx)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (expectedPath.equals(currentImagePath)) {
                            boolean isWhite = detectWhiteBackground(resource);
                            sWhiteBackgroundCache.put(expectedPath, isWhite);
                            setBitmap(resource, isWhite, true);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        if (expectedPath.equals(currentImagePath)) {
                            setBitmap(null, false, false);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (expectedPath.equals(currentImagePath)) {
                            loadPlaceholder(targetSizeDp);
                        }
                    }
                });
    }

    private void loadPlaceholder(int targetSizeDp) {
        int sizePx = (int) (targetSizeDp * getResources().getDisplayMetrics().density);
        Glide.with(getContext())
                .asBitmap()
                .load(R.drawable.ic_placeholder_product)
                .override(sizePx, sizePx)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        setBitmap(resource, false, false);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        setBitmap(null, false, false);
                    }
                });
    }

    public static boolean detectWhiteBackground(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return false;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w < 10 || h < 10) return false;

        // Sample 4 corners and middle of each edge
        int[][] points = new int[][]{
                {4, 4},
                {w - 5, 4},
                {4, h - 5},
                {w - 5, h - 5},
                {w / 2, 4},
                {w / 2, h - 5},
                {4, h / 2},
                {w - 5, h / 2}
        };

        for (int[] pt : points) {
            if (!isPatchWhite(bitmap, pt[0], pt[1])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPatchWhite(Bitmap bitmap, int cx, int cy) {
        int rSum = 0, gSum = 0, bSum = 0, count = 0;
        int radius = 2; // 5x5 patch
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int x = Math.max(0, Math.min(w - 1, cx + dx));
                int y = Math.max(0, Math.min(h - 1, cy + dy));
                int pixel = bitmap.getPixel(x, y);

                int alpha = Color.alpha(pixel);
                if (alpha < 200) {
                    return false; // Transparent or semi-transparent is not white background
                }
                rSum += Color.red(pixel);
                gSum += Color.green(pixel);
                bSum += Color.blue(pixel);
                count++;
            }
        }
        if (count == 0) return false;
        int avgR = rSum / count;
        int avgG = gSum / count;
        int avgB = bSum / count;
        return avgR >= AppConstants.WHITE_SAMPLE_THRESHOLD
                && avgG >= AppConstants.WHITE_SAMPLE_THRESHOLD
                && avgB >= AppConstants.WHITE_SAMPLE_THRESHOLD;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.save();

        // Rounded clip
        clipRect.set(0, 0, w, h);
        if (cornerRadius > 0) {
            clipPath.reset();
            clipPath.addRoundRect(clipRect, cornerRadius, cornerRadius, Path.Direction.CW);
            canvas.clipPath(clipPath);
        }

        // Draw background if set
        if (cardBackgroundColor != Color.TRANSPARENT) {
            bgPaint.setColor(cardBackgroundColor);
            canvas.drawRect(clipRect, bgPaint);
        }

        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            float padW = w * paddingRatio;
            float padH = h * paddingRatio;
            float availW = Math.max(1f, w - 2 * padW);
            float availH = Math.max(1f, h - 2 * padH);

            float bmW = currentBitmap.getWidth();
            float bmH = currentBitmap.getHeight();
            float scale = Math.min(availW / bmW, availH / bmH);
            float drawW = bmW * scale;
            float drawH = bmH * scale;

            float left = (w - drawW) / 2f;
            float top = (h - drawH) / 2f;
            dstRect.set(left, top, left + drawW, top + drawH);

            if (enableBlend && isWhiteBackground) {
                // PorterDuff.Mode.MULTIPLY blend layer over pastel background
                int saveCount = canvas.saveLayer(0, 0, w, h, null);

                // Draw background color first inside layer so hardware canvas blend works
                if (cardBackgroundColor != Color.TRANSPARENT) {
                    bgPaint.setColor(cardBackgroundColor);
                    canvas.drawRect(clipRect, bgPaint);
                }

                // Draw very soft shadow ellipse under the product (60% width, ~8% alpha)
                float shadowW = drawW * AppConstants.SHADOW_WIDTH_RATIO;
                float shadowH = Math.max(6f, drawH * AppConstants.SHADOW_HEIGHT_RATIO);
                float shadowLeft = (w - shadowW) / 2f;
                float shadowTop = dstRect.bottom - shadowH / 1.5f;
                shadowRect.set(shadowLeft, shadowTop, shadowLeft + shadowW, shadowTop + shadowH);
                canvas.drawOval(shadowRect, shadowPaint);

                // Draw bitmap with multiply and brightness scale
                blendPaint.setAlpha((int) (255 * displayAlpha));
                canvas.drawBitmap(currentBitmap, null, dstRect, blendPaint);

                canvas.restoreToCount(saveCount);
            } else {
                normalPaint.setAlpha((int) (255 * displayAlpha));
                canvas.drawBitmap(currentBitmap, null, dstRect, normalPaint);
            }
        }

        canvas.restore();
    }
}
