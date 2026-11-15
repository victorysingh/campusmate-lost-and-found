package com.campusmate.logic;

import com.campusmate.Config;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Form validators. Each returns null when the value is OK, or the error
 * message to show under the text box.
 */
public final class Validators {

    private Validators() {
    }

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** True if {@code email} may use the app. An empty domain allows every email. */
    public static boolean isAllowedEmail(String email, String domain) {
        if (domain == null || domain.isEmpty()) return true;
        return email.trim().toLowerCase(Locale.ROOT).endsWith("@" + domain.toLowerCase(Locale.ROOT));
    }

    public static String email(String value) {
        return email(value, Config.ALLOWED_EMAIL_DOMAIN);
    }

    public static String email(String value, String domain) {
        String email = value == null ? "" : value.trim();
        if (email.isEmpty()) return "Enter your email";
        if (!EMAIL.matcher(email).matches()) return "Enter a valid email";
        if (!isAllowedEmail(email, domain)) return "Use your college email (@" + domain + ")";
        return null;
    }

    public static String password(String value) {
        if (value == null || value.length() < 6) return "Password must be at least 6 characters";
        return null;
    }

    public static String confirmPassword(String password, String confirm) {
        if (confirm == null || !confirm.equals(password)) return "Passwords do not match";
        return null;
    }

    public static String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) return "Enter " + fieldName;
        return null;
    }

    /** Required, with a minimum and maximum length (after trimming). */
    public static String length(String value, String fieldName, int min, int max) {
        String missing = required(value, fieldName);
        if (missing != null) return missing;
        int len = value.trim().length();
        if (len < min) return "Too short: use at least " + min + " characters";
        if (len > max) return "Too long: use at most " + max + " characters";
        return null;
    }

    public static String title(String value) {
        return length(value, "a title", 3, 60);
    }

    public static String description(String value) {
        return length(value, "a short description", 10, 500);
    }

    public static String proofQuestion(String value) {
        String error = length(value, "a question only the owner can answer", 8, 150);
        if (error == null && !value.trim().endsWith("?")) return "End the question with \"?\"";
        return error;
    }

    public static String claimAnswer(String value) {
        return length(value, "your answer", 2, 300);
    }

    public static String chatMessage(String value) {
        if (value == null || value.trim().isEmpty()) return "Type a message";
        if (value.trim().length() > 1000) return "Message is too long";
        return null;
    }
}
