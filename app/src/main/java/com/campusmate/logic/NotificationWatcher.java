package com.campusmate.logic;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.campusmate.data.FeedState;
import com.campusmate.data.Repositories;
import com.campusmate.model.AppUser;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Listens to the live posts and claims (Firestore listeners, or mock data)
 * while the app is running, and shows a notification when:
 *  - someone claims one of my posts,
 *  - my claim is approved or rejected,
 *  - a new post matches one of my open posts.
 *
 * Claim statuses are remembered on the phone (SharedPreferences), so a
 * claim approved while the app was closed is announced on the next start.
 */
public class NotificationWatcher {

    private final Context context;
    private final SharedPreferences prefs;
    private final MutableLiveData<EventDetector.Notice> inAppNotice = new MutableLiveData<>();
    private AppUser user;
    private Set<String> seenItemIds; // null until the first list of posts arrives

    private final Observer<FeedState> feedObserver = this::onFeed;
    private final Observer<List<Claim>> claimsObserver = this::onClaims;

    public NotificationWatcher(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences("notification_state", Context.MODE_PRIVATE);
    }

    /**
     * Notices that could not be shown as phone notifications (permission
     * off). MainActivity shows them as an in-app banner instead.
     */
    public LiveData<EventDetector.Notice> inAppNotice() {
        return inAppNotice;
    }

    /** Called after the banner was shown, so it does not appear again when the screen is recreated. */
    public void consumeInAppNotice() {
        inAppNotice.setValue(null);
    }

    /** Shows a notice now: as a phone notification, or in the app if notifications are off. */
    public void deliver(EventDetector.Notice notice) {
        if (!NotificationHelper.show(context, notice)) inAppNotice.setValue(notice);
    }

    public void start(AppUser user) {
        stop();
        this.user = user;
        seenItemIds = null;
        Repositories.items().feed().observeForever(feedObserver);
        Repositories.claims().claims().observeForever(claimsObserver);
    }

    public void stop() {
        Repositories.items().feed().removeObserver(feedObserver);
        Repositories.claims().claims().removeObserver(claimsObserver);
        user = null;
    }

    private void onFeed(FeedState state) {
        if (user == null || state == null || state.loading || state.error != null) return;
        if (seenItemIds == null) {
            // First list after login: these posts are not "new".
            seenItemIds = new HashSet<>();
            for (Item item : state.items) seenItemIds.add(item.id);
            return;
        }
        List<Item> newItems = new ArrayList<>();
        for (Item item : state.items) {
            if (seenItemIds.add(item.id)) newItems.add(item);
        }
        deliver(EventDetector.forNewItems(user.uid, newItems, state.items));
    }

    private void onClaims(List<Claim> claims) {
        if (user == null || claims == null) return;
        // On a phone that has never seen these claims, lastSeen is empty: then only
        // claims still waiting for my decision are announced, never old approvals.
        String key = "claims_" + user.uid;
        Map<String, String> lastSeen = readStatuses(key);
        deliver(EventDetector.forClaims(user.uid, lastSeen, claims));

        Set<String> now = new HashSet<>();
        for (Claim c : claims) now.add(c.id + "|" + c.status);
        prefs.edit().putStringSet(key, now).apply();
    }

    private Map<String, String> readStatuses(String key) {
        Map<String, String> map = new HashMap<>();
        for (String entry : prefs.getStringSet(key, new HashSet<>())) {
            int bar = entry.lastIndexOf('|');
            if (bar > 0) map.put(entry.substring(0, bar), entry.substring(bar + 1));
        }
        return map;
    }

    private void deliver(List<EventDetector.Notice> notices) {
        for (EventDetector.Notice notice : notices) deliver(notice);
    }
}
