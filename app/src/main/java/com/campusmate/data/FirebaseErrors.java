package com.campusmate.data;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;

/** Friendly error messages and the offline-safe write helper for Firebase. */
final class FirebaseErrors {

    private FirebaseErrors() {
    }

    /** How long to wait for the server before treating a write as "saved offline". */
    static final long WRITE_TIMEOUT_MS = 8000;

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    static String message(Exception e) {
        if (e instanceof FirebaseNetworkException) return "No internet connection. Try again.";
        if (e instanceof FirebaseAuthException) {
            switch (((FirebaseAuthException) e).getErrorCode()) {
                case "ERROR_INVALID_EMAIL":
                    return "That email address is not valid.";
                case "ERROR_USER_NOT_FOUND":
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_INVALID_CREDENTIAL":
                    return "Wrong email or password.";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "An account with this email already exists. Log in instead.";
                case "ERROR_WEAK_PASSWORD":
                    return "Password is too weak. Use at least 6 characters.";
                case "ERROR_TOO_MANY_REQUESTS":
                    return "Too many attempts. Wait a minute and try again.";
                case "ERROR_USER_DISABLED":
                    return "This account has been disabled.";
                default:
                    return "Login failed. Please try again.";
            }
        }
        if (e instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) e).getCode()) {
                case PERMISSION_DENIED:
                    return "You are not allowed to do that.";
                case UNAVAILABLE:
                    return "Cannot reach the server. Check your internet.";
                case NOT_FOUND:
                    return "This no longer exists.";
                default:
                    break;
            }
        }
        return "Something went wrong. Please try again.";
    }

    /**
     * Firestore saves writes on the phone first and finishes the Task only
     * when the server confirms. Offline, that never happens, so the post
     * button would spin forever. After {@link #WRITE_TIMEOUT_MS} we report
     * success: the write is safe on the phone and uploads when back online.
     */
    static <T> void complete(Task<?> task, T value, Callback<T> callback) {
        final boolean[] finished = {false};
        Runnable timeout = () -> {
            if (finished[0]) return;
            finished[0] = true;
            callback.onSuccess(value);
        };
        MAIN.postDelayed(timeout, WRITE_TIMEOUT_MS);
        task.addOnCompleteListener(t -> {
            if (finished[0]) return;
            finished[0] = true;
            MAIN.removeCallbacks(timeout);
            if (t.isSuccessful()) callback.onSuccess(value);
            else callback.onError(message(t.getException()));
        });
    }
}
