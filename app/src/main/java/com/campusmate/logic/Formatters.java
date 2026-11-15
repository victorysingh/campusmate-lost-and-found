package com.campusmate.logic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Turns dates into short, friendly text. */
public final class Formatters {

    private Formatters() {
    }

    /** "12 Nov 2026" */
    public static String date(long millis) {
        return new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(new Date(millis));
    }

    /** "Today", "Yesterday", "3 days ago" or "12 Nov", compared with {@code now}. */
    public static String relativeDay(long millis, long now) {
        int days = daysBetween(millis, now);
        if (days <= 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        return new SimpleDateFormat("d MMM", Locale.ENGLISH).format(new Date(millis));
    }

    /** "14:05", shown under chat bubbles. */
    public static String time(long millis) {
        return new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(millis));
    }

    private static int daysBetween(long from, long to) {
        Calendar a = startOfDay(from);
        Calendar b = startOfDay(to);
        return (int) Math.round((b.getTimeInMillis() - a.getTimeInMillis()) / 86_400_000.0);
    }

    private static Calendar startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }
}
