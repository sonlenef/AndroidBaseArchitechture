package dev.sonle.androidbasearchitechture.domain.usecase

import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.core.util.Constants
import dev.sonle.androidbasearchitechture.core.util.ValidationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

/**
 * Use case for user login
 */
class LoginUseCase {
    
    /**
     * Login with email and password
     */
    operator fun invoke(email: String, password: String): Flow<NetworkResult<LoginResult>> = flow {
        try {
            emit(NetworkResult.Loading)
            
            // Validate input
            val validationError = validateCredentials(email, password)
            if (validationError != null) {
                emit(NetworkResult.Error(validationError))
                return@flow
            }
            
            // Simulate login process (replace with actual authentication)
            kotlinx.coroutines.delay(1000) // Simulate network delay
            
            // For demo purposes, accept any valid email/password combination
            if (isValidCredentials(email, password)) {
                val loginResult = LoginResult(
                    userId = 1L,
                    email = email,
                    name = "Demo User",
                    isSuccess = true
                )
                emit(NetworkResult.Success(loginResult))
            } else {
                emit(NetworkResult.Error("Invalid credentials"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            emit(NetworkResult.Error("Login failed: ${e.message}", e))
        }
    }
    
    /**
     * Validate login credentials
     */
    private fun validateCredentials(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !ValidationUtils.isValidEmail(email) -> "Invalid email format"
            password.isBlank() -> "Password is required"
            !ValidationUtils.isValidPassword(password) -> "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters"
            else -> null
        }
    }
    
    /**
     * Check if credentials are valid (demo implementation)
     */
    private fun isValidCredentials(email: String, password: String): Boolean {
        // Demo: accept any valid email with password length >= 6
        return ValidationUtils.isValidEmail(email) && password.length >= 6
    }
}

/**
 * Login result data class
 */
data class LoginResult(
    val userId: Long,
    val email: String,
    val name: String,
    val isSuccess: Boolean
)
