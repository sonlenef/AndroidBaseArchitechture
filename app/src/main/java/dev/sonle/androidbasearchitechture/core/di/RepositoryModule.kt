package dev.sonle.androidbasearchitechture.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sonle.androidbasearchitechture.data.repository.UserRepositoryImpl
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository
import javax.inject.Singleton

/**
 * Module providing repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
