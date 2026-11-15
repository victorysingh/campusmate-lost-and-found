package com.campusmate.data;

import com.campusmate.Config;
import com.campusmate.model.AppUser;
import com.campusmate.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 12 realistic example posts for demos (demo menu -> Load sample data).
 *
 * Five lost/found pairs match each other and two posts have no match, so
 * every part of matching can be shown. In mock mode the posts belong to six
 * sample students, who also have demo accounts (password "demo123"), so you
 * can log in as the finder and approve a claim.
 */
public final class SampleData {

    private SampleData() {
    }

    public static final String[] STUDENTS = {
            "Priya Sharma", "Rahul Kumar", "Ananya Rao", "Kiran M", "Sneha Patil", "Arjun N",
    };

    public static String uidOf(String name) {
        return "sample-" + name.toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    public static String emailOf(String name) {
        return name.toLowerCase(Locale.ROOT).replace(' ', '.') + "@" + Config.exampleDomain();
    }

    /** type, title, category, description, location, posted by, proof question (found posts). */
    private static final String[][] ROWS = {
            {Item.LOST, "Apple AirPods Pro", "Electronics",
                    "White AirPods Pro in the charging case. Small scratch on the lid and a blue sticker inside.",
                    "Library · 1st floor reading area", "Priya Sharma", ""},
            {Item.FOUND, "AirPods case near reading tables", "Electronics",
                    "White AirPods case with both pods, has a blue sticker. Collected at the library desk.",
                    "Library", "Rahul Kumar", "What colour is the sticker inside the case?"},
            {Item.LOST, "Casio fx-991ES calculator", "Electronics",
                    "Scientific calculator, grey, name \"ANANYA\" scratched on the back cover.",
                    "Seminar hall", "Ananya Rao", ""},
            {Item.FOUND, "Scientific calculator", "Electronics",
                    "Casio calculator left on a canteen table after lunch. Has a name scratched on the back.",
                    "Block A canteen", "Kiran M", "What name is scratched on the back?"},
            {Item.LOST, "Student ID card", "ID Card",
                    "College ID card of a 2nd year ECE student, on a red lanyard.",
                    "Main gate", "Sneha Patil", ""},
            {Item.FOUND, "ID card with red lanyard", "ID Card",
                    "Found a college ID card on a red lanyard near the security cabin.",
                    "Main gate", "Arjun N", "Which branch and year is printed on the card?"},
            {Item.LOST, "Steel water bottle", "Water Bottle",
                    "Blue Milton steel bottle, 1 litre, with a dent near the bottom and stickers.",
                    "Basketball court", "Rahul Kumar", ""},
            {Item.FOUND, "Blue steel bottle", "Water Bottle",
                    "Blue steel bottle with stickers, left on the bench after the match.",
                    "Basketball court", "Priya Sharma", "What brand is the bottle?"},
            {Item.LOST, "Black leather wallet", "Wallet",
                    "Black leather wallet with a metro card and some cash. No ID inside.",
                    "Block A canteen", "Kiran M", ""},
            {Item.FOUND, "Wallet found in seminar hall", "Wallet",
                    "Black leather wallet under a seat in row 4 after the guest lecture.",
                    "Seminar hall", "Sneha Patil", "Which travel card is inside the wallet?"},
            {Item.LOST, "Data Structures notes", "Books & Notes",
                    "Blue spiral notebook with handwritten DSA notes for the internals.",
                    "CSE block · Room 204", "Arjun N", ""},
            {Item.FOUND, "Grey hoodie", "Clothing",
                    "Grey hoodie, size M, left in the auditorium after the fest rehearsal.",
                    "Auditorium", "Ananya Rao", "What is printed on the back of the hoodie?"},
    };

    /**
     * @param useSampleOwners true in mock mode. With Firebase the security
     *                        rules only allow posting as yourself, so pass false.
     */
    public static List<Item> build(AppUser me, boolean useSampleOwners, long now) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < ROWS.length; i++) {
            String[] r = ROWS[i];
            Item item = new Item();
            item.type = r[0];
            item.title = r[1];
            item.category = r[2];
            item.description = r[3];
            item.location = r[4];
            item.proofQuestion = r[6];
            item.date = now - (i / 3) * 86_400_000L;
            item.ownerId = useSampleOwners ? uidOf(r[5]) : me.uid;
            item.ownerName = useSampleOwners ? r[5] : me.name;
            item.ownerEmail = useSampleOwners ? emailOf(r[5]) : me.email;
            // Spread out by a few minutes so the feed order is stable.
            item.createdAt = now - 5 * 60_000L * (ROWS.length - i);
            items.add(item);
        }
        return items;
    }
}
