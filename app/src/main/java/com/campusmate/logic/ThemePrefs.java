package com.campusmate.logic;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

/** Remembers the light / dark choice on the phone (SharedPreferences). */
public final class ThemePrefs {

    private ThemePrefs() {
    }

    private static final String FILE = "settings";
    private static final String KEY_DARK = "dark_mode";

    public static boolean isDark(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_DARK, false);
    }

    /** Saves the choice and redraws every open screen in the new theme. */
    public static void setDark(Context context, boolean dark) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_DARK, dark).apply();
        apply(context);
    }

    public static void apply(Context context) {
        AppCompatDelegate.setDefaultNightMode(isDark(context)
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
