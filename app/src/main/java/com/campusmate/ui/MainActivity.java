package com.campusmate.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.campusmate.R;
import com.campusmate.data.Repositories;
import com.campusmate.data.Session;
import com.campusmate.databinding.ActivityMainBinding;
import com.campusmate.logic.EventDetector;
import com.campusmate.logic.NotificationHelper;
import com.campusmate.model.AppUser;
import com.campusmate.model.Claim;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * The main screen after login: four tabs (Home, My posts, Claims, Profile)
 * in a bottom bar, and a centred "+" button to post an item.
 * The tabs are Fragments that are shown / hidden, so each keeps its scroll position.
 */
public class MainActivity extends AppCompatActivity {

    /** Set by a notification: which post or claim to open. */
    public static final String EXTRA_OPEN_ITEM = "open_item";
    public static final String EXTRA_OPEN_CLAIM = "open_claim";

    private static final String[] TAGS = {"home", "my_posts", "claims", "profile"};
    private static final int[] TAB_IDS = {R.id.nav_home, R.id.nav_my_posts, R.id.nav_claims, R.id.nav_profile};

    private ActivityMainBinding b;
    private AppUser user;
    private ActivityResultLauncher<String> notificationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this); // teal splash while the app starts
        super.onCreate(savedInstanceState);

        user = Repositories.auth().currentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.showDemoBanner(b.demoBanner.getRoot());
        Session.start(this, user);

        setUpTabs(savedInstanceState == null);
        b.fab.setOnClickListener(v -> startActivity(AddItemActivity.newIntent(this, null)));
        if (savedInstanceState == null) Motion.popIn(b.fab, 250);

        notificationPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        });
        askForNotificationsOnce();

        // Badge on the Claims tab = claims waiting for my decision.
        Repositories.claims().claims().observe(this, this::updateClaimsBadge);
        // Notices that could not be shown as phone notifications appear as a snackbar.
        Session.watcher().inAppNotice().observe(this, this::showInAppNotice);

        if (savedInstanceState == null) openFromNotification(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openFromNotification(intent);
    }

    /** Ask Android 13+ for notification permission the first time the main screen opens. */
    private void askForNotificationsOnce() {
        if (Build.VERSION.SDK_INT < 33 || NotificationHelper.canNotify(this)) return;
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (prefs.getBoolean("asked_notifications", false)) return;
        prefs.edit().putBoolean("asked_notifications", true).apply();
        requestNotificationPermission();
    }

    /** Used by the Profile tab too. */
    public void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void setUpTabs(boolean firstTime) {
        FragmentManager fm = getSupportFragmentManager();
        if (firstTime) {
            fm.beginTransaction()
                    .add(R.id.fragment_container, new HomeFragment(), TAGS[0])
                    .add(R.id.fragment_container, new MyPostsFragment(), TAGS[1])
                    .add(R.id.fragment_container, new ClaimsFragment(), TAGS[2])
                    .add(R.id.fragment_container, new ProfileFragment(), TAGS[3])
                    .commitNow();
            showFragmentFor(R.id.nav_home, false);
        }
        b.bottomNav.setOnItemSelectedListener(item -> {
            showFragmentFor(item.getItemId(), true);
            return true;
        });
        b.bottomNav.setOnItemReselectedListener(item -> {
        });
    }

    /** Switches to a tab, exactly as if the student tapped it in the bottom bar. */
    public void showTab(@IdRes int tabId) {
        b.bottomNav.setSelectedItemId(tabId); // calls the listener above
    }

    /** Shows one tab's fragment and hides the others. */
    private void showFragmentFor(@IdRes int tabId, boolean animate) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        if (animate) tx.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short);
        for (int i = 0; i < TAGS.length; i++) {
            Fragment f = fm.findFragmentByTag(TAGS[i]);
            if (f == null) continue;
            if (TAB_IDS[i] == tabId) tx.show(f);
            else tx.hide(f);
        }
        tx.commitNow();
    }

    private void updateClaimsBadge(@Nullable List<Claim> claims) {
        int waiting = 0;
        if (claims != null) {
            for (Claim c : claims) if (c.isOwner(user.uid) && c.isPending()) waiting++;
        }
        if (waiting == 0) {
            b.bottomNav.removeBadge(R.id.nav_claims);
        } else {
            BadgeDrawable badge = b.bottomNav.getOrCreateBadge(R.id.nav_claims);
            badge.setNumber(waiting);
        }
    }

    private void showInAppNotice(@Nullable EventDetector.Notice notice) {
        if (notice == null) return;
        Session.watcher().consumeInAppNotice();
        // Wait until the FAB has been laid out, otherwise the snackbar ignores
        // its anchor and covers the bottom bar.
        b.fab.post(() -> Snackbar.make(b.getRoot(), notice.title + "\n" + notice.text, Snackbar.LENGTH_LONG)
                .setAnchorView(b.fab)
                .setAction(R.string.view, v -> open(notice.itemId, notice.claimId))
                .show());
    }

    private void openFromNotification(@Nullable Intent intent) {
        if (intent == null) return;
        String itemId = intent.getStringExtra(EXTRA_OPEN_ITEM);
        String claimId = intent.getStringExtra(EXTRA_OPEN_CLAIM);
        intent.removeExtra(EXTRA_OPEN_ITEM);
        intent.removeExtra(EXTRA_OPEN_CLAIM);
        open(itemId, claimId);
    }

    /** Opens the chat for an approved claim, the Claims tab for other claims, or the post. */
    private void open(@Nullable String itemId, @Nullable String claimId) {
        if (claimId != null) {
            Claim claim = Repositories.claims().findById(claimId);
            if (claim != null && claim.isApproved()) {
                startActivity(ChatActivity.newIntent(this, claimId));
            } else {
                showTab(R.id.nav_claims);
            }
        } else if (itemId != null) {
            startActivity(ItemDetailActivity.newIntent(this, itemId));
        }
    }
}
