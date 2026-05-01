package dev.sonle.androidbasearchitecture.domain.usecase

import dev.sonle.androidbasearchitecture.domain.model.User
import dev.sonle.androidbasearchitecture.domain.repository.UserRepository
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
