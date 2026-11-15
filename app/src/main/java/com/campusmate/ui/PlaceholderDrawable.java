package com.campusmate.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Picture shown when a post has no photo: a soft background in the category's
 * colour, a faint dot pattern, and the category icon in a circle.
 * Calm in the feed, and it adapts to light and dark mode.
 */
public final class PlaceholderDrawable extends Drawable {

    private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dots = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable icon;
    private final float density;

    public PlaceholderDrawable(Context context, String category) {
        int hue = Ui.categoryColor(category);
        boolean dark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        density = context.getResources().getDisplayMetrics().density;

        if (dark) {
            background.setColor(ColorUtils.blendARGB(0xFF141C1D, hue, 0.22f));
            dots.setColor(ColorUtils.setAlphaComponent(hue, 60));
            circle.setColor(ColorUtils.blendARGB(0xFF141C1D, hue, 0.38f));
        } else {
            background.setColor(ColorUtils.blendARGB(Color.WHITE, hue, 0.12f));
            dots.setColor(ColorUtils.setAlphaComponent(hue, 34));
            circle.setColor(Color.WHITE);
        }
        int iconColor = dark ? ColorUtils.blendARGB(hue, Color.WHITE, 0.45f) : hue;

        Drawable d = ContextCompat.getDrawable(context, Ui.categoryIcon(category));
        icon = DrawableCompat.wrap(d == null ? new android.graphics.drawable.ColorDrawable() : d).mutate();
        DrawableCompat.setTint(icon, iconColor);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        canvas.drawRect(b, background);

        // Faint dot grid, 16 dp apart.
        float step = 16 * density;
        float r = 1.4f * density;
        for (float y = b.top + step / 2; y < b.bottom; y += step) {
            for (float x = b.left + step / 2; x < b.right; x += step) {
                canvas.drawCircle(x, y, r, dots);
            }
        }

        // Icon in a circle, sized to the picture (smaller on thumbnails).
        float side = Math.min(b.width(), b.height());
        float radius = Math.min(side * 0.24f, 44 * density);
        float cx = b.exactCenterX();
        float cy = b.exactCenterY();
        canvas.drawCircle(cx, cy, radius, circle);
        int half = Math.round(radius * 0.55f);
        icon.setBounds(Math.round(cx) - half, Math.round(cy) - half, Math.round(cx) + half, Math.round(cy) + half);
        icon.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        background.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        background.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        // Not OPAQUE: rounded thumbnails clip the corners and need the card drawn behind them.
        return PixelFormat.TRANSLUCENT;
    }
}
