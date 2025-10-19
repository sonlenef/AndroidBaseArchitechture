package dev.sonle.androidbasearchitechture.core.network

import dev.sonle.androidbasearchitechture.data.model.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for API calls
 */
interface ApiService {
    
    /**
     * Get all users
     */
    @GET("users")
    suspend fun getUsers(): List<UserDto>
    
    /**
     * Get user by ID
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserDto
}
