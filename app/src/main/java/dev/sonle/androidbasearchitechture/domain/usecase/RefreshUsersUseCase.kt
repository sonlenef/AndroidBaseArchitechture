package dev.sonle.androidbasearchitechture.domain.usecase

import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.domain.model.User
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository


/**
 * Use case for refreshing users from remote source
 */
class RefreshUsersUseCase(
    private val userRepository: UserRepository
) {
    
    /**
     * Refresh users from remote source
     */
    suspend operator fun invoke(): NetworkResult<List<User>> {
        return userRepository.refreshUsers()
    }
}
