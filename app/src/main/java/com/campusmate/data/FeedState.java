package com.campusmate.data;

import com.campusmate.model.Item;

import java.util.Collections;
import java.util.List;

/** What the feed shows right now: loading, an error, or the list of posts. */
public final class FeedState {

    public final boolean loading;
    public final List<Item> items;   // newest first; empty while loading
    public final String error;       // null when there is no error

    private FeedState(boolean loading, List<Item> items, String error) {
        this.loading = loading;
        this.items = items;
        this.error = error;
    }

    public static FeedState loading() {
        return new FeedState(true, Collections.emptyList(), null);
    }

    public static FeedState ready(List<Item> items) {
        return new FeedState(false, Collections.unmodifiableList(items), null);
    }

    public static FeedState failed(String error) {
        return new FeedState(false, Collections.emptyList(), error);
    }
}
