package com.campusmate.data;

import android.content.Context;

import com.campusmate.Config;
import com.google.firebase.FirebaseApp;

/**
 * Picks Firebase or mock versions of the repositories once, when the app
 * starts. Screens ask this class for them instead of creating their own, so
 * the whole app shares one copy (a simple "service locator").
 */
public final class Repositories {

    private Repositories() {
    }

    private static AuthRepository auth;
    private static ItemRepository items;
    private static ClaimRepository claims;
    private static boolean demoMode = true;

    public static synchronized void init(Context context) {
        if (auth != null) return;
        if (!Config.USE_MOCK_DATA && isFirebaseConfigured(context)) {
            auth = new FirebaseAuthRepository();
            items = new FirestoreItemRepository();
            claims = new FirestoreClaimRepository();
            demoMode = false;
        } else {
            useMock();
        }
    }

    /** Fresh, empty mock repositories. Also used by tests before each test. */
    public static synchronized void useMock() {
        auth = new MockAuthRepository();
        items = new MockItemRepository();
        claims = new MockClaimRepository();
        demoMode = true;
    }

    /** True without google-services.json: FirebaseApp then cannot start. */
    private static boolean isFirebaseConfigured(Context context) {
        try {
            return !FirebaseApp.getApps(context).isEmpty() || FirebaseApp.initializeApp(context) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static AuthRepository auth() {
        return auth;
    }

    public static ItemRepository items() {
        return items;
    }

    public static ClaimRepository claims() {
        return claims;
    }

    /** True in mock mode: data lives in memory only and the yellow DEMO MODE strip shows. */
    public static boolean isDemoMode() {
        return demoMode;
    }
}
