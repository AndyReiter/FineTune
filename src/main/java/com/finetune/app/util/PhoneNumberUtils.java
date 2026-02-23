package com.finetune.app.util;

/**
 * Utility class for normalizing phone numbers to a consistent format.
 * Removes all non-digit characters for consistent matching.
 */
public class PhoneNumberUtils {

    /**
     * Normalizes a phone number by removing all non-digit characters.
     * This allows matching phone numbers regardless of formatting.
     * 
     * Examples:
     * - "(555) 123-4567" -> "5551234567"
     * - "555.123.4567" -> "5551234567"
     * - "+1-555-123-4567" -> "15551234567"
     * 
     * @param phoneNumber The phone number to normalize
     * @return Normalized phone number (digits only), or original if null
     */
    public static String normalize(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // Remove all non-digit characters
        String normalized = phoneNumber.replaceAll("[^0-9]", "");
        
        // If it's empty after removing non-digits, return the original
        if (normalized.isEmpty()) {
            return phoneNumber;
        }
        
        return normalized;
    }

    /**
     * Checks if two phone numbers match after normalization.
     * 
     * @param phone1 First phone number
     * @param phone2 Second phone number
     * @return true if the normalized phone numbers match, false otherwise
     */
    public static boolean matches(String phone1, String phone2) {
        if (phone1 == null && phone2 == null) {
            return true;
        }
        if (phone1 == null || phone2 == null) {
            return false;
        }
        
        String normalized1 = normalize(phone1);
        String normalized2 = normalize(phone2);
        
        return normalized1.equals(normalized2);
    }
}
