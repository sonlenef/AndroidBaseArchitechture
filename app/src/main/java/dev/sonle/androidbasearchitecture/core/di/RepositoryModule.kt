package dev.sonle.androidbasearchitecture.core.di

import dev.sonle.androidbasearchitecture.data.repository.UserRepositoryImpl
import dev.sonle.androidbasearchitecture.domain.repository.UserRepository
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Module providing repository implementations
 */
val repositoryModule = module {
    single { UserRepositoryImpl(get(), get(), get(named("IoDispatcher"))) } bind UserRepository::class
}
