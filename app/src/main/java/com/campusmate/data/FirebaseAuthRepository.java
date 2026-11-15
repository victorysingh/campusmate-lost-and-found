package com.campusmate.data;

import androidx.annotation.Nullable;

import com.campusmate.model.AppUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/** Email + password login with Firebase Authentication. */
public class FirebaseAuthRepository implements AuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public AppUser currentUser() {
        return toAppUser(auth.getCurrentUser());
    }

    @Override
    public void signIn(String email, String password, Callback<AppUser> callback) {
        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(result -> callback.onSuccess(toAppUser(result.getUser())))
                .addOnFailureListener(e -> callback.onError(FirebaseErrors.message(e)));
    }

    @Override
    public void signUp(String name, String email, String password, Callback<AppUser> callback) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError("Sign up failed. Please try again.");
                        return;
                    }
                    // Save the name on the account so it shows on every phone.
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim()).build();
                    user.updateProfile(profile).addOnCompleteListener(t ->
                            callback.onSuccess(new AppUser(user.getUid(), name.trim(), user.getEmail())));
                })
                .addOnFailureListener(e -> callback.onError(FirebaseErrors.message(e)));
    }

    @Override
    public void signOut() {
        auth.signOut();
    }

    private static AppUser toAppUser(@Nullable FirebaseUser user) {
        if (user == null) return null;
        String email = user.getEmail() == null ? "" : user.getEmail();
        String name = user.getDisplayName();
        if (name == null || name.isEmpty()) {
            name = email.contains("@") ? email.substring(0, email.indexOf('@')) : "Student";
        }
        return new AppUser(user.getUid(), name, email);
    }
}
