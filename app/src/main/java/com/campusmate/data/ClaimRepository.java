package com.campusmate.data;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.campusmate.model.AppUser;
import com.campusmate.model.ChatMessage;
import com.campusmate.model.Claim;

import java.util.List;

/** Claims and their chats. Implemented by FirestoreClaimRepository and MockClaimRepository. */
public interface ClaimRepository {

    /** Claims where I am the post owner or the claimant, newest first. null = still loading. */
    LiveData<List<Claim>> claims();

    void start(AppUser user);

    void stop();

    /** Sends a new claim (status PENDING). Fails if I already claimed this post. */
    void create(Claim claim, Callback<String> callback);

    /** Post owner only: Claim.APPROVED or Claim.REJECTED. */
    void setStatus(String claimId, String status, Callback<Void> callback);

    /** Live chat messages of one claim, oldest first. Listens only while a screen observes it. */
    LiveData<List<ChatMessage>> messages(String claimId);

    void sendMessage(String claimId, ChatMessage message, Callback<Void> callback);

    @Nullable
    Claim findById(String claimId);

    /** Mock mode only: remove every claim and message. */
    void clearAll();
}
