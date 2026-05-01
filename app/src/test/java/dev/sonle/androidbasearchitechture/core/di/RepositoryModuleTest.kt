package dev.sonle.androidbasearchitechture.core.di

import dev.sonle.androidbasearchitechture.data.local.UserLocalDataSource
import dev.sonle.androidbasearchitechture.data.remote.UserRemoteDataSource
import dev.sonle.androidbasearchitechture.domain.repository.UserRepository
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class RepositoryModuleTest {

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `repository module should resolve UserRepository with IoDispatcher`() {
        val testDataModule: Module = module {
            single { mockk<UserLocalDataSource>(relaxed = true) }
            single { mockk<UserRemoteDataSource>(relaxed = true) }
        }

        val koinApp = startKoin {
            modules(
                dispatcherModule,
                repositoryModule,
                testDataModule
            )
        }

        val userRepository = koinApp.koin.get<UserRepository>()
        assertNotNull(userRepository)
    }
}
