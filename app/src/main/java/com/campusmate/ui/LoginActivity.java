package com.campusmate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.campusmate.Config;
import com.campusmate.R;
import com.campusmate.data.Callback;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.ActivityLoginBinding;
import com.campusmate.logic.Validators;
import com.campusmate.model.AppUser;
import com.google.android.material.snackbar.Snackbar;

/** Email + password login. Only college emails are accepted (see Config). */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.showDemoBanner(b.demoBanner.getRoot());

        if (Repositories.isDemoMode()) {
            b.demoHint.setVisibility(View.VISIBLE);
            b.demoHint.setText(getString(R.string.demo_accounts_hint, Config.exampleDomain(), Config.DEMO_PASSWORD));
        }

        b.loginButton.setOnClickListener(v -> logIn());
        b.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                logIn();
                return true;
            }
            return false;
        });
        b.signupLink.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void logIn() {
        String email = Ui.text(b.email).trim();
        String password = Ui.text(b.password);
        String emailError = Validators.email(email);
        String passwordError = Validators.password(password);
        b.emailLayout.setError(emailError);
        b.passwordLayout.setError(passwordError);
        if (emailError != null || passwordError != null) return;

        Ui.hideKeyboard(b.getRoot());
        setBusy(true);
        Repositories.auth().signIn(email, password, new Callback<AppUser>() {
            @Override
            public void onSuccess(AppUser user) {
                openMain();
            }

            @Override
            public void onError(String message) {
                setBusy(false);
                Snackbar.make(b.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void setBusy(boolean busy) {
        b.loginButton.setEnabled(!busy);
        b.signupLink.setEnabled(!busy);
        b.progress.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
    }
}
