package dev.sonle.pdfscanner

import dev.sonle.pdfscanner.data.model.UserDto
import dev.sonle.pdfscanner.data.model.UserEntity
import dev.sonle.pdfscanner.domain.model.User

/**
 * Test data for unit tests
 */
object TestData {
    
    // Sample User domain model
    val sampleUser = User(
        id = 1L,
        name = "John Doe",
        email = "john.doe@example.com",
        phone = "+1234567890",
        website = "https://johndoe.com",
        username = "johndoe",
        isFavorite = false,
        createdAt = 1640995200000L, // 2022-01-01
        updatedAt = 1640995200000L
    )
    
    // Sample User DTO
    val sampleUserDto = UserDto(
        id = 1L,
        name = "John Doe",
        email = "john.doe@example.com",
        phone = "+1234567890",
        website = "https://johndoe.com",
        username = "johndoe"
    )
    
    // Sample User Entity
    val sampleUserEntity = UserEntity(
        id = 1L,
        name = "John Doe",
        email = "john.doe@example.com",
        phone = "+1234567890",
        website = "https://johndoe.com",
        username = "johndoe",
        isFavorite = false,
        createdAt = 1640995200000L,
        updatedAt = 1640995200000L
    )
    
    // Sample User list
    val sampleUsers = listOf(
        sampleUser,
        User(
            id = 2L,
            name = "Jane Smith",
            email = "jane.smith@example.com",
            phone = "+0987654321",
            website = "https://janesmith.com",
            username = "janesmith",
            isFavorite = true
        ),
        User(
            id = 3L,
            name = "Bob Johnson",
            email = "bob.johnson@example.com",
            phone = "+1122334455",
            website = "https://bobjohnson.com",
            username = "bobjohnson",
            isFavorite = false
        )
    )
    
    // Sample User DTO list
    val sampleUserDtos = listOf(
        sampleUserDto,
        UserDto(
            id = 2L,
            name = "Jane Smith",
            email = "jane.smith@example.com",
            phone = "+0987654321",
            website = "https://janesmith.com",
            username = "janesmith"
        ),
        UserDto(
            id = 3L,
            name = "Bob Johnson",
            email = "bob.johnson@example.com",
            phone = "+1122334455",
            website = "https://bobjohnson.com",
            username = "bobjohnson"
        )
    )
    
    // Sample User Entity list
    val sampleUserEntities = listOf(
        sampleUserEntity,
        UserEntity(
            id = 2L,
            name = "Jane Smith",
            email = "jane.smith@example.com",
            phone = "+0987654321",
            website = "https://janesmith.com",
            username = "janesmith",
            isFavorite = true
        ),
        UserEntity(
            id = 3L,
            name = "Bob Johnson",
            email = "bob.johnson@example.com",
            phone = "+1122334455",
            website = "https://bobjohnson.com",
            username = "bobjohnson",
            isFavorite = false
        )
    )
    
    // Test constants
    const val TEST_EMAIL = "test@example.com"
    const val TEST_PASSWORD = "password123"
    const val TEST_NAME = "Test User"
    const val TEST_PHONE = "+1234567890"
    const val TEST_WEBSITE = "https://test.com"
    const val TEST_USERNAME = "testuser"
    const val TEST_USER_ID = 1L
    
    // Invalid test data
    const val INVALID_EMAIL = "invalid-email"
    const val INVALID_PASSWORD = "123"
    const val INVALID_NAME = ""
    const val INVALID_PHONE = "123"
    const val INVALID_WEBSITE = "not-a-website"
}
