package com.campusmate;

import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.campusmate.data.Callback;
import com.campusmate.data.MockDelay;
import com.campusmate.data.Repositories;
import com.campusmate.data.SampleData;
import com.campusmate.data.Session;
import com.campusmate.model.AppUser;
import com.campusmate.model.Item;
import com.campusmate.ui.SkeletonView;

import org.junit.Before;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shared setup for Robolectric tests: fresh mock data, no fake network
 * delay, no endless animations, light theme.
 */
public abstract class TestBase {

    protected Context context;

    @Before
    public void resetApp() {
        context = ApplicationProvider.getApplicationContext();
        MockDelay.millis = 0;
        SkeletonView.animationsEnabled = false;
        Repositories.useMock();
        Session.reset(context);
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear()
                .putBoolean("asked_notifications", true).commit();
        context.getSharedPreferences("notification_state", Context.MODE_PRIVATE).edit().clear().commit();
    }

    /** Runs everything waiting on the main thread (mock "network" callbacks). */
    protected static void idle() {
        shadowOf(Looper.getMainLooper()).idle();
    }

    /** Logs in as a demo student and returns them. */
    protected AppUser logIn(String email, String password) {
        AtomicReference<AppUser> user = new AtomicReference<>();
        Repositories.auth().signIn(email, password, callback(user));
        idle();
        return user.get();
    }

    protected AppUser signUp(String name, String email) {
        AtomicReference<AppUser> user = new AtomicReference<>();
        Repositories.auth().signUp(name, email, "secret1", callback(user));
        idle();
        return user.get();
    }

    /** Starts listening like MainActivity does and loads the 12 sample posts. */
    protected void startWithSamples(AppUser me) {
        Session.start(context, me);
        List<Item> samples = SampleData.build(me, true, System.currentTimeMillis());
        Repositories.items().addAll(samples, callback(new AtomicReference<>()));
        idle();
    }

    protected Item findByTitle(String title) {
        for (Item item : Repositories.items().feed().getValue().items) {
            if (item.title.equals(title)) return item;
        }
        throw new AssertionError("No post called " + title);
    }

    protected static <T> Callback<T> callback(AtomicReference<T> into) {
        return new Callback<T>() {
            @Override
            public void onSuccess(T result) {
                into.set(result);
            }

            @Override
            public void onError(String message) {
                throw new AssertionError(message);
            }
        };
    }

    protected static String rahul() {
        return SampleData.emailOf("Rahul Kumar");
    }
}
