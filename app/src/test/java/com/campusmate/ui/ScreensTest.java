package com.campusmate.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;

import com.campusmate.R;
import com.campusmate.TestBase;
import com.campusmate.data.Repositories;
import com.campusmate.data.Session;
import com.campusmate.logic.ThemePrefs;
import com.campusmate.model.AppUser;
import com.campusmate.model.ChatMessage;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowNotificationManager;
import org.robolectric.shadows.ShadowPopupMenu;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Drives the real screens on the laptop (Robolectric, no phone needed):
 * taps buttons, types text and checks what the app shows and saves.
 */
@RunWith(RobolectricTestRunner.class)
@Config(qualifiers = "w390dp-h844dp-xhdpi")
public class ScreensTest extends TestBase {

    @After
    public void lightTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private static Intent startedBy(Activity activity) {
        return shadowOf(activity).getNextStartedActivity();
    }

    private static CharSequence errorOf(Activity activity, int layoutId) {
        return ((TextInputLayout) activity.findViewById(layoutId)).getError();
    }

    private Claim claimBy(AppUser claimant, Item item) {
        Claim c = new Claim();
        c.itemId = item.id;
        c.itemTitle = item.title;
        c.itemType = item.type;
        c.ownerId = item.ownerId;
        c.ownerName = item.ownerName;
        c.claimantId = claimant.uid;
        c.claimantName = claimant.name;
        c.question = item.proofQuestion;
        c.answer = "Blue sticker";
        c.createdAt = System.currentTimeMillis();
        AtomicReference<String> id = new AtomicReference<>();
        Repositories.claims().create(c, callback(id));
        idle();
        c.id = id.get();
        return c;
    }

    @Test
    public void loginShowsErrorsThenOpensMainForADemoAccount() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.login_button)).perform(click());
            scenario.onActivity(a -> {
                assertEquals("Enter your email", errorOf(a, R.id.email_layout));
                assertEquals("Password must be at least 6 characters", errorOf(a, R.id.password_layout));
            });

            onView(withId(R.id.email)).perform(replaceText(rahul()));
            onView(withId(R.id.password)).perform(replaceText(com.campusmate.Config.DEMO_PASSWORD), closeSoftKeyboard());
            onView(withId(R.id.login_button)).perform(click());
            idle();
            scenario.onActivity(a -> assertEquals(MainActivity.class.getName(),
                    startedBy(a).getComponent().getClassName()));
            assertEquals("Rahul Kumar", Repositories.auth().currentUser().name);
        }
    }

    @Test
    public void signupChecksPasswordsMatch() {
        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(SignupActivity.class)) {
            onView(withId(R.id.name)).perform(replaceText("Asha Rao"));
            onView(withId(R.id.email)).perform(replaceText("asha.rao@college.edu"));
            onView(withId(R.id.password)).perform(replaceText("secret1"));
            onView(withId(R.id.confirm)).perform(replaceText("secret2"), closeSoftKeyboard());
            onView(withId(R.id.signup_button)).perform(click());
            scenario.onActivity(a -> assertEquals("Passwords do not match", errorOf(a, R.id.confirm_layout)));

            onView(withId(R.id.confirm)).perform(replaceText("secret1"), closeSoftKeyboard());
            onView(withId(R.id.signup_button)).perform(click());
            idle();
            assertEquals("Asha Rao", Repositories.auth().currentUser().name);
        }
    }

    @Test
    public void homeShowsGreetingCountsAndFilters() {
        startWithSamples(signUp("Asha Rao", "asha.rao@college.edu"));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            idle();
            onView(withId(R.id.greeting)).check(matches(withText("Hi, Asha")));
            onView(withId(R.id.lost_button)).check(matches(withText("Lost (6)")));
            onView(withId(R.id.found_button)).check(matches(withText("Found (6)")));

            // Category chip: only wallets.
            scenario.onActivity(a -> {
                ChipGroup chips = a.findViewById(R.id.category_chips);
                for (int i = 0; i < chips.getChildCount(); i++) {
                    Chip chip = (Chip) chips.getChildAt(i);
                    if ("Wallet".equals(chip.getTag())) chip.performClick();
                }
            });
            onView(withId(R.id.lost_button)).check(matches(withText("Lost (1)")));
            onView(withId(R.id.found_button)).check(matches(withText("Found (1)")));

            // Search with nothing matching shows the empty state.
            onView(withId(R.id.search)).perform(replaceText("zzz"), closeSoftKeyboard());
            onView(withText(R.string.empty_search_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void demoMenuLoadsSampleData() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        Session.start(context, asha);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            idle();
            onView(withText(R.string.empty_lost_title)).check(matches(isDisplayed()));
            scenario.onActivity(a -> a.findViewById(R.id.logo).performLongClick());
            idle();
            View sheet = ShadowDialog.getLatestDialog().findViewById(R.id.rows);
            clickRowWithText(sheet, context.getString(R.string.demo_load_samples));
            idle();
            onView(withId(R.id.lost_button)).check(matches(withText("Lost (6)")));
        }
    }

    private static void clickRowWithText(View root, String text) {
        java.util.ArrayList<View> found = new java.util.ArrayList<>();
        root.findViewsWithText(found, text, View.FIND_VIEWS_WITH_TEXT);
        assertTrue("no row " + text, !found.isEmpty());
        ((View) found.get(0).getParent().getParent()).performClick();
    }

    @Test
    public void detailShowsMatchesWithSharedWords() {
        startWithSamples(signUp("Asha Rao", "asha.rao@college.edu"));
        Item lostAirPods = findByTitle("Apple AirPods Pro");
        Intent intent = ItemDetailActivity.newIntent(context, lostAirPods.id);
        try (ActivityScenario<ItemDetailActivity> scenario = ActivityScenario.launch(intent)) {
            idle();
            onView(withId(R.id.matches_title)).check(matches(withText("Possible matches (1)")));
            onView(withText("AirPods case near reading tables")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            onView(withId(R.id.action_button)).check(matches(withText(R.string.action_i_found_this)));
        }
    }

    @Test
    public void claimDialogAsksTheProofQuestionAndSendsTheClaim() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        Item foundAirPods = findByTitle("AirPods case near reading tables");
        try (ActivityScenario<ItemDetailActivity> scenario =
                     ActivityScenario.launch(ItemDetailActivity.newIntent(context, foundAirPods.id))) {
            idle();
            onView(withId(R.id.action_button)).check(matches(withText(R.string.action_this_is_mine))).perform(click());
            AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
            EditText answer = dialog.findViewById(R.id.answer);
            assertEquals(foundAirPods.proofQuestion,
                    ((android.widget.TextView) dialog.findViewById(R.id.question)).getText().toString());

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick(); // empty answer
            idle();
            assertEquals("Enter your answer", ((TextInputLayout) dialog.findViewById(R.id.answer_layout)).getError());

            answer.setText("Blue");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            idle();
            Claim claim = Repositories.claims().findById(Claim.idFor(foundAirPods.id, asha.uid));
            assertNotNull(claim);
            assertEquals(Claim.PENDING, claim.status);
            assertEquals("Blue", claim.answer);
            onView(withId(R.id.action_button)).check(matches(withText(R.string.action_claim_pending)));
        }
    }

    @Test
    public void ownerApprovesClaimFromTheClaimsTab() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        Claim claim = claimBy(asha, findByTitle("AirPods case near reading tables"));
        Session.logOut();

        AppUser rahulUser = logIn(rahul(), com.campusmate.Config.DEMO_PASSWORD);
        Session.start(context, rahulUser);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            idle();
            scenario.onActivity(a -> a.showTab(R.id.nav_claims));
            onView(withText("From Asha Rao")).check(matches(isDisplayed()));
            onView(withId(R.id.approve_button)).perform(click());
            idle();
            assertTrue(Repositories.claims().findById(claim.id).isApproved());
            onView(withId(R.id.chat_button)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void chatIsLockedUntilApprovedThenSendsMessages() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        Claim claim = claimBy(asha, findByTitle("AirPods case near reading tables"));

        try (ActivityScenario<ChatActivity> scenario = ActivityScenario.launch(ChatActivity.newIntent(context, claim.id))) {
            idle();
            onView(withId(R.id.locked_notice)).check(matches(isDisplayed()));
        }

        // Rahul approves (directly in the data), then Asha chats.
        Session.logOut();
        Session.start(context, logIn(rahul(), com.campusmate.Config.DEMO_PASSWORD));
        Repositories.claims().setStatus(claim.id, Claim.APPROVED, callback(new AtomicReference<>()));
        idle();
        Session.logOut();
        Session.start(context, logIn("asha.rao@college.edu", "secret1"));
        idle();

        try (ActivityScenario<ChatActivity> scenario = ActivityScenario.launch(ChatActivity.newIntent(context, claim.id))) {
            idle();
            onView(withId(R.id.input)).perform(replaceText("Can we meet at the library desk at 4?"), closeSoftKeyboard());
            onView(withId(R.id.send_button)).perform(click());
            idle();
            onView(withText("Can we meet at the library desk at 4?")).check(matches(isDisplayed()));
            ChatMessage sent = Repositories.claims().messages(claim.id).getValue().get(0);
            assertEquals(asha.uid, sent.senderId);
        }
    }

    @Test
    public void postFormValidatesEachStepAndSaves() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        Session.start(context, asha);
        idle();
        try (ActivityScenario<AddItemActivity> scenario = ActivityScenario.launch(AddItemActivity.newIntent(context, null))) {
            onView(withId(R.id.next_button)).perform(click());        // photo is optional
            onView(withId(R.id.next_button)).perform(click());        // empty details
            scenario.onActivity(a -> assertEquals("Enter a title", errorOf(a, R.id.title_layout)));

            onView(withId(R.id.type_found)).perform(click());
            onView(withId(R.id.title)).perform(replaceText("Steel bottle"));
            onView(withId(R.id.description)).perform(replaceText("Blue steel bottle with stickers"));
            onView(withId(R.id.proof_question)).perform(scrollTo(), replaceText("What brand is the bottle?"), closeSoftKeyboard());
            scenario.onActivity(a -> ((MaterialAutoCompleteTextView) a.findViewById(R.id.category)).setText("Water Bottle", false));
            onView(withId(R.id.next_button)).perform(click());

            onView(withId(R.id.next_button)).check(matches(withText(R.string.post))).perform(click()); // no place yet
            scenario.onActivity(a -> assertEquals("Choose a place", errorOf(a, R.id.place_layout)));
            scenario.onActivity(a -> ((MaterialAutoCompleteTextView) a.findViewById(R.id.place)).setText("Basketball court", false));
            onView(withId(R.id.place_detail)).perform(replaceText("near the bench"), closeSoftKeyboard());
            onView(withId(R.id.next_button)).perform(click());
            idle();

            Item saved = Repositories.items().feed().getValue().items.get(0);
            assertEquals("Steel bottle", saved.title);
            assertEquals(Item.FOUND, saved.type);
            assertEquals("Basketball court · near the bench", saved.location);
            assertEquals("What brand is the bottle?", saved.proofQuestion);
            scenario.onActivity(a -> assertEquals(ItemDetailActivity.class.getName(),
                    startedBy(a).getComponent().getClassName()));
        }
    }

    @Test
    public void myPostsMarksReturnedAndDeletesAfterConfirming() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        Session.start(context, asha);
        Item post = new Item();
        post.title = "Grey umbrella";
        post.category = "Other";
        post.location = "Library";
        post.ownerId = asha.uid;
        post.ownerName = asha.name;
        post.createdAt = System.currentTimeMillis();
        AtomicReference<String> id = new AtomicReference<>();
        Repositories.items().add(post, callback(id));
        idle();

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            idle();
            scenario.onActivity(a -> a.showTab(R.id.nav_my_posts));
            onView(withId(R.id.more)).perform(click());
            android.widget.PopupMenu menu = ShadowPopupMenu.getLatestPopupMenu();
            shadowOf(menu).getOnMenuItemClickListener().onMenuItemClick(menu.getMenu().findItem(R.id.action_toggle_returned));
            menu.dismiss();
            idle();
            assertTrue(Repositories.items().findById(id.get()).isReturned());

            onView(withId(R.id.more)).perform(click());
            menu = ShadowPopupMenu.getLatestPopupMenu();
            shadowOf(menu).getOnMenuItemClickListener().onMenuItemClick(menu.getMenu().findItem(R.id.action_delete));
            menu.dismiss();
            AlertDialog confirm = (AlertDialog) ShadowDialog.getLatestDialog();
            Button delete = confirm.getButton(AlertDialog.BUTTON_POSITIVE);
            delete.performClick();
            idle();
            assertEquals(null, Repositories.items().findById(id.get()));
            onView(withText(R.string.my_posts_empty_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void ownerGetsAPhoneNotificationForANewClaim() {
        Application app = ApplicationProvider.getApplicationContext();
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        Item foundAirPods = findByTitle("AirPods case near reading tables");
        Session.logOut();

        shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS);
        Session.start(context, logIn(rahul(), com.campusmate.Config.DEMO_PASSWORD));
        idle();
        claimBy(asha, foundAirPods); // Asha's claim arrives while Rahul's app is running

        ShadowNotificationManager notifications = shadowOf((NotificationManager) app.getSystemService(Application.NOTIFICATION_SERVICE));
        assertEquals(1, notifications.getAllNotifications().size());
        assertEquals("New claim on \"AirPods case near reading tables\"",
                notifications.getAllNotifications().get(0).extras.getString("android.title"));
    }

    @Test
    public void darkModeSwitchIsRemembered() {
        Session.start(context, signUp("Asha Rao", "asha.rao@college.edu"));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            idle();
            scenario.onActivity(a -> a.showTab(R.id.nav_profile));
            onView(withId(R.id.dark_mode)).perform(click());
            assertTrue(ThemePrefs.isDark(context));
            assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode());
        }
    }
}
