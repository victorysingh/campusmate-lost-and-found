package com.campusmate.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.campusmate.model.Claim;
import com.campusmate.model.Item;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetectorTest {

    private static Claim claim(String status) {
        Claim c = new Claim();
        c.id = "item1_asha";
        c.itemId = "item1";
        c.itemTitle = "AirPods case";
        c.itemType = Item.FOUND;
        c.ownerId = "rahul";
        c.ownerName = "Rahul";
        c.claimantId = "asha";
        c.claimantName = "Asha";
        c.status = status;
        return c;
    }

    @Test
    public void ownerHearsAboutANewClaim() {
        List<EventDetector.Notice> notices = EventDetector.forClaims("rahul", new HashMap<>(),
                Collections.singletonList(claim(Claim.PENDING)));
        assertEquals(1, notices.size());
        assertTrue(notices.get(0).title.startsWith("New claim"));
        assertEquals("item1_asha", notices.get(0).claimId);
    }

    @Test
    public void claimantHearsAboutApprovalOnlyWhenStatusChanged() {
        Map<String, String> lastSeen = new HashMap<>();
        lastSeen.put("item1_asha", Claim.PENDING);
        List<EventDetector.Notice> approved = EventDetector.forClaims("asha", lastSeen,
                Collections.singletonList(claim(Claim.APPROVED)));
        assertEquals("Claim approved", approved.get(0).title);

        lastSeen.put("item1_asha", Claim.APPROVED);
        assertTrue(EventDetector.forClaims("asha", lastSeen,
                Collections.singletonList(claim(Claim.APPROVED))).isEmpty());
    }

    @Test
    public void oldDecisionsAreNotAnnouncedOnANewPhone() {
        // Nothing seen before on this phone: an old approval is not news.
        assertTrue(EventDetector.forClaims("asha", new HashMap<>(),
                Collections.singletonList(claim(Claim.APPROVED))).isEmpty());
    }

    @Test
    public void newMatchingPostFromSomeoneElseNotifiesMe() {
        Item mine = new Item();
        mine.id = "a";
        mine.ownerId = "asha";
        mine.type = Item.LOST;
        mine.category = "Wallet";
        mine.title = "Black leather wallet";
        Item theirs = new Item();
        theirs.id = "b";
        theirs.ownerId = "kiran";
        theirs.ownerName = "Kiran";
        theirs.type = Item.FOUND;
        theirs.category = "Wallet";
        theirs.title = "Leather wallet found";
        theirs.location = "Seminar hall";

        List<EventDetector.Notice> notices = EventDetector.forNewItems("asha",
                Collections.singletonList(theirs), Arrays.asList(mine, theirs));
        assertEquals(1, notices.size());
        assertEquals("a", notices.get(0).itemId); // tapping opens MY post and its matches

        // My own new post never notifies me.
        assertTrue(EventDetector.forNewItems("kiran", Collections.singletonList(theirs),
                Arrays.asList(mine, theirs)).isEmpty());
    }
}
