package dev.sonle.androidbasearchitecture.domain.repository

import dev.sonle.androidbasearchitecture.core.network.NetworkResult
import dev.sonle.androidbasearchitecture.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User operations
 */
interface UserRepository {
    
    /**
     * Get all users
     */
    fun getUsers(): Flow<List<User>>
    
    /**
     * Get user by ID
     */
    fun getUserById(id: Long): Flow<NetworkResult<User>>
    
    /**
     * Get favorite users
     */
    fun getFavoriteUsers(): Flow<List<User>>
    
    /**
     * Search users by name or email
     */
    fun searchUsers(query: String): Flow<List<User>>
    
    /**
     * Refresh users from remote source
     */
    suspend fun refreshUsers(): NetworkResult<List<User>>
    
    /**
     * Update user
     */
    suspend fun updateUser(user: User): NetworkResult<User>
    
    /**
     * Update user favorite status
     */
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): NetworkResult<Unit>
    
    /**
     * Delete user
     */
    suspend fun deleteUser(id: Long): NetworkResult<Unit>
    
    /**
     * Get user count
     */
    suspend fun getUserCount(): Int
    
    /**
     * Get favorite user count
     */
    suspend fun getFavoriteUserCount(): Int
}
