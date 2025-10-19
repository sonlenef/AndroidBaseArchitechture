package dev.sonle.androidbasearchitechture.domain.usecase

import dev.sonle.androidbasearchitechture.domain.model.User
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all users
 */
class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    /**
     * Get all users
     */
    operator fun invoke(): Flow<List<User>> {
        return userRepository.getUsers()
    }
}
