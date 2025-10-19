package dev.sonle.androidbasearchitechture.domain.usecase

import app.cash.turbine.test
import dev.sonle.androidbasearchitechture.TestData
import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for LoginUseCase
 */
@ExperimentalCoroutinesApi
class LoginUseCaseTest {
    
    @get:Rule
    val testDispatcherRule = dev.sonle.androidbasearchitechture.StandardTestDispatcherRule()
    
    private val loginUseCase = LoginUseCase()
    
    @Test
    fun `login should emit success when credentials are valid`() = runTest {
        // Given
        val email = TestData.TEST_EMAIL
        val password = TestData.TEST_PASSWORD
        
        // When & Then
        loginUseCase(email, password).test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            
            val success = awaitItem()
            assertTrue(success.isSuccess)
            assertTrue(success.getDataOrNull()?.isSuccess == true)
            assertTrue(success.getDataOrNull()?.email == email)
        }
    }
    
    @Test
    fun `login should emit error when email is invalid`() = runTest {
        // Given
        val email = TestData.INVALID_EMAIL
        val password = TestData.TEST_PASSWORD
        
        // When & Then
        loginUseCase(email, password).test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            
            val error = awaitItem()
            assertTrue(error.isError)
            assertTrue(error.getErrorMessageOrNull()?.contains("Invalid email format") == true)
        }
    }
    
    @Test
    fun `login should emit error when password is too short`() = runTest {
        // Given
        val email = TestData.TEST_EMAIL
        val password = TestData.INVALID_PASSWORD
        
        // When & Then
        loginUseCase(email, password).test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            
            val error = awaitItem()
            assertTrue(error.isError)
            assertTrue(error.getErrorMessageOrNull()?.contains("Password must be at least") == true)
        }
    }
    
    @Test
    fun `login should emit error when email is blank`() = runTest {
        // Given
        val email = ""
        val password = TestData.TEST_PASSWORD
        
        // When & Then
        loginUseCase(email, password).test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            
            val error = awaitItem()
            assertTrue(error.isError)
            assertTrue(error.getErrorMessageOrNull()?.contains("Email is required") == true)
        }
    }
    
    @Test
    fun `login should emit error when password is blank`() = runTest {
        // Given
        val email = TestData.TEST_EMAIL
        val password = ""
        
        // When & Then
        loginUseCase(email, password).test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            
            val error = awaitItem()
            assertTrue(error.isError)
            assertTrue(error.getErrorMessageOrNull()?.contains("Password is required") == true)
        }
    }
}
