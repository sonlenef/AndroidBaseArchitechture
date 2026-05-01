package dev.sonle.androidbasearchitecture.domain.usecase

import dev.sonle.androidbasearchitecture.core.network.NetworkResult
import dev.sonle.androidbasearchitecture.domain.model.User
import dev.sonle.androidbasearchitecture.domain.repository.UserRepository


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
