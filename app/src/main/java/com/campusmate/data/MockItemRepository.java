package com.campusmate.data;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campusmate.model.AppUser;
import com.campusmate.model.Item;

import java.util.ArrayList;
import java.util.List;

/** Posts kept in memory for mock mode. Works like Firestore, without the internet. */
public class MockItemRepository implements ItemRepository {

    private final MutableLiveData<FeedState> feed = new MutableLiveData<>(FeedState.loading());
    private final List<Item> items = new ArrayList<>(); // newest first
    private boolean listening;
    private int generation; // ignores a slow "load" that finishes after stop()
    private int nextId = 1;

    @Override
    public LiveData<FeedState> feed() {
        return feed;
    }

    @Override
    public void start(AppUser user) {
        listening = true;
        int myGeneration = ++generation;
        feed.setValue(FeedState.loading());
        MockDelay.run(() -> {
            if (listening && myGeneration == generation) publish();
        });
    }

    @Override
    public void stop() {
        listening = false;
        generation++;
        feed.setValue(FeedState.loading());
    }

    @Override
    public void refresh() {
        start(null);
    }

    @Override
    public void add(Item item, Callback<String> callback) {
        MockDelay.run(() -> {
            Item saved = item.copy();
            saved.id = "demo-item-" + nextId++;
            insertNewestFirst(saved);
            publish();
            callback.onSuccess(saved.id);
        });
    }

    @Override
    public void addAll(List<Item> newItems, Callback<Integer> callback) {
        MockDelay.run(() -> {
            for (Item item : newItems) {
                Item saved = item.copy();
                saved.id = "demo-item-" + nextId++;
                insertNewestFirst(saved);
            }
            publish();
            callback.onSuccess(newItems.size());
        });
    }

    @Override
    public void update(Item item, Callback<Void> callback) {
        MockDelay.run(() -> {
            int index = indexOf(item.id);
            if (index < 0) {
                callback.onError("This post no longer exists.");
                return;
            }
            items.set(index, item.copy());
            publish();
            callback.onSuccess(null);
        });
    }

    @Override
    public void delete(String itemId, Callback<Void> callback) {
        MockDelay.run(() -> {
            int index = indexOf(itemId);
            if (index >= 0) items.remove(index);
            publish();
            callback.onSuccess(null);
        });
    }

    @Override
    public void setStatus(String itemId, String status, Callback<Void> callback) {
        MockDelay.run(() -> {
            int index = indexOf(itemId);
            if (index < 0) {
                callback.onError("This post no longer exists.");
                return;
            }
            items.get(index).status = status;
            publish();
            callback.onSuccess(null);
        });
    }

    @Nullable
    @Override
    public Item findById(String itemId) {
        int index = indexOf(itemId);
        return index < 0 ? null : items.get(index).copy();
    }

    @Override
    public void clearAll(Callback<Void> callback) {
        items.clear();
        publish();
        callback.onSuccess(null);
    }

    private void insertNewestFirst(Item item) {
        int i = 0;
        while (i < items.size() && items.get(i).createdAt > item.createdAt) i++;
        items.add(i, item);
    }

    private int indexOf(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(id)) return i;
        }
        return -1;
    }

    /** Sends copies to the screens, like a fresh Firestore snapshot. */
    private void publish() {
        if (!listening) return;
        List<Item> snapshot = new ArrayList<>();
        for (Item item : items) snapshot.add(item.copy());
        feed.setValue(FeedState.ready(snapshot));
    }
}
