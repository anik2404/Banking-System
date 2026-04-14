package com.banking.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil — hashes passwords with SHA-256.
 * Passwords are NEVER stored in plain text.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Returns the SHA-256 hex digest of the input string.
     * Example: hash("secret") -> "2bb80d537..." (64 hex chars)
     */
    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }

    /** Returns true if plainText hashes to the stored hash. */
    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equals(storedHash);
    }
}