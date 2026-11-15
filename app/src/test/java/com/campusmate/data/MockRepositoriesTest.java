package com.campusmate.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.campusmate.Config;
import com.campusmate.TestBase;
import com.campusmate.logic.MatchService;
import com.campusmate.model.AppUser;
import com.campusmate.model.ChatMessage;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** The claim -> approve -> chat rules, checked on the mock database. */
@RunWith(RobolectricTestRunner.class)
public class MockRepositoriesTest extends TestBase {

    private Claim claimFrom(AppUser claimant, Item item) {
        Claim c = new Claim();
        c.itemId = item.id;
        c.itemTitle = item.title;
        c.itemType = item.type;
        c.ownerId = item.ownerId;
        c.ownerName = item.ownerName;
        c.claimantId = claimant.uid;
        c.claimantName = claimant.name;
        c.question = item.proofQuestion;
        c.answer = "Blue";
        return c;
    }

    @Test
    public void sampleDataHasTwelvePostsAndFiveMatchingPairs() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        List<Item> items = Repositories.items().feed().getValue().items;
        assertEquals(12, items.size());
        int lostWithMatch = 0;
        for (Item item : items) {
            if (item.isLost() && !MatchService.findMatches(item, items).isEmpty()) lostWithMatch++;
        }
        assertEquals(5, lostWithMatch);
    }

    @Test
    public void claimApproveChatFlow() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        startWithSamples(asha);
        Item airpods = findByTitle("AirPods case near reading tables"); // posted by Rahul

        AtomicReference<String> claimId = new AtomicReference<>();
        Repositories.claims().create(claimFrom(asha, airpods), callback(claimId));
        idle();
        assertEquals(Claim.idFor(airpods.id, asha.uid), claimId.get());

        // A second claim on the same post is refused.
        AtomicReference<String> error = new AtomicReference<>();
        Repositories.claims().create(claimFrom(asha, airpods), errorInto(error));
        idle();
        assertNotNull(error.get());

        // Chat is locked while pending.
        Repositories.claims().sendMessage(claimId.get(), new ChatMessage(asha.uid, asha.name, "Hi", 1), errorInto(error));
        idle();
        assertEquals("Chat opens after the claim is approved.", error.get());

        // Asha cannot approve her own claim.
        error.set(null);
        Repositories.claims().setStatus(claimId.get(), Claim.APPROVED, errorInto(error));
        idle();
        assertNotNull(error.get());

        // Rahul logs in and approves.
        Session.logOut();
        AppUser rahulUser = logIn(rahul(), Config.DEMO_PASSWORD);
        Session.start(context, rahulUser);
        idle();
        assertEquals(1, Repositories.claims().claims().getValue().size());
        Repositories.claims().setStatus(claimId.get(), Claim.APPROVED, callback(new AtomicReference<>()));
        idle();
        assertTrue(Repositories.claims().findById(claimId.get()).isApproved());

        // Now both can chat.
        Repositories.claims().sendMessage(claimId.get(),
                new ChatMessage(rahulUser.uid, rahulUser.name, "Meet at the library desk?", 2), callback(new AtomicReference<>()));
        idle();
        assertEquals(1, Repositories.claims().messages(claimId.get()).getValue().size());
    }

    @Test
    public void editMarkReturnedAndDelete() {
        AppUser asha = signUp("Asha Rao", "asha.rao@college.edu");
        Session.start(context, asha);
        Item post = new Item();
        post.title = "Grey umbrella";
        post.category = "Other";
        post.ownerId = asha.uid;
        post.createdAt = System.currentTimeMillis();
        AtomicReference<String> id = new AtomicReference<>();
        Repositories.items().add(post, callback(id));
        idle();

        Item saved = Repositories.items().findById(id.get());
        saved.title = "Grey folding umbrella";
        Repositories.items().update(saved, callback(new AtomicReference<>()));
        Repositories.items().setStatus(id.get(), Item.RETURNED, callback(new AtomicReference<>()));
        idle();
        Item updated = Repositories.items().findById(id.get());
        assertEquals("Grey folding umbrella", updated.title);
        assertTrue(updated.isReturned());

        Repositories.items().delete(id.get(), callback(new AtomicReference<>()));
        idle();
        assertNull(Repositories.items().findById(id.get()));
    }

    @Test
    public void wrongPasswordIsRefused() {
        AtomicReference<String> error = new AtomicReference<>();
        Repositories.auth().signIn(rahul(), "nope123", errorInto(error));
        idle();
        assertEquals("Wrong email or password.", error.get());
        assertNull(Repositories.auth().currentUser());
    }

    private static <T> Callback<T> errorInto(AtomicReference<String> error) {
        return new Callback<T>() {
            @Override
            public void onSuccess(T result) {
                throw new AssertionError("expected an error");
            }

            @Override
            public void onError(String message) {
                error.set(message);
            }
        };
    }
}
