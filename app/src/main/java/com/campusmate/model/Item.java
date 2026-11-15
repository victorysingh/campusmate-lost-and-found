package com.campusmate.model;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * One Lost or Found post. Stored as a document in the Firestore collection
 * "items" (see {@link #toMap()} and {@link #fromMap} for the exact fields).
 */
public class Item {

    public static final String LOST = "lost";
    public static final String FOUND = "found";
    public static final String OPEN = "open";
    public static final String RETURNED = "returned";

    public String id = "";            // Firestore document id
    public String type = LOST;         // LOST or FOUND
    public String title = "";
    public String category = "Other";
    public String description = "";
    public String location = "";
    public long date;                  // day it was lost/found (milliseconds)
    public String imageBase64;         // compressed JPEG as text, or null (no Firebase Storage)
    public String ownerId = "";        // uid of the student who posted it
    public String ownerName = "";
    public String ownerEmail = "";
    public long createdAt;             // when the post was made; the feed sorts by this
    public String status = OPEN;       // OPEN, or RETURNED once the item is back with its owner
    public String proofQuestion = "";  // FOUND posts: question only the real owner can answer

    public boolean isLost() {
        return LOST.equals(type);
    }

    public boolean isReturned() {
        return RETURNED.equals(status);
    }

    public boolean hasImage() {
        return imageBase64 != null && !imageBase64.isEmpty();
    }

    /** Converts this item into a map that Firestore can store. */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("title", title);
        map.put("category", category);
        map.put("description", description);
        map.put("location", location);
        map.put("date", new Timestamp(new Date(date)));
        map.put("imageBase64", imageBase64);
        map.put("ownerId", ownerId);
        map.put("ownerName", ownerName);
        map.put("ownerEmail", ownerEmail);
        map.put("createdAt", new Timestamp(new Date(createdAt)));
        map.put("status", status);
        map.put("proofQuestion", proofQuestion);
        return map;
    }

    /** Builds an item from a Firestore document. Missing fields get safe defaults, so one bad document cannot crash the feed. */
    public static Item fromMap(String id, Map<String, Object> map) {
        Item item = new Item();
        item.id = id;
        item.type = FOUND.equals(map.get("type")) ? FOUND : LOST;
        item.title = text(map.get("title"), "");
        item.category = text(map.get("category"), "Other");
        item.description = text(map.get("description"), "");
        item.location = text(map.get("location"), "");
        item.date = millis(map.get("date"));
        Object image = map.get("imageBase64");
        item.imageBase64 = image instanceof String ? (String) image : null;
        item.ownerId = text(map.get("ownerId"), "");
        item.ownerName = text(map.get("ownerName"), "");
        item.ownerEmail = text(map.get("ownerEmail"), "");
        item.createdAt = millis(map.get("createdAt"));
        item.status = RETURNED.equals(map.get("status")) ? RETURNED : OPEN;
        item.proofQuestion = text(map.get("proofQuestion"), "");
        return item;
    }

    /** A field-by-field copy, used by the edit screen and the mock database. */
    public Item copy() {
        Item c = new Item();
        c.id = id;
        c.type = type;
        c.title = title;
        c.category = category;
        c.description = description;
        c.location = location;
        c.date = date;
        c.imageBase64 = imageBase64;
        c.ownerId = ownerId;
        c.ownerName = ownerName;
        c.ownerEmail = ownerEmail;
        c.createdAt = createdAt;
        c.status = status;
        c.proofQuestion = proofQuestion;
        return c;
    }

    static String text(Object value, String fallback) {
        return value instanceof String ? (String) value : fallback;
    }

    static long millis(Object value) {
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().getTime();
        if (value instanceof Date) return ((Date) value).getTime();
        if (value instanceof Long) return (Long) value;
        return System.currentTimeMillis();
    }
}
