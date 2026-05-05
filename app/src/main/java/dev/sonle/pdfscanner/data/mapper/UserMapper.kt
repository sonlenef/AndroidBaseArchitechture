package dev.sonle.pdfscanner.data.mapper

import dev.sonle.pdfscanner.data.model.UserDto
import dev.sonle.pdfscanner.data.model.UserEntity
import dev.sonle.pdfscanner.domain.model.User

/**
 * Mapper functions for User data transformation
 */
object UserMapper {
    
    /**
     * Convert UserDto to User domain model
     */
    fun UserDto.toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email,
            phone = phone,
            website = website,
            username = username,
            isFavorite = false
        )
    }
    
    /**
     * Convert UserEntity to User domain model
     */
    fun UserEntity.toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email,
            phone = phone,
            website = website,
            username = username,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    /**
     * Convert User domain model to UserEntity
     */
    fun User.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            phone = phone,
            website = website,
            username = username,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    /**
     * Convert UserDto to UserEntity
     */
    fun UserDto.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            phone = phone,
            website = website,
            username = username,
            isFavorite = false
        )
    }
    
    /**
     * Convert list of UserDto to list of User domain models
     */
    fun List<UserDto>.toDomain(): List<User> {
        return map { it.toDomain() }
    }
    
    /**
     * Convert list of UserEntity to list of User domain models
     */
    fun List<UserEntity>.toDomainFromEntity(): List<User> {
        return map { it.toDomain() }
    }
    
    /**
     * Convert list of User domain models to list of UserEntity
     */
    fun List<User>.toEntity(): List<UserEntity> {
        return map { it.toEntity() }
    }
    
    /**
     * Convert list of UserDto to list of UserEntity
     */
    fun List<UserDto>.toEntityFromDto(): List<UserEntity> {
        return map { it.toEntity() }
    }
}
