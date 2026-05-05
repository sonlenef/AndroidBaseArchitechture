package dev.sonle.pdfscanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for User
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
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
