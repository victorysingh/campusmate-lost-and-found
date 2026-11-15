package com.campusmate.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Grey placeholder cards that gently pulse while posts are loading
 * (a simple "shimmer"). The animation runs only while the view is visible.
 */
public class SkeletonView extends LinearLayout {

    /** Tests switch the endless animation off. */
    public static boolean animationsEnabled = true;

    private ObjectAnimator pulse;

    public SkeletonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (isShown()) start();
        else stop();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    private void start() {
        if (pulse != null || !animationsEnabled) return;
        pulse = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0.45f);
        pulse.setDuration(700);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.start();
    }

    private void stop() {
        if (pulse == null) return;
        pulse.cancel();
        pulse = null;
        setAlpha(1f);
    }
}
