package com.campusmate.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;

import com.campusmate.Config;
import com.campusmate.R;
import com.campusmate.TestBase;
import com.campusmate.data.MockDelay;
import com.campusmate.data.Repositories;
import com.campusmate.data.Session;
import com.campusmate.logic.ImageUtils;
import com.campusmate.model.AppUser;
import com.campusmate.model.ChatMessage;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Saves a PNG of every screen at phone size (390 x 844 dp) into docs/screenshots.
 * Robolectric draws real pixels (graphicsMode NATIVE), so no phone is needed.
 * Run with: ./gradlew testDebugUnitTest -Pscreenshots --tests '*ScreenshotTest'
 */
@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(qualifiers = "w390dp-h844dp-xhdpi")
public class ScreenshotTest extends TestBase {

    private File outDir;

    @Before
    public void onlyWhenAsked() {
        String dir = System.getProperty("screenshots.dir");
        Assume.assumeTrue("run with -Pscreenshots", dir != null);
        outDir = new File(dir);
        outDir.mkdirs();
    }

    @After
    public void lightAgain() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    // ---- helpers -----------------------------------------------------------

    private static void settle() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(800)); // let animations finish
    }

    /**
     * A test draws the screen by hand, and Android's "drawing time" only moves on
     * real frames, so the lists' fall-down animation would stay at its first frame
     * (cards invisible). Switch those animations off before taking the picture.
     */
    private static void stopLayoutAnimations(View view) {
        if (!(view instanceof android.view.ViewGroup)) return;
        android.view.ViewGroup group = (android.view.ViewGroup) view;
        group.setLayoutAnimation(null);
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).clearAnimation();
            stopLayoutAnimations(group.getChildAt(i));
        }
    }

    private void save(Activity activity, String name) {
        save(activity, null, name);
    }

    /** Draws the screen (and an open dialog on top, with a dim layer) into a PNG. */
    private void save(Activity activity, Dialog dialog, String name) {
        settle();
        View root = activity.getWindow().getDecorView();
        stopLayoutAnimations(root);
        Bitmap bitmap = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        root.draw(canvas);
        if (dialog != null) {
            canvas.drawColor(Color.argb(110, 0, 0, 0));
            View d = dialog.getWindow().getDecorView();
            float x = (root.getWidth() - d.getWidth()) / 2f;
            float y = d.getHeight() >= root.getHeight() - 4 ? 0 : (root.getHeight() - d.getHeight()) / 2f;
            if (dialog instanceof com.google.android.material.bottomsheet.BottomSheetDialog) y = 0;
            canvas.save();
            canvas.translate(x, y);
            d.draw(canvas);
            canvas.restore();
        }
        try (FileOutputStream out = new FileOutputStream(new File(outDir, name + ".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private String bottlePhoto() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("sample_bottle.jpg")) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            return ImageUtils.toBase64(bitmap, Config.MAX_IMAGE_BYTES);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /** Asha with the sample posts; the found bottle gets a real photo. */
    private AppUser ashaWithSamples() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        Item bottle = findByTitle("Blue steel bottle");
        bottle.imageBase64 = bottlePhoto();
        Repositories.items().update(bottle, callback(new AtomicReference<>()));
        idle();
        return asha;
    }

    private Claim claim(AppUser claimant, Item item, String answer) {
        Claim c = new Claim();
        c.itemId = item.id;
        c.itemTitle = item.title;
        c.itemType = item.type;
        c.ownerId = item.ownerId;
        c.ownerName = item.ownerName;
        c.claimantId = claimant.uid;
        c.claimantName = claimant.name;
        c.question = item.proofQuestion;
        c.answer = answer;
        c.createdAt = System.currentTimeMillis();
        AtomicReference<String> id = new AtomicReference<>();
        Repositories.claims().create(c, callback(id));
        idle();
        c.id = id.get();
        return c;
    }

    private void clickChip(Activity a, String category) {
        ChipGroup chips = a.findViewById(R.id.category_chips);
        for (int i = 0; i < chips.getChildCount(); i++) {
            Chip chip = (Chip) chips.getChildAt(i);
            if (category.equals(chip.getTag())) chip.performClick();
        }
    }

    // ---- screens -------------------------------------------------------------

    @Test
    public void loginAndSignup() {
        try (ActivityScenario<LoginActivity> s = ActivityScenario.launch(LoginActivity.class)) {
            s.onActivity(a -> save(a, "01_login"));
        }
        try (ActivityScenario<SignupActivity> s = ActivityScenario.launch(SignupActivity.class)) {
            onView(withId(R.id.name)).perform(replaceText("Asha Rao"));
            onView(withId(R.id.email)).perform(replaceText("asha.rao@college.edu"));
            onView(withId(R.id.password)).perform(replaceText("secret1"));
            onView(withId(R.id.confirm)).perform(replaceText("secret1"), closeSoftKeyboard());
            s.onActivity(a -> save(a, "02_signup"));
        }
    }

    @Test
    public void homeLoadingEmptyAndDemoMenu() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        MockDelay.millis = 60_000; // keep "loading" on screen
        Session.start(context, asha);
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            s.onActivity(a -> save(a, "03_home_loading"));
        }
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(61));
        MockDelay.millis = 0;
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            idle();
            s.onActivity(a -> save(a, "04_home_empty"));
            s.onActivity(a -> a.findViewById(R.id.logo).performLongClick());
            s.onActivity(a -> save(a, ShadowDialog.getLatestDialog(), "05_demo_menu"));
        }
    }

    @Test
    public void feedFiltersAndDetail() {
        ashaWithSamples();
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            idle();
            s.onActivity(a -> save(a, "06_home_lost"));
            onView(withId(R.id.found_button)).perform(click());
            s.onActivity(a -> save(a, "07_home_found"));
            s.onActivity(a -> clickChip(a, "Electronics"));
            s.onActivity(a -> save(a, "08_category_filter"));
        }
        Item airpods = findByTitle("Apple AirPods Pro");
        try (ActivityScenario<ItemDetailActivity> s =
                     ActivityScenario.launch(ItemDetailActivity.newIntent(context, airpods.id))) {
            idle();
            s.onActivity(a -> save(a, "09_detail"));
            s.onActivity(a -> {
                ((com.google.android.material.appbar.AppBarLayout) a.findViewById(R.id.app_bar)).setExpanded(false, false);
                androidx.core.widget.NestedScrollView scroll = a.findViewById(R.id.scroll);
                scroll.scrollTo(0, a.findViewById(R.id.description).getTop() - 160);
            });
            s.onActivity(a -> save(a, "10_detail_matches"));
        }
    }

    @Test
    public void claimDialog() {
        ashaWithSamples();
        Item found = findByTitle("AirPods case near reading tables");
        try (ActivityScenario<ItemDetailActivity> s =
                     ActivityScenario.launch(ItemDetailActivity.newIntent(context, found.id))) {
            idle();
            onView(withId(R.id.action_button)).perform(click());
            Dialog dialog = ShadowDialog.getLatestDialog();
            ((android.widget.EditText) dialog.findViewById(R.id.answer)).setText("Blue, with a small star");
            s.onActivity(a -> save(a, dialog, "11_claim_dialog"));
        }
    }

    @Test
    public void postForm() {
        ashaWithSamples();
        try (ActivityScenario<AddItemActivity> s = ActivityScenario.launch(AddItemActivity.newIntent(context, null))) {
            s.onActivity(a -> save(a, "12_post_photo_empty"));
            s.onActivity(a -> {
                new ViewModelProvider(a).get(AddItemViewModel.class).photoBase64 = bottlePhoto();
                a.recreate();
            });
            idle();
            s.onActivity(a -> save(a, "13_post_photo_added"));
            onView(withId(R.id.next_button)).perform(click());
            onView(withId(R.id.next_button)).perform(click()); // empty -> errors
            s.onActivity(a -> save(a, "14_post_validation"));
            onView(withId(R.id.type_found)).perform(click());
            onView(withId(R.id.title)).perform(replaceText("Steel bottle"));
            onView(withId(R.id.description)).perform(replaceText("Blue steel bottle with stickers and a small dent"));
            s.onActivity(a -> ((MaterialAutoCompleteTextView) a.findViewById(R.id.category)).setText("Water Bottle", false));
            onView(withId(R.id.proof_question)).perform(replaceText("What brand is the bottle?"), closeSoftKeyboard());
            onView(withId(R.id.next_button)).perform(click());
            s.onActivity(a -> {
                ((MaterialAutoCompleteTextView) a.findViewById(R.id.place)).setText("Basketball court", false);
                ((android.widget.EditText) a.findViewById(R.id.place_detail)).setText("near the bench");
            });
            s.onActivity(a -> save(a, "16_post_location"));
        }
        // Step 2 filled in, shown on its own (the form above has moved on)
        try (ActivityScenario<AddItemActivity> s = ActivityScenario.launch(AddItemActivity.newIntent(context, null))) {
            onView(withId(R.id.next_button)).perform(click());
            onView(withId(R.id.type_found)).perform(click());
            onView(withId(R.id.title)).perform(replaceText("Steel bottle"));
            onView(withId(R.id.description)).perform(replaceText("Blue steel bottle with stickers and a small dent"));
            s.onActivity(a -> ((MaterialAutoCompleteTextView) a.findViewById(R.id.category)).setText("Water Bottle", false));
            onView(withId(R.id.proof_question)).perform(replaceText("What brand is the bottle?"), closeSoftKeyboard());
            s.onActivity(a -> save(a, "15_post_details"));
        }
    }

    @Test
    public void myPostsClaimsAndChat() {
        AppUser asha = ashaWithSamples();
        Item found = findByTitle("AirPods case near reading tables");
        Claim claim = claim(asha, found, "Blue, with a small star");
        Session.logOut();

        AppUser rahulUser = logIn(rahul(), com.campusmate.Config.DEMO_PASSWORD);
        Session.start(context, rahulUser);
        idle();
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            idle();
            s.onActivity(a -> a.showTab(R.id.nav_my_posts));
            s.onActivity(a -> save(a, "17_my_posts"));
            s.onActivity(a -> a.showTab(R.id.nav_claims));
            s.onActivity(a -> save(a, "18_claims_pending"));
            onView(withId(R.id.approve_button)).perform(click());
            idle();
            s.onActivity(a -> save(a, "19_claims_approved"));
        }
        long now = System.currentTimeMillis();
        Repositories.claims().sendMessage(claim.id, new ChatMessage(rahulUser.uid, rahulUser.name,
                "Hi Asha! The case has a blue star sticker, so it is yours.", now - 120_000), callback(new AtomicReference<>()));
        idle();
        Repositories.claims().sendMessage(claim.id, new ChatMessage(asha.uid, asha.name,
                "Yes, that's mine! Can I collect it at the library desk at 4?", now - 60_000), callback(new AtomicReference<>()));
        Repositories.claims().sendMessage(claim.id, new ChatMessage(rahulUser.uid, rahulUser.name,
                "Sure, see you there.", now), callback(new AtomicReference<>()));
        idle();
        try (ActivityScenario<ChatActivity> s = ActivityScenario.launch(ChatActivity.newIntent(context, claim.id))) {
            idle();
            s.onActivity(a -> save(a, "20_chat"));
        }
    }

    @Test
    public void matchBannerAndProfile() {
        ashaWithSamples();
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            idle();
            // Notifications are off in the test, so the notice shows as an in-app banner.
            s.onActivity(a -> a.findViewById(R.id.logo).performLongClick());
            View rows = ShadowDialog.getLatestDialog().findViewById(R.id.rows);
            java.util.ArrayList<View> found = new java.util.ArrayList<>();
            rows.findViewsWithText(found, context.getString(R.string.demo_simulate_match), View.FIND_VIEWS_WITH_TEXT);
            ((View) found.get(0).getParent().getParent()).performClick();
            idle();
            s.onActivity(a -> save(a, "21_match_banner"));
            s.onActivity(a -> a.showTab(R.id.nav_profile));
            s.onActivity(a -> save(a, "22_profile"));
        }
    }

    @Test
    public void darkMode() {
        ashaWithSamples();
        com.campusmate.logic.ThemePrefs.setDark(context, true);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        RuntimeEnvironment.setQualifiers("+night");
        try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
            idle();
            s.onActivity(a -> save(a, "23_home_dark"));
            s.onActivity(a -> a.showTab(R.id.nav_profile));
            s.onActivity(a -> save(a, "24_profile_dark"));
        }
        Item airpods = findByTitle("Blue steel bottle");
        try (ActivityScenario<ItemDetailActivity> s =
                     ActivityScenario.launch(ItemDetailActivity.newIntent(context, airpods.id))) {
            idle();
            s.onActivity(a -> save(a, "25_detail_dark"));
        }
    }
}
