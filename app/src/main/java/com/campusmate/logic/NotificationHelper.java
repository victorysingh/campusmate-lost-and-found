package com.campusmate.logic;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.campusmate.R;
import com.campusmate.ui.MainActivity;

/** Shows local notifications (no Firebase Cloud Messaging: that needs Cloud Functions). */
public final class NotificationHelper {

    private NotificationHelper() {
    }

    public static final String CHANNEL_ID = "campusmate_updates";

    /** Android 8+ needs a channel before showing notifications. Safe to call many times. */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.notification_channel_description));
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    /** False until the student allows notifications (Android 13+ asks at runtime). */
    public static boolean canNotify(Context context) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /** Shows {@code notice}. Tapping it opens the item or the chat. Returns false if not allowed. */
    public static boolean show(Context context, EventDetector.Notice notice) {
        if (!canNotify(context)) return false;

        Intent open = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.EXTRA_OPEN_ITEM, notice.itemId)
                .putExtra(MainActivity.EXTRA_OPEN_CLAIM, notice.claimId);
        int id = (notice.title + notice.text).hashCode();
        PendingIntent tap = PendingIntent.getActivity(context, id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_campusmate)
                .setColor(ContextCompat.getColor(context, R.color.brand_teal))
                .setContentTitle(notice.title)
                .setContentText(notice.text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notice.text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(tap);
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build());
            return true;
        } catch (SecurityException e) {
            return false; // permission was taken away while the app was running
        }
    }
}
