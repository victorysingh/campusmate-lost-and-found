package com.campusmate.model;

import com.google.firebase.Timestamp;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A request from one student (the claimant) about someone else's post.
 *
 * On a FOUND post the claimant says "this is mine" and answers the finder's
 * proof question. On a LOST post the claimant says "I found this" and
 * explains where. The post owner then approves or rejects. Chat opens only
 * after approval.
 *
 * Stored in Firestore as claims/{itemId_claimantId}. Using that id means a
 * student can claim the same post only once.
 */
public class Claim {

    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";

    public String id = "";
    public String itemId = "";
    public String itemTitle = "";
    public String itemType = Item.FOUND;
    public String ownerId = "";       // who posted the item and decides
    public String ownerName = "";
    public String claimantId = "";    // who sent the claim
    public String claimantName = "";
    public String question = "";
    public String answer = "";
    public String status = PENDING;
    public long createdAt;
    public long updatedAt;

    public static String idFor(String itemId, String claimantId) {
        return itemId + "_" + claimantId;
    }

    public boolean isPending() {
        return PENDING.equals(status);
    }

    public boolean isApproved() {
        return APPROVED.equals(status);
    }

    public boolean isOwner(String uid) {
        return ownerId.equals(uid);
    }

    /** The name of the other person in this claim, seen from {@code myUid}. */
    public String otherPersonName(String myUid) {
        return isOwner(myUid) ? claimantName : ownerName;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("itemTitle", itemTitle);
        map.put("itemType", itemType);
        map.put("ownerId", ownerId);
        map.put("ownerName", ownerName);
        map.put("claimantId", claimantId);
        map.put("claimantName", claimantName);
        // Security rules let only these two people read the claim and its chat.
        map.put("participants", Arrays.asList(ownerId, claimantId));
        map.put("question", question);
        map.put("answer", answer);
        map.put("status", status);
        map.put("createdAt", new Timestamp(new Date(createdAt)));
        map.put("updatedAt", new Timestamp(new Date(updatedAt)));
        return map;
    }

    public static Claim fromMap(String id, Map<String, Object> map) {
        Claim c = new Claim();
        c.id = id;
        c.itemId = Item.text(map.get("itemId"), "");
        c.itemTitle = Item.text(map.get("itemTitle"), "");
        c.itemType = Item.LOST.equals(map.get("itemType")) ? Item.LOST : Item.FOUND;
        c.ownerId = Item.text(map.get("ownerId"), "");
        c.ownerName = Item.text(map.get("ownerName"), "");
        c.claimantId = Item.text(map.get("claimantId"), "");
        c.claimantName = Item.text(map.get("claimantName"), "");
        c.question = Item.text(map.get("question"), "");
        c.answer = Item.text(map.get("answer"), "");
        Object status = map.get("status");
        c.status = APPROVED.equals(status) ? APPROVED : REJECTED.equals(status) ? REJECTED : PENDING;
        c.createdAt = Item.millis(map.get("createdAt"));
        c.updatedAt = Item.millis(map.get("updatedAt"));
        return c;
    }

    public Claim copy() {
        Claim c = fromMap(id, toMap());
        c.createdAt = createdAt;
        c.updatedAt = updatedAt;
        return c;
    }
}
