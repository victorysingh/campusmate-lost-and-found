package com.campusmate;

import android.app.Application;

import com.campusmate.data.Repositories;
import com.campusmate.data.Session;
import com.campusmate.logic.NotificationHelper;
import com.campusmate.logic.ThemePrefs;

/** Runs once when the app process starts, before any screen. */
public class CampusmateApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemePrefs.apply(this);
        NotificationHelper.createChannel(this);
        Repositories.init(this);
        Session.init(this);
    }
}
