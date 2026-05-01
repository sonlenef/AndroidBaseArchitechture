package dev.sonle.androidbasearchitecture.core.util

import dev.sonle.androidbasearchitecture.TestData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ValidationUtils
 */
class ValidationUtilsTest {
    
    @Test
    fun `isValidEmail should return true for valid email`() {
        assertTrue(ValidationUtils.isValidEmail(TestData.TEST_EMAIL))
        assertTrue(ValidationUtils.isValidEmail("user@domain.com"))
        assertTrue(ValidationUtils.isValidEmail("test.email+tag@example.co.uk"))
    }
    
    @Test
    fun `isValidEmail should return false for invalid email`() {
        assertFalse(ValidationUtils.isValidEmail(TestData.INVALID_EMAIL))
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("user@"))
        assertFalse(ValidationUtils.isValidEmail("@domain.com"))
        assertFalse(ValidationUtils.isValidEmail("user.domain.com"))
    }
    
    @Test
    fun `isValidPassword should return true for valid password`() {
        assertTrue(ValidationUtils.isValidPassword(TestData.TEST_PASSWORD))
        assertTrue(ValidationUtils.isValidPassword("123456"))
        assertTrue(ValidationUtils.isValidPassword("password"))
    }
    
    @Test
    fun `isValidPassword should return false for invalid password`() {
        assertFalse(ValidationUtils.isValidPassword(TestData.INVALID_PASSWORD))
        assertFalse(ValidationUtils.isValidPassword(""))
        assertFalse(ValidationUtils.isValidPassword("12345"))
    }
    
    @Test
    fun `isValidName should return true for valid name`() {
        assertTrue(ValidationUtils.isValidName(TestData.TEST_NAME))
        assertTrue(ValidationUtils.isValidName("John Doe"))
        assertTrue(ValidationUtils.isValidName("Mary-Jane"))
        assertTrue(ValidationUtils.isValidName("O'Connor"))
    }
    
    @Test
    fun `isValidName should return false for invalid name`() {
        assertFalse(ValidationUtils.isValidName(TestData.INVALID_NAME))
        assertFalse(ValidationUtils.isValidName("A"))
        assertFalse(ValidationUtils.isValidName("John123"))
        assertFalse(ValidationUtils.isValidName("John@Doe"))
    }
    
    @Test
    fun `isValidPhone should return true for valid phone`() {
        assertTrue(ValidationUtils.isValidPhone(TestData.TEST_PHONE))
        assertTrue(ValidationUtils.isValidPhone("+1234567890"))
        assertTrue(ValidationUtils.isValidPhone("123-456-7890"))
        assertTrue(ValidationUtils.isValidPhone("(123) 456-7890"))
    }
    
    @Test
    fun `isValidPhone should return false for invalid phone`() {
        assertFalse(ValidationUtils.isValidPhone(TestData.INVALID_PHONE))
        assertFalse(ValidationUtils.isValidPhone(""))
        assertFalse(ValidationUtils.isValidPhone("123"))
        assertFalse(ValidationUtils.isValidPhone("abc-def-ghij"))
    }
    
    @Test
    fun `isValidWebsite should return true for valid website`() {
        assertTrue(ValidationUtils.isValidWebsite(TestData.TEST_WEBSITE))
        assertTrue(ValidationUtils.isValidWebsite("https://example.com"))
        assertTrue(ValidationUtils.isValidWebsite("http://example.com"))
        assertTrue(ValidationUtils.isValidWebsite("example.com"))
    }
    
    @Test
    fun `isValidWebsite should return false for invalid website`() {
        assertFalse(ValidationUtils.isValidWebsite(TestData.INVALID_WEBSITE))
        assertFalse(ValidationUtils.isValidWebsite(""))
        assertFalse(ValidationUtils.isValidWebsite("not-a-website"))
        assertFalse(ValidationUtils.isValidWebsite("ftp://example.com"))
    }
}
