package dev.sonle.androidbasearchitecture.data.local

import dev.sonle.androidbasearchitecture.data.model.UserEntity

import kotlinx.coroutines.flow.Flow

/**
 * Local data source for User operations using Room
 */
class UserLocalDataSource(
    private val userDao: UserDao
) {
    
    /**
     * Get all users as Flow
     */
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)
    
    /**
     * Get user by ID as Flow
     */
    fun getUserByIdFlow(id: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(id)
    
    /**
     * Get favorite users
     */
    fun getFavoriteUsers(): Flow<List<UserEntity>> = userDao.getFavoriteUsers()
    
    /**
     * Search users by name or email
     */
    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)
    
    /**
     * Insert user
     */
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    
    /**
     * Insert multiple users
     */
    suspend fun insertUsers(users: List<UserEntity>) = userDao.insertUsers(users)
    
    /**
     * Update user
     */
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    
    /**
     * Update user favorite status
     */
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) = 
        userDao.updateFavoriteStatus(id, isFavorite)
    
    /**
     * Delete user
     */
    suspend fun deleteUser(id: Long) = userDao.deleteUser(id)
    
    /**
     * Delete all users
     */
    suspend fun deleteAllUsers() = userDao.deleteAllUsers()
    
    /**
     * Get user count
     */
    suspend fun getUserCount(): Int = userDao.getUserCount()
    
    /**
     * Get favorite user count
     */
    suspend fun getFavoriteUserCount(): Int = userDao.getFavoriteUserCount()
}
