package com.campusmate.ui;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Small entrance animations shared by the screens, so they all move the same way.
 * Each one finishes in under 0.7 s. If the phone has "Remove animations" switched
 * on, Android skips them and the views simply appear.
 */
public final class Motion {

    private static final Interpolator EASE_OUT = new DecelerateInterpolator(2f);

    private Motion() {
    }

    /** Fades a view in while it rises a little (16 dp). */
    public static void fadeUp(View view, long delayMs) {
        rise(view, delayMs, 16);
    }

    /** Fades a view in while it rises from further down (for sheets and bottom buttons). */
    public static void slideUp(View view, long delayMs) {
        rise(view, delayMs, 48);
    }

    private static void rise(View view, long delayMs, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        view.setAlpha(0f);
        view.setTranslationY(dp * density);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delayMs)
                .setDuration(380)
                .setInterpolator(EASE_OUT)
                .start();
    }

    /** Grows a view from 60% with a small bounce (logo, + button). */
    public static void popIn(View view, long delayMs) {
        view.setAlpha(0f);
        view.setScaleX(0.6f);
        view.setScaleY(0.6f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delayMs)
                .setDuration(420)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .start();
    }
}
