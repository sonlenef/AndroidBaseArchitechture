package dev.sonle.androidbasearchitecture.domain.usecase

import dev.sonle.androidbasearchitecture.core.network.NetworkResult
import dev.sonle.androidbasearchitecture.domain.repository.UserRepository


/**
 * Use case for updating user favorite status
 */
class UpdateFavoriteStatusUseCase(
    private val userRepository: UserRepository
) {
    
    /**
     * Update user favorite status
     */
    suspend operator fun invoke(id: Long, isFavorite: Boolean): NetworkResult<Unit> {
        return userRepository.updateFavoriteStatus(id, isFavorite)
    }
}
