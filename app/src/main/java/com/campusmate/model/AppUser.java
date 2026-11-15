package com.campusmate.model;

/** The logged-in student. */
public class AppUser {

    public final String uid;
    public final String name;
    public final String email;

    public AppUser(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    /** "Asha Rao" -> "Asha", used in the greeting. */
    public String firstName() {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) return "there";
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }

    /** "Asha Rao" -> "AR", shown in the round avatar. */
    public String initials() {
        String[] parts = (name == null ? "" : name.trim()).split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }
}
