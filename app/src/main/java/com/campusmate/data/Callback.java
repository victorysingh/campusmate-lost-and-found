package com.campusmate.data;

/**
 * Result of a slow operation (network or database). Always called on the
 * main (UI) thread, so it is safe to update views inside it.
 */
public interface Callback<T> {

    void onSuccess(T result);

    /** {@code message} is short and friendly, ready to show in a snackbar. */
    void onError(String message);
}
