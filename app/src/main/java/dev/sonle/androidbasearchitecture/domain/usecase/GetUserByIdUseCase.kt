package dev.sonle.androidbasearchitecture.domain.usecase

import dev.sonle.androidbasearchitecture.core.network.NetworkResult
import dev.sonle.androidbasearchitecture.domain.model.User
import dev.sonle.androidbasearchitecture.domain.repository.UserRepository
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
