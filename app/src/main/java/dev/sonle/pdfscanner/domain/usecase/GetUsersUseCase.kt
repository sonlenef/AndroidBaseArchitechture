package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.User
import dev.sonle.pdfscanner.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow


/**
 * Use case for getting all users
 */
class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    
    /**
     * Get all users
     */
    operator fun invoke(): Flow<List<User>> {
        return userRepository.getUsers()
    }
}
