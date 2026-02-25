package dev.sonle.androidbasearchitechture.data.repository

import dev.sonle.androidbasearchitechture.core.network.NetworkResult
import dev.sonle.androidbasearchitechture.data.local.UserLocalDataSource
import dev.sonle.androidbasearchitechture.data.mapper.UserMapper.toDomain
import dev.sonle.androidbasearchitechture.data.mapper.UserMapper.toDomainFromEntity
import dev.sonle.androidbasearchitechture.data.mapper.UserMapper.toEntity
import dev.sonle.androidbasearchitechture.data.mapper.UserMapper.toEntityFromDto
import dev.sonle.androidbasearchitechture.data.remote.UserRemoteDataSource
import dev.sonle.androidbasearchitechture.domain.model.User
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository implementation for User operations
 * Implements offline-first strategy
 */
class UserRepositoryImpl(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : UserRepository {
    
    override fun getUsers(): Flow<List<User>> {
        return localDataSource.getAllUsers().map { entities ->
            entities.toDomainFromEntity()
        }
    }
    
    override fun getUserById(id: Long): Flow<NetworkResult<User>> = flow {
        try {
            // First try to get from local cache
            val localUser = localDataSource.getUserById(id)
            if (localUser != null) {
                emit(NetworkResult.Success(localUser.toDomain()))
            } else {
                // If not found locally, try remote
                remoteDataSource.getUserById(id).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            val userEntity = result.data.toEntity()
                            localDataSource.insertUser(userEntity)
                            emit(NetworkResult.Success(result.data.toDomain()))
                        }
                        is NetworkResult.Error -> emit(result)
                        is NetworkResult.Loading -> emit(result)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user by ID: $id")
            emit(NetworkResult.Error("Failed to get user: ${e.message}", e))
        }
    }
    
    override fun getFavoriteUsers(): Flow<List<User>> {
        return localDataSource.getFavoriteUsers().map { entities ->
            entities.toDomainFromEntity()
        }
    }
    
    override fun searchUsers(query: String): Flow<List<User>> {
        return localDataSource.searchUsers(query).map { entities ->
            entities.toDomainFromEntity()
        }
    }
    
    override suspend fun refreshUsers(): NetworkResult<List<User>> = withContext(ioDispatcher) {
        try {
            var result: NetworkResult<List<User>> = NetworkResult.Loading
            
            remoteDataSource.getUsers().collect { networkResult ->
                when (networkResult) {
                    is NetworkResult.Success -> {
                        val users = networkResult.data.toDomain()
                        val entities = networkResult.data.toEntityFromDto()
                        localDataSource.insertUsers(entities)
                        result = NetworkResult.Success(users)
                    }
                    is NetworkResult.Error -> {
                        result = networkResult
                    }
                    is NetworkResult.Loading -> {
                        // Keep loading state
                    }
                }
            }
            
            result
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing users")
            NetworkResult.Error("Failed to refresh users: ${e.message}", e)
        }
    }
    
    override suspend fun updateUser(user: User): NetworkResult<User> = withContext(ioDispatcher) {
        try {
            val entity = user.toEntity()
            localDataSource.updateUser(entity)
            NetworkResult.Success(user)
        } catch (e: Exception) {
            Timber.e(e, "Error updating user: ${user.id}")
            NetworkResult.Error("Failed to update user: ${e.message}", e)
        }
    }
    
    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): NetworkResult<Unit> = withContext(ioDispatcher) {
        try {
            localDataSource.updateFavoriteStatus(id, isFavorite)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating favorite status for user: $id")
            NetworkResult.Error("Failed to update favorite status: ${e.message}", e)
        }
    }
    
    override suspend fun deleteUser(id: Long): NetworkResult<Unit> = withContext(ioDispatcher) {
        try {
            localDataSource.deleteUser(id)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting user: $id")
            NetworkResult.Error("Failed to delete user: ${e.message}", e)
        }
    }
    
    override suspend fun getUserCount(): Int = withContext(ioDispatcher) {
        try {
            localDataSource.getUserCount()
        } catch (e: Exception) {
            Timber.e(e, "Error getting user count")
            0
        }
    }
    
    override suspend fun getFavoriteUserCount(): Int = withContext(ioDispatcher) {
        try {
            localDataSource.getFavoriteUserCount()
        } catch (e: Exception) {
            Timber.e(e, "Error getting favorite user count")
            0
        }
    }
}
