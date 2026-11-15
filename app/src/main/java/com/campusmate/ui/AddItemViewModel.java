package com.campusmate.ui;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

/**
 * Keeps the post form's state when the screen is rotated or the theme
 * changes (the Activity is recreated, the ViewModel survives). The photo is
 * too big to put in a saved-state Bundle.
 */
public class AddItemViewModel extends ViewModel {

    public int step = 0;
    public String photoBase64;       // compressed photo, or null
    public Uri cameraUri;            // where the camera app writes the new photo
    public long date = System.currentTimeMillis();
    public boolean loadedForEdit;    // fields were filled from the post being edited
}
