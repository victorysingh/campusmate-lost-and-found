package com.campusmate.data;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campusmate.Config;
import com.campusmate.model.AppUser;
import com.campusmate.model.Item;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Posts in the Firestore collection "items". A realtime listener keeps the
 * feed up to date: when anyone posts, every phone sees it within a second.
 */
public class FirestoreItemRepository implements ItemRepository {

    private final CollectionReference items = FirebaseFirestore.getInstance().collection("items");
    private final MutableLiveData<FeedState> feed = new MutableLiveData<>(FeedState.loading());
    private ListenerRegistration listener;
    private List<Item> latest = new ArrayList<>();

    @Override
    public LiveData<FeedState> feed() {
        return feed;
    }

    @Override
    public void start(AppUser user) {
        stopListening();
        feed.setValue(FeedState.loading());
        listener = items.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(Config.FEED_LIMIT)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        feed.setValue(FeedState.failed(FirebaseErrors.message(error)));
                        return;
                    }
                    if (snapshot == null) return;
                    List<Item> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        if (doc.getData() != null) list.add(Item.fromMap(doc.getId(), doc.getData()));
                    }
                    latest = list;
                    feed.setValue(FeedState.ready(list));
                });
    }

    @Override
    public void stop() {
        stopListening();
        latest = new ArrayList<>();
        feed.setValue(FeedState.loading());
    }

    @Override
    public void refresh() {
        start(null);
    }

    @Override
    public void add(Item item, Callback<String> callback) {
        DocumentReference doc = items.document(); // the id is made on the phone, so it works offline too
        FirebaseErrors.complete(doc.set(item.toMap()), doc.getId(), callback);
    }

    @Override
    public void addAll(List<Item> newItems, Callback<Integer> callback) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for (Item item : newItems) batch.set(items.document(), item.toMap());
        FirebaseErrors.complete(batch.commit(), newItems.size(), callback);
    }

    @Override
    public void update(Item item, Callback<Void> callback) {
        FirebaseErrors.complete(items.document(item.id).set(item.toMap()), null, callback);
    }

    @Override
    public void delete(String itemId, Callback<Void> callback) {
        FirebaseErrors.complete(items.document(itemId).delete(), null, callback);
    }

    @Override
    public void setStatus(String itemId, String status, Callback<Void> callback) {
        FirebaseErrors.complete(items.document(itemId).update("status", status), null, callback);
    }

    @Nullable
    @Override
    public Item findById(String itemId) {
        for (Item item : latest) {
            if (item.id.equals(itemId)) return item.copy();
        }
        return null;
    }

    @Override
    public void clearAll(Callback<Void> callback) {
        callback.onError("Reset works in demo mode only. Delete posts from My posts instead.");
    }

    private void stopListening() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
