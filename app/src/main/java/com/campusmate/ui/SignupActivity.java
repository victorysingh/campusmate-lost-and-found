package com.campusmate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.campusmate.data.Callback;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.ActivitySignupBinding;
import com.campusmate.logic.Validators;
import com.campusmate.model.AppUser;
import com.google.android.material.snackbar.Snackbar;

/** Create an account with name, college email and password. */
public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.showDemoBanner(b.demoBanner.getRoot());

        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.loginLink.setOnClickListener(v -> finish());
        b.signupButton.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        String name = Ui.text(b.name).trim();
        String email = Ui.text(b.email).trim();
        String password = Ui.text(b.password);
        String confirm = Ui.text(b.confirm);

        String nameError = Validators.length(name, "your name", 2, 40);
        String emailError = Validators.email(email);
        String passwordError = Validators.password(password);
        String confirmError = passwordError == null ? Validators.confirmPassword(password, confirm) : null;
        b.nameLayout.setError(nameError);
        b.emailLayout.setError(emailError);
        b.passwordLayout.setError(passwordError);
        b.confirmLayout.setError(confirmError);
        if (nameError != null || emailError != null || passwordError != null || confirmError != null) return;

        Ui.hideKeyboard(b.getRoot());
        setBusy(true);
        Repositories.auth().signUp(name, email, password, new Callback<AppUser>() {
            @Override
            public void onSuccess(AppUser user) {
                startActivity(new Intent(SignupActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }

            @Override
            public void onError(String message) {
                setBusy(false);
                Snackbar.make(b.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setBusy(boolean busy) {
        b.signupButton.setEnabled(!busy);
        b.progress.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
    }
}
