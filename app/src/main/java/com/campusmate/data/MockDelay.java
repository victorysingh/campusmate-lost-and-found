package com.campusmate.data;

import android.os.Handler;
import android.os.Looper;

/**
 * Mock mode pretends to be a slow network, so loading skeletons and
 * progress spinners can be seen in the demo. Tests set {@link #millis} to 0.
 */
public final class MockDelay {

    private MockDelay() {
    }

    public static long millis = 600;

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Runs {@code work} on the main thread after the fake network delay. */
    public static void run(Runnable work) {
        MAIN.postDelayed(work, millis);
    }
}
