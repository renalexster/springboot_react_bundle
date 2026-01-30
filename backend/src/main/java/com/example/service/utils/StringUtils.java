package com.example.service.utils;

/**
 * String helper utilities used across the service layer.
 */
public final class StringUtils {

    private StringUtils() {
        // utility class
    }

    /**
     * Trims the input and returns null if the resulting string is empty.
     */
    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Joins two strings with a single space, ignoring blanks.
     * If both inputs are blank/null, returns null. If one is blank, returns the other trimmed.
     */
    public static String joinNonBlank(String a, String b) {
        String at = trimToNull(a);
        String bt = trimToNull(b);
        if (at == null && bt == null) return null;
        if (at == null) return bt;
        if (bt == null) return at;
        return at + ' ' + bt;
    }
}
