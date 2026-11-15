package com.campusmate.data;

import android.content.Context;

import com.campusmate.logic.NotificationWatcher;
import com.campusmate.model.AppUser;

/** Starts and stops everything that belongs to one logged-in student. */
public final class Session {

    private Session() {
    }

    private static String startedFor;
    private static NotificationWatcher watcher;

    /** Called by MainActivity. Starting twice for the same student does nothing. */
    public static void start(Context context, AppUser user) {
        if (user.uid.equals(startedFor)) return;
        stopListening();
        startedFor = user.uid;
        Repositories.items().start(user);
        Repositories.claims().start(user);
        watcher().start(user);
    }

    public static void logOut() {
        stopListening();
        Repositories.auth().signOut();
    }

    public static NotificationWatcher watcher() {
        return watcher;
    }

    /** Created by CampusmateApp when the app starts. */
    public static void init(Context context) {
        if (watcher == null) watcher = new NotificationWatcher(context);
    }

    /** Tests: forget the started student and the watcher (repositories are replaced too). */
    public static void reset(Context context) {
        if (watcher != null) watcher.stop();
        watcher = new NotificationWatcher(context);
        startedFor = null;
    }

    private static void stopListening() {
        if (startedFor == null) return;
        watcher.stop();
        Repositories.items().stop();
        Repositories.claims().stop();
        startedFor = null;
    }
}
