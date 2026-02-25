package dev.sonle.androidbasearchitechture.core.di

import dev.sonle.androidbasearchitechture.data.repository.UserRepositoryImpl
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Module providing repository implementations
 */
val repositoryModule = module {
    singleOf(::UserRepositoryImpl) bind UserRepository::class
}
