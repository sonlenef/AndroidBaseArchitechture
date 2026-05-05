package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.core.network.NetworkResult
import dev.sonle.pdfscanner.domain.model.User
import dev.sonle.pdfscanner.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow


/**
 * Use case for getting a user by ID
 */
class GetUserByIdUseCase(
    private val userRepository: UserRepository
) {
    
    /**
     * Get user by ID
     */
    operator fun invoke(id: Long): Flow<NetworkResult<User>> {
        return userRepository.getUserById(id)
    }
}
