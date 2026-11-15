package com.campusmate.data;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.campusmate.model.AppUser;
import com.campusmate.model.Item;

import java.util.List;

/** Lost and Found posts. Implemented by FirestoreItemRepository and MockItemRepository. */
public interface ItemRepository {

    /** Live list of posts (newest first). Screens observe this and redraw on every change. */
    LiveData<FeedState> feed();

    /** Starts listening for posts after login. */
    void start(AppUser user);

    /** Stops listening (logout). */
    void stop();

    /** Pull-to-refresh: listen again from scratch. */
    void refresh();

    /** Saves a new post; the callback gets its new id. */
    void add(Item item, Callback<String> callback);

    /** Saves several posts at once (sample data); the callback gets how many. */
    void addAll(List<Item> items, Callback<Integer> callback);

    void update(Item item, Callback<Void> callback);

    void delete(String itemId, Callback<Void> callback);

    /** Item.OPEN or Item.RETURNED. */
    void setStatus(String itemId, String status, Callback<Void> callback);

    /** A post from the latest list, or null. */
    @Nullable
    Item findById(String itemId);

    /** Mock mode only: remove every post. Firebase returns an error. */
    void clearAll(Callback<Void> callback);
}
