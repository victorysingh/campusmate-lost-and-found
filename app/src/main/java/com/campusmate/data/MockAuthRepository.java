package com.campusmate.data;

import androidx.annotation.Nullable;

import com.campusmate.Config;
import com.campusmate.model.AppUser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * In-memory accounts for mock mode. Nothing is saved: accounts disappear
 * when the app is closed. The six sample students always exist, with
 * password "demo123", so the demo can switch between finder and owner.
 */
public class MockAuthRepository implements AuthRepository {

    private static final class Account {
        final AppUser user;
        final String password;

        Account(AppUser user, String password) {
            this.user = user;
            this.password = password;
        }
    }

    private final Map<String, Account> accounts = new HashMap<>(); // key: lowercase email
    private AppUser current;
    private int nextId = 1;

    public MockAuthRepository() {
        for (String name : SampleData.STUDENTS) {
            AppUser user = new AppUser(SampleData.uidOf(name), name, SampleData.emailOf(name));
            accounts.put(user.email, new Account(user, Config.DEMO_PASSWORD));
        }
    }

    @Nullable
    @Override
    public AppUser currentUser() {
        return current;
    }

    @Override
    public void signIn(String email, String password, Callback<AppUser> callback) {
        MockDelay.run(() -> {
            Account account = accounts.get(email.trim().toLowerCase(Locale.ROOT));
            if (account == null || !account.password.equals(password)) {
                callback.onError("Wrong email or password.");
                return;
            }
            current = account.user;
            callback.onSuccess(current);
        });
    }

    @Override
    public void signUp(String name, String email, String password, Callback<AppUser> callback) {
        MockDelay.run(() -> {
            String key = email.trim().toLowerCase(Locale.ROOT);
            if (accounts.containsKey(key)) {
                callback.onError("An account with this email already exists. Log in instead.");
                return;
            }
            AppUser user = new AppUser("demo-user-" + nextId++, name.trim(), key);
            accounts.put(key, new Account(user, password));
            current = user;
            callback.onSuccess(user);
        });
    }

    @Override
    public void signOut() {
        current = null;
    }
}
