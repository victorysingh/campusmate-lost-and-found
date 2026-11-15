package com.campusmate.data;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.campusmate.model.AppUser;
import com.campusmate.model.ChatMessage;
import com.campusmate.model.Claim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Claims and chats kept in memory for mock mode. All demo accounts share
 * this memory, so you can claim as one student, log out, and approve as
 * another. It applies the same checks as the Firestore security rules.
 */
public class MockClaimRepository implements ClaimRepository {

    private final MutableLiveData<List<Claim>> myClaims = new MutableLiveData<>(null);
    private final List<Claim> allClaims = new ArrayList<>(); // newest first
    private final Map<String, List<ChatMessage>> chats = new HashMap<>();
    private final Map<String, MutableLiveData<List<ChatMessage>>> chatLiveData = new HashMap<>();
    private AppUser user;
    private int nextMessageId = 1;

    @Override
    public LiveData<List<Claim>> claims() {
        return myClaims;
    }

    @Override
    public void start(AppUser user) {
        this.user = user;
        myClaims.setValue(null);
        MockDelay.run(this::publish);
    }

    @Override
    public void stop() {
        user = null;
        myClaims.setValue(null);
    }

    @Override
    public void create(Claim claim, Callback<String> callback) {
        MockDelay.run(() -> {
            String id = Claim.idFor(claim.itemId, claim.claimantId);
            if (find(id) != null) {
                callback.onError("You have already sent a claim for this post.");
                return;
            }
            if (claim.claimantId.equals(claim.ownerId)) {
                callback.onError("You cannot claim your own post.");
                return;
            }
            Claim saved = claim.copy();
            saved.id = id;
            saved.status = Claim.PENDING;
            allClaims.add(0, saved);
            publish();
            callback.onSuccess(id);
        });
    }

    @Override
    public void setStatus(String claimId, String status, Callback<Void> callback) {
        MockDelay.run(() -> {
            Claim claim = find(claimId);
            if (claim == null) {
                callback.onError("This claim no longer exists.");
                return;
            }
            if (user == null || !claim.isOwner(user.uid)) {
                callback.onError("Only the person who posted the item can decide.");
                return;
            }
            claim.status = status;
            claim.updatedAt = System.currentTimeMillis();
            publish();
            callback.onSuccess(null);
        });
    }

    @Override
    public LiveData<List<ChatMessage>> messages(String claimId) {
        MutableLiveData<List<ChatMessage>> live = chatLiveData.get(claimId);
        if (live == null) {
            live = new MutableLiveData<>(new ArrayList<>(chatOf(claimId)));
            chatLiveData.put(claimId, live);
        }
        return live;
    }

    @Override
    public void sendMessage(String claimId, ChatMessage message, Callback<Void> callback) {
        MockDelay.run(() -> {
            Claim claim = find(claimId);
            if (claim == null || !claim.isApproved()) {
                callback.onError("Chat opens after the claim is approved.");
                return;
            }
            ChatMessage saved = new ChatMessage(message.senderId, message.senderName, message.text, message.createdAt);
            saved.id = "demo-msg-" + nextMessageId++;
            chatOf(claimId).add(saved);
            ((MutableLiveData<List<ChatMessage>>) messages(claimId)).setValue(new ArrayList<>(chatOf(claimId)));
            callback.onSuccess(null);
        });
    }

    @Nullable
    @Override
    public Claim findById(String claimId) {
        Claim claim = find(claimId);
        return claim == null ? null : claim.copy();
    }

    @Override
    public void clearAll() {
        allClaims.clear();
        chats.clear();
        for (MutableLiveData<List<ChatMessage>> live : chatLiveData.values()) live.setValue(new ArrayList<>());
        publish();
    }

    private List<ChatMessage> chatOf(String claimId) {
        List<ChatMessage> list = chats.get(claimId);
        if (list == null) {
            list = new ArrayList<>();
            chats.put(claimId, list);
        }
        return list;
    }

    private Claim find(String id) {
        for (Claim c : allClaims) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }

    /** Only the claims the logged-in student takes part in, like the Firestore query. */
    private void publish() {
        if (user == null) return;
        List<Claim> mine = new ArrayList<>();
        for (Claim c : allClaims) {
            if (c.ownerId.equals(user.uid) || c.claimantId.equals(user.uid)) mine.add(c.copy());
        }
        myClaims.setValue(mine);
    }
}
