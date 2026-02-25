package dev.sonle.androidbasearchitechture.domain.usecase

import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository


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
