package dev.sonle.androidbasearchitechture.data.local

import androidx.room.*
import dev.sonle.androidbasearchitechture.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User operations
 */
@Dao
interface UserDao {
    
    /**
     * Get all users as Flow
     */
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    /**
     * Get user by ID
     */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?
    
    /**
     * Get user by ID as Flow
     */
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>
    
    /**
     * Get favorite users
     */
    @Query("SELECT * FROM users WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteUsers(): Flow<List<UserEntity>>
    
    /**
     * Search users by name or email
     */
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchUsers(query: String): Flow<List<UserEntity>>
    
    /**
     * Insert user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    /**
     * Insert multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    /**
     * Update user
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * Update user favorite status
     */
    @Query("UPDATE users SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Delete user
     */
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)
    
    /**
     * Delete all users
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    /**
     * Get user count
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    /**
     * Get favorite user count
     */
    @Query("SELECT COUNT(*) FROM users WHERE isFavorite = 1")
    suspend fun getFavoriteUserCount(): Int
}
