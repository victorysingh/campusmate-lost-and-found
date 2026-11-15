package com.campusmate.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campusmate.model.AppUser;
import com.campusmate.model.ChatMessage;
import com.campusmate.model.Claim;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Claims in the collection "claims" and chat messages in
 * claims/{claimId}/messages. The security rules (firestore.rules) make
 * sure only the two people in a claim can read it or chat.
 */
public class FirestoreClaimRepository implements ClaimRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference claims = db.collection("claims");
    private final MutableLiveData<List<Claim>> myClaims = new MutableLiveData<>(null);
    private ListenerRegistration listener;

    @Override
    public LiveData<List<Claim>> claims() {
        return myClaims;
    }

    @Override
    public void start(AppUser user) {
        stop();
        // No orderBy here: array-contains + orderBy would need a composite index. We sort on the phone.
        listener = claims.whereArrayContains("participants", user.uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return; // keep the last good list
                    List<Claim> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        if (doc.getData() != null) list.add(Claim.fromMap(doc.getId(), doc.getData()));
                    }
                    list.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
                    myClaims.setValue(list);
                });
    }

    @Override
    public void stop() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
        myClaims.setValue(null);
    }

    @Override
    public void create(Claim claim, Callback<String> callback) {
        String id = Claim.idFor(claim.itemId, claim.claimantId);
        if (findById(id) != null) {
            callback.onError("You have already sent a claim for this post.");
            return;
        }
        DocumentReference doc = claims.document(id);
        Claim toSave = claim.copy();
        toSave.status = Claim.PENDING;
        FirebaseErrors.complete(doc.set(toSave.toMap()), id, callback);
    }

    @Override
    public void setStatus(String claimId, String status, Callback<Void> callback) {
        Map<String, Object> change = new HashMap<>();
        change.put("status", status);
        change.put("updatedAt", new Timestamp(new Date()));
        FirebaseErrors.complete(claims.document(claimId).update(change), null, callback);
    }

    @Override
    public LiveData<List<ChatMessage>> messages(String claimId) {
        return new MessagesLiveData(claims.document(claimId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING));
    }

    @Override
    public void sendMessage(String claimId, ChatMessage message, Callback<Void> callback) {
        DocumentReference doc = claims.document(claimId).collection("messages").document();
        FirebaseErrors.complete(doc.set(message.toMap()), null, callback);
    }

    @Nullable
    @Override
    public Claim findById(String claimId) {
        List<Claim> list = myClaims.getValue();
        if (list == null) return null;
        for (Claim c : list) {
            if (c.id.equals(claimId)) return c.copy();
        }
        return null;
    }

    @Override
    public void clearAll() {
        // Not available with Firebase (and the rules forbid deleting claims).
    }

    /**
     * LiveData that listens to Firestore only while a screen is visible
     * (onActive) and stops when it is not (onInactive), to save battery and data.
     */
    private static final class MessagesLiveData extends LiveData<List<ChatMessage>> {
        private final Query query;
        private ListenerRegistration registration;

        MessagesLiveData(@NonNull Query query) {
            super(new ArrayList<>());
            this.query = query;
        }

        @Override
        protected void onActive() {
            registration = query.addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                List<ChatMessage> list = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    if (doc.getData() != null) list.add(ChatMessage.fromMap(doc.getId(), doc.getData()));
                }
                setValue(list);
            });
        }

        @Override
        protected void onInactive() {
            if (registration != null) {
                registration.remove();
                registration = null;
            }
        }
    }
}
