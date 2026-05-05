package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.core.network.NetworkResult
import dev.sonle.pdfscanner.domain.model.User
import dev.sonle.pdfscanner.domain.repository.UserRepository


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
