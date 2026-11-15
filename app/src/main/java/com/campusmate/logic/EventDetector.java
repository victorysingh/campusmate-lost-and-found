package com.campusmate.logic;

import com.campusmate.model.Claim;
import com.campusmate.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Decides which phone notifications to show when the data changes.
 *
 * It has no Android code, so it is easy to unit test. NotificationWatcher
 * feeds it the latest Firestore (or mock) data and shows what it returns.
 */
public final class EventDetector {

    private EventDetector() {
    }

    /** One notification to show. {@code itemId} / {@code claimId} say what to open on tap. */
    public static final class Notice {
        public final String title;
        public final String text;
        public final String itemId;
        public final String claimId;

        public Notice(String title, String text, String itemId, String claimId) {
            this.title = title;
            this.text = text;
            this.itemId = itemId;
            this.claimId = claimId;
        }
    }

    /**
     * Compares claims with the statuses we saw last time ({@code lastSeen}: claim id -> status).
     *  - I own the post and a claim I have not seen arrives -> "New claim".
     *  - I sent the claim and its status changed -> "Approved" / "Rejected".
     */
    public static List<Notice> forClaims(String myUid, Map<String, String> lastSeen, List<Claim> claims) {
        List<Notice> notices = new ArrayList<>();
        for (Claim claim : claims) {
            String before = lastSeen.get(claim.id);
            if (claim.isOwner(myUid)) {
                if (before == null && claim.isPending()) {
                    String what = Item.FOUND.equals(claim.itemType)
                            ? claim.claimantName + " says it is theirs. Check the answer and approve or reject."
                            : claim.claimantName + " may have found it. Check the details and approve or reject.";
                    notices.add(new Notice("New claim on \"" + claim.itemTitle + "\"", what, claim.itemId, claim.id));
                }
            } else if (before != null && !before.equals(claim.status)) {
                if (claim.isApproved()) {
                    notices.add(new Notice("Claim approved",
                            claim.ownerName + " approved your claim for \"" + claim.itemTitle + "\". Open the chat to meet up.",
                            claim.itemId, claim.id));
                } else if (Claim.REJECTED.equals(claim.status)) {
                    notices.add(new Notice("Claim not approved",
                            claim.ownerName + " did not approve your claim for \"" + claim.itemTitle + "\".",
                            claim.itemId, claim.id));
                }
            }
        }
        return notices;
    }

    /**
     * For posts that just appeared ({@code newItems}), finds the ones made by
     * someone else that match one of my open posts.
     */
    public static List<Notice> forNewItems(String myUid, Collection<Item> newItems, List<Item> allItems) {
        List<Item> myOpenPosts = new ArrayList<>();
        for (Item item : allItems) {
            if (item.ownerId.equals(myUid) && !item.isReturned()) myOpenPosts.add(item);
        }

        List<Notice> notices = new ArrayList<>();
        for (Item newItem : newItems) {
            if (newItem.ownerId.equals(myUid) || newItem.isReturned()) continue;
            List<MatchService.MatchResult> matches = MatchService.findMatches(newItem, myOpenPosts);
            if (matches.isEmpty()) continue;
            MatchService.MatchResult best = matches.get(0);
            String kind = newItem.isLost() ? "LOST" : "FOUND";
            notices.add(new Notice(
                    "Possible match for your \"" + best.item.title + "\"",
                    newItem.ownerName + " posted \"" + newItem.title + "\" (" + kind + ") at " + newItem.location
                            + ". Matched on: " + String.join(", ", best.commonWords),
                    best.item.id, null));
        }
        return notices;
    }
}
