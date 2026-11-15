package com.campusmate.ui;

import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;

import com.campusmate.Config;
import com.campusmate.R;
import com.campusmate.data.Callback;
import com.campusmate.data.FeedState;
import com.campusmate.data.Repositories;
import com.campusmate.data.SampleData;
import com.campusmate.data.Session;
import com.campusmate.databinding.ItemMenuRowBinding;
import com.campusmate.databinding.SheetDemoMenuBinding;
import com.campusmate.logic.EventDetector;
import com.campusmate.logic.MatchService;
import com.campusmate.logic.ThemePrefs;
import com.campusmate.model.AppUser;
import com.campusmate.model.Item;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/** Hidden menu for demos and the viva (long-press the logo on Home). */
public final class DemoMenu {

    private DemoMenu() {
    }

    public static void show(FragmentActivity activity) {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        SheetDemoMenuBinding b = SheetDemoMenuBinding.inflate(activity.getLayoutInflater());
        sheet.setContentView(b.getRoot());

        if (Repositories.isDemoMode()) {
            b.accountsHint.setText(activity.getString(R.string.demo_accounts_hint,
                    Config.exampleDomain(), Config.DEMO_PASSWORD));
        } else {
            b.accountsHint.setVisibility(View.GONE);
        }

        addRow(b, R.drawable.ic_add, R.string.demo_load_samples, R.string.demo_load_samples_sub, () -> {
            sheet.dismiss();
            loadSamples(activity);
        });
        if (Repositories.isDemoMode()) {
            addRow(b, R.drawable.ic_delete, R.string.demo_reset, R.string.demo_reset_sub, () -> {
                sheet.dismiss();
                Repositories.claims().clearAll();
                Repositories.items().clearAll(snackbar(activity, activity.getString(R.string.data_reset)));
            });
        }
        addRow(b, R.drawable.ic_dark_mode, R.string.demo_toggle_theme, R.string.demo_toggle_theme_sub, () -> {
            sheet.dismiss();
            ThemePrefs.setDark(activity, !ThemePrefs.isDark(activity));
        });
        addRow(b, R.drawable.ic_notifications, R.string.demo_simulate_match, R.string.demo_simulate_match_sub, () -> {
            sheet.dismiss();
            simulateMatch(activity);
        });
        sheet.show();
    }

    private static void addRow(SheetDemoMenuBinding b, @DrawableRes int icon, @StringRes int title,
                               @StringRes int subtitle, Runnable action) {
        ItemMenuRowBinding row = ItemMenuRowBinding.inflate(android.view.LayoutInflater.from(b.getRoot().getContext()),
                b.rows, true);
        row.icon.setImageResource(icon);
        row.title.setText(title);
        row.subtitle.setText(subtitle);
        row.getRoot().setOnClickListener(v -> action.run());
    }

    private static void loadSamples(FragmentActivity activity) {
        AppUser me = Repositories.auth().currentUser();
        if (me == null) return;
        List<Item> samples = SampleData.build(me, Repositories.isDemoMode(), System.currentTimeMillis());
        Repositories.items().addAll(samples, new Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                show(activity, activity.getString(R.string.samples_loaded, count));
            }

            @Override
            public void onError(String message) {
                show(activity, message);
            }
        });
    }

    /**
     * Demo mode with one of my own posts: a sample student posts a matching
     * item, and the normal listener raises a real notification.
     * Otherwise: show a notification for an existing match pair.
     */
    private static void simulateMatch(FragmentActivity activity) {
        AppUser me = Repositories.auth().currentUser();
        FeedState feed = Repositories.items().feed().getValue();
        if (me == null || feed == null) return;

        Item myPost = null;
        for (Item item : feed.items) {
            if (item.ownerId.equals(me.uid) && !item.isReturned()) {
                myPost = item;
                break;
            }
        }

        if (Repositories.isDemoMode() && myPost != null) {
            String finder = "Kiran M";
            Item twin = new Item();
            twin.type = myPost.isLost() ? Item.FOUND : Item.LOST;
            twin.title = (twin.isLost() ? "Lost: " : "Found: ") + myPost.title;
            twin.category = myPost.category;
            twin.description = myPost.description;
            twin.location = myPost.location;
            twin.date = System.currentTimeMillis();
            twin.createdAt = System.currentTimeMillis();
            twin.ownerId = SampleData.uidOf(finder);
            twin.ownerName = finder;
            twin.ownerEmail = SampleData.emailOf(finder);
            twin.proofQuestion = twin.isLost() ? "" : "Describe one detail only the owner would know?";
            Repositories.items().add(twin, snackbar(activity, finder + " posted a matching item."));
            return;
        }

        for (Item item : feed.items) {
            List<MatchService.MatchResult> matches = MatchService.findMatches(item, feed.items);
            if (!item.isLost() || matches.isEmpty()) continue;
            Item found = matches.get(0).item;
            Session.watcher().deliver(new EventDetector.Notice(
                    "Possible match for \"" + item.title + "\"",
                    found.ownerName + " posted \"" + found.title + "\" (FOUND) at " + found.location
                            + ". Matched on: " + String.join(", ", matches.get(0).commonWords),
                    item.id, null));
            return;
        }
        show(activity, activity.getString(R.string.demo_load_samples) + " first.");
    }

    private static <T> Callback<T> snackbar(FragmentActivity activity, String success) {
        return new Callback<T>() {
            @Override
            public void onSuccess(T result) {
                show(activity, success);
            }

            @Override
            public void onError(String message) {
                show(activity, message);
            }
        };
    }

    private static void show(FragmentActivity activity, String message) {
        View root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        Snackbar snack = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        View fab = activity.findViewById(R.id.fab);
        if (fab != null) snack.setAnchorView(fab);
        snack.show();
    }
}
