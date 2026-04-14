package com.banking.util;

/**
 * InputValidator — centralised input validation helpers.
 */
public class InputValidator {

    private InputValidator() {}

    public static boolean isValidUsername(String username) {
        // 3-50 characters, alphanumeric + underscore only
        return username != null &&
               username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    public static boolean isValidPassword(String password) {
        // Minimum 6 characters
        return password != null && password.length() >= 6;
    }

    public static boolean isValidAmount(double amount) {
        return amount > 0 && amount <= 10_000_000;
    }

    public static boolean isValidFullName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 100;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true; // optional
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
}