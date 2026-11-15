package com.campusmate.data;

import androidx.annotation.Nullable;

import com.campusmate.model.AppUser;

/** Login and sign up. Implemented by FirebaseAuthRepository and MockAuthRepository. */
public interface AuthRepository {

    /** The logged-in student, or null. */
    @Nullable
    AppUser currentUser();

    void signIn(String email, String password, Callback<AppUser> callback);

    void signUp(String name, String email, String password, Callback<AppUser> callback);

    void signOut();
}
