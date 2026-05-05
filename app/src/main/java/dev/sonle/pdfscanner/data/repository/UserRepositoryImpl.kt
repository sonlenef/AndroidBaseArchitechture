package dev.sonle.pdfscanner.data.repository

import dev.sonle.pdfscanner.core.network.NetworkResult
import dev.sonle.pdfscanner.data.local.UserLocalDataSource
import dev.sonle.pdfscanner.data.mapper.UserMapper.toDomain
import dev.sonle.pdfscanner.data.mapper.UserMapper.toDomainFromEntity
import dev.sonle.pdfscanner.data.mapper.UserMapper.toEntity
import dev.sonle.pdfscanner.data.mapper.UserMapper.toEntityFromDto
import dev.sonle.pdfscanner.data.remote.UserRemoteDataSource
import dev.sonle.pdfscanner.domain.model.User
import dev.sonle.pdfscanner.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
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
            // Check if local data exists first
            val localUser = localDataSource.getUserById(id)
            
            if (localUser == null) {
                emit(NetworkResult.Loading)
                // Fetch from remote if not found locally
                remoteDataSource.getUserById(id).collect { result ->
                    if (result is NetworkResult.Success) {
                        localDataSource.insertUser(result.data.toEntity())
                    }
                }
            }
            
            // Observe local database as the single source of truth
            emitAll(
                localDataSource.getUserByIdFlow(id).map { entity ->
                    if (entity != null) {
                        NetworkResult.Success(entity.toDomain())
                    } else {
                        NetworkResult.Error("User not found")
                    }
                }
            )
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
