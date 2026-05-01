package dev.sonle.androidbasearchitecture.domain.usecase

import dev.sonle.androidbasearchitecture.core.network.NetworkResult
import dev.sonle.androidbasearchitecture.core.util.ValidationUtils
import dev.sonle.androidbasearchitecture.domain.model.User
import dev.sonle.androidbasearchitecture.domain.repository.UserRepository


/**
 * Use case for updating user information
 */
class UpdateUserUseCase(
    private val userRepository: UserRepository
) {
    
    /**
     * Update user information
     */
    suspend operator fun invoke(user: User): NetworkResult<User> {
        // Validate user data
        val validationResult = validateUser(user)
        if (validationResult != null) {
            return NetworkResult.Error(validationResult)
        }
        
        return userRepository.updateUser(user)
    }
    
    /**
     * Validate user data
     */
    private fun validateUser(user: User): String? {
        return when {
            !ValidationUtils.isValidName(user.name) -> "Invalid name format"
            !ValidationUtils.isValidEmail(user.email) -> "Invalid email format"
            !ValidationUtils.isValidPhone(user.phone) -> "Invalid phone format"
            !ValidationUtils.isValidWebsite(user.website) -> "Invalid website format"
            else -> null
        }
    }
}
