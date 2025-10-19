package dev.sonle.androidbasearchitechture.domain.model

/**
 * Domain model for User
 */
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val website: String,
    val username: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
