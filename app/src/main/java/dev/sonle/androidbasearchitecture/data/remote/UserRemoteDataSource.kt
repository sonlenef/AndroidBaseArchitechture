package dev.sonle.androidbasearchitecture.data.remote

import dev.sonle.androidbasearchitecture.core.network.ApiService
import dev.sonle.androidbasearchitecture.core.network.NetworkResult
import dev.sonle.androidbasearchitecture.data.model.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
/**
 * Remote data source for User operations using Retrofit
 */
class UserRemoteDataSource(
    private val apiService: ApiService
) {
    
    /**
     * Get all users from API
     */
    fun getUsers(): Flow<NetworkResult<List<UserDto>>> = flow {
        try {
            emit(NetworkResult.Loading)
            val users = apiService.getUsers()
            emit(NetworkResult.Success(users))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching users from API")
            emit(NetworkResult.Error("Failed to fetch users: ${e.message}", e))
        }
    }
    
    /**
     * Get user by ID from API
     */
    fun getUserById(id: Long): Flow<NetworkResult<UserDto>> = flow {
        try {
            emit(NetworkResult.Loading)
            val user = apiService.getUserById(id)
            emit(NetworkResult.Success(user))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user $id from API")
            emit(NetworkResult.Error("Failed to fetch user: ${e.message}", e))
        }
    }
}
