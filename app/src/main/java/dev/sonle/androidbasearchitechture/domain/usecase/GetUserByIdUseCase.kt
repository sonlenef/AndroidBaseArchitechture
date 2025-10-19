package dev.sonle.androidbasearchitechture.domain.usecase

import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.domain.model.User
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a user by ID
 */
class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    /**
     * Get user by ID
     */
    operator fun invoke(id: Long): Flow<NetworkResult<User>> {
        return userRepository.getUserById(id)
    }
}
