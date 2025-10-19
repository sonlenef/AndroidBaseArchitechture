package dev.sonle.androidbasearchitechture.core.util

import java.util.regex.Pattern

/**
 * Utility class for input validation
 */
object ValidationUtils {
    
    // Email validation pattern
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    
    // Phone validation pattern (basic international format)
    private val PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s\\-()]{7,20}$"
    )
    
    // Website validation pattern
    private val WEBSITE_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$"
    )
    
    /**
     * Validates email format
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        if (email.length > Constants.MAX_EMAIL_LENGTH) return false
        return EMAIL_PATTERN.matcher(email.trim()).matches()
    }
    
    /**
     * Validates password strength
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= Constants.MIN_PASSWORD_LENGTH
    }
    
    /**
     * Validates name format
     */
    fun isValidName(name: String): Boolean {
        if (name.isBlank()) return false
        val trimmedName = name.trim()
        return trimmedName.length in Constants.MIN_NAME_LENGTH..Constants.MAX_NAME_LENGTH &&
                trimmedName.all { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }
    }
    
    /**
     * Validates phone number format
     */
    fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        val trimmedPhone = phone.trim()
        return trimmedPhone.length in 7..Constants.MAX_PHONE_LENGTH &&
                PHONE_PATTERN.matcher(trimmedPhone).matches()
    }
    
    /**
     * Validates website URL format
     */
    fun isValidWebsite(website: String): Boolean {
        if (website.isBlank()) return false
        val trimmedWebsite = website.trim()
        return trimmedWebsite.length <= Constants.MAX_WEBSITE_LENGTH &&
                WEBSITE_PATTERN.matcher(trimmedWebsite).matches()
    }
    
    /**
     * Validates if string is not blank
     */
    fun isNotBlank(value: String): Boolean {
        return value.isNotBlank()
    }
    
    /**
     * Validates if string length is within range
     */
    fun isLengthInRange(value: String, minLength: Int, maxLength: Int): Boolean {
        return value.length in minLength..maxLength
    }
    
    /**
     * Validates if string contains only letters and spaces
     */
    fun containsOnlyLettersAndSpaces(value: String): Boolean {
        return value.all { it.isLetter() || it.isWhitespace() }
    }
    
    /**
     * Validates if string contains only alphanumeric characters
     */
    fun containsOnlyAlphanumeric(value: String): Boolean {
        return value.all { it.isLetterOrDigit() }
    }
    
    /**
     * Validates if string is a valid integer
     */
    fun isValidInteger(value: String): Boolean {
        return try {
            value.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Validates if string is a valid long
     */
    fun isValidLong(value: String): Boolean {
        return try {
            value.toLong()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Validates if string is a valid double
     */
    fun isValidDouble(value: String): Boolean {
        return try {
            value.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
}
