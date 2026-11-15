package com.campusmate.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;

import com.campusmate.R;
import com.campusmate.TestBase;
import com.campusmate.data.Repositories;
import com.campusmate.data.Session;
import com.campusmate.model.AppUser;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Opens every screen on a small phone (320 x 568 dp) and fails if any visible
 * view sticks out of its parent, which is how cut-off text and buttons look.
 * Lists and scroll views are skipped: their content is meant to go past the edge.
 */
@RunWith(RobolectricTestRunner.class)
@Config(qualifiers = "w320dp-h568dp-mdpi")
public class SmallScreenTest extends TestBase {

    private final List<String> problems = new ArrayList<>();

    private static boolean scrolls(View v) {
        return v instanceof RecyclerView || v instanceof ScrollView || v instanceof NestedScrollView
                || v instanceof HorizontalScrollView || v instanceof AppBarLayout
                || v instanceof androidx.coordinatorlayout.widget.CoordinatorLayout;
    }

    private void findOverflow(Activity a, View view, String screen) {
        if (!(view instanceof ViewGroup) || view.getVisibility() != View.VISIBLE || scrolls(view)) return;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            // The FAB sits on top of the bottom bar on purpose.
            boolean onPurpose = group instanceof BottomAppBar;
            Rect r = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            boolean inside = r.left >= -1 && r.top >= -1
                    && r.right <= group.getWidth() + 1 && r.bottom <= group.getHeight() + 1;
            if (!inside && !onPurpose) {
                String name = child.getId() == View.NO_ID ? child.getClass().getSimpleName()
                        : a.getResources().getResourceEntryName(child.getId());
                problems.add(screen + ": " + name + " " + r.toShortString()
                        + " outside parent " + group.getWidth() + "x" + group.getHeight());
            }
            findOverflow(a, child, screen);
        }
    }

    private void check(Activity a, String screen) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(800));
        View root = a.getWindow().getDecorView();
        findOverflow(a, root, screen);
        // Picture for a person to look at too (not committed: it lives in build/).
        File dir = new File("build/small-screens");
        dir.mkdirs();
        Bitmap bitmap = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        stopLayoutAnimations(root);
        root.draw(new Canvas(bitmap));
        try (FileOutputStream out = new FileOutputStream(new File(dir, screen + ".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static void stopLayoutAnimations(View view) {
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        group.setLayoutAnimation(null);
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).clearAnimation();
            stopLayoutAnimations(group.getChildAt(i));
        }
    }

    @Test
    public void everyScreenFitsASmallPhone() {
        try (ActivityScenario<LoginActivity> s = ActivityScenario.launch(LoginActivity.class)) {
            s.onActivity(a -> check(a, "login"));
        }
        try (ActivityScenario<SignupActivity> s = ActivityScenario.launch(SignupActivity.class)) {
            s.onActivity(a -> check(a, "signup"));
        }

        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            idle();
            s.onActivity(a -> check(a, "home"));
            s.onActivity(a -> a.showTab(R.id.nav_my_posts));
            s.onActivity(a -> check(a, "my_posts"));
            s.onActivity(a -> a.showTab(R.id.nav_claims));
            s.onActivity(a -> check(a, "claims"));
            s.onActivity(a -> a.showTab(R.id.nav_profile));
            s.onActivity(a -> check(a, "profile"));
        }

        Item airpods = findByTitle("Apple AirPods Pro");
        try (ActivityScenario<ItemDetailActivity> s =
                     ActivityScenario.launch(ItemDetailActivity.newIntent(context, airpods.id))) {
            idle();
            s.onActivity(a -> check(a, "detail"));
        }

        try (ActivityScenario<AddItemActivity> s = ActivityScenario.launch(AddItemActivity.newIntent(context, null))) {
            s.onActivity(a -> check(a, "post_photo"));
            onView(withId(R.id.next_button)).perform(click());
            onView(withId(R.id.next_button)).perform(click()); // shows the error texts
            s.onActivity(a -> check(a, "post_details_errors"));
        }

        // An approved claim, so the chat is open.
        Item found = findByTitle("AirPods case near reading tables");
        Claim c = new Claim();
        c.itemId = found.id;
        c.itemTitle = found.title;
        c.itemType = found.type;
        c.ownerId = found.ownerId;
        c.ownerName = found.ownerName;
        c.claimantId = asha.uid;
        c.claimantName = asha.name;
        c.question = found.proofQuestion;
        c.answer = "Blue, with a small star";
        AtomicReference<String> id = new AtomicReference<>();
        Repositories.claims().create(c, callback(id));
        idle();
        Session.logOut();
        AppUser rahulUser = logIn(rahul(), com.campusmate.Config.DEMO_PASSWORD);
        Session.start(context, rahulUser);
        idle();
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            idle();
            s.onActivity(a -> a.showTab(R.id.nav_claims));
            s.onActivity(a -> check(a, "claims_pending"));
        }
        Repositories.claims().setStatus(id.get(), Claim.APPROVED, callback(new AtomicReference<>()));
        idle();
        try (ActivityScenario<ChatActivity> s = ActivityScenario.launch(ChatActivity.newIntent(context, id.get()))) {
            idle();
            s.onActivity(a -> check(a, "chat"));
        }

        assertTrue("Views cut off on a small phone:\n" + String.join("\n", problems), problems.isEmpty());
    }
}
