package com.campusmate;

/**
 * App-wide settings the team may need to change. Nothing secret goes here.
 */
public final class Config {

    private Config() {
    }

    /**
     * true  = MOCK MODE: logins, posts, claims and chats live in memory only
     *         (no internet or Firebase needed). Use this for demos.
     * false = use Firebase. If Firebase is not set up yet (no
     *         google-services.json), the app still falls back to mock mode.
     */
    public static final boolean USE_MOCK_DATA = true;

    /**
     * Only emails ending in "@" + this domain can sign up or log in.
     * TODO(team): put the college domain here, for example "dsce.edu.in".
     * Leave it empty to allow any email address (useful while testing).
     */
    public static final String ALLOWED_EMAIL_DOMAIN = "";

    /** Categories shown as chips and in the post form. Matching only compares posts in the same category. */
    public static final String[] CATEGORIES = {
            "Electronics", "ID Card", "Wallet", "Keys", "Books & Notes",
            "Bags", "Clothing", "Water Bottle", "Other",
    };

    /** Places on campus offered in the post form's location step. */
    public static final String[] CAMPUS_LOCATIONS = {
            "Main gate", "Library", "Block A canteen", "Seminar hall", "Auditorium",
            "CSE block", "Mechanical block", "Basketball court", "Parking area", "Hostel", "Other",
    };

    /** Photos are compressed until they are smaller than this (300 KB). */
    public static final int MAX_IMAGE_BYTES = 300 * 1024;

    /** The feed loads at most this many of the newest posts. */
    public static final int FEED_LIMIT = 100;

    /** Password of the sample student accounts in mock mode (see SampleData). */
    public static final String DEMO_PASSWORD = "demo123";

    /** Email domain used for sample students and examples. */
    public static String exampleDomain() {
        return ALLOWED_EMAIL_DOMAIN.isEmpty() ? "college.edu" : ALLOWED_EMAIL_DOMAIN;
    }
}
