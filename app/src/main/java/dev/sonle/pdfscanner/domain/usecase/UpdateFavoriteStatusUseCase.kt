package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.core.network.NetworkResult
import dev.sonle.pdfscanner.domain.repository.UserRepository


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
