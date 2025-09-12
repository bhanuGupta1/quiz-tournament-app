package com.quiztournament.quiz_backend.util;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6 && password.length() <= 100;
    }

    /**
     * Sanitize string input
     */
    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("[<>\"'&]", "");
    }

    /**
     * Validate tournament name
     */
    public static boolean isValidTournamentName(String name) {
        return name != null && name.trim().length() >= 3 && name.trim().length() <= 100;
    }

    /**
     * Validate score range
     */
    public static boolean isValidScore(Double score) {
        return score != null && score >= 0.0 && score <= 100.0;
    }

    /**
     * Check if string is not null or empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}