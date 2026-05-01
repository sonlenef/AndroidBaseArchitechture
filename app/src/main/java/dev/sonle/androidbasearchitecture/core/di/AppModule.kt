package dev.sonle.androidbasearchitecture.core.di

import dev.sonle.androidbasearchitecture.core.analytics.AnalyticsManager
import dev.sonle.androidbasearchitecture.core.config.RemoteConfigManager
import dev.sonle.androidbasearchitecture.core.crashlytics.CrashlyticsManager
import dev.sonle.androidbasearchitecture.core.fcm.NotificationHelper
import dev.sonle.androidbasearchitecture.core.performance.PerformanceMonitor
import dev.sonle.androidbasearchitecture.data.local.UserLocalDataSource
import dev.sonle.androidbasearchitecture.data.remote.UserRemoteDataSource
import dev.sonle.androidbasearchitecture.domain.usecase.GetUserByIdUseCase
import dev.sonle.androidbasearchitecture.domain.usecase.GetUsersUseCase
import dev.sonle.androidbasearchitecture.domain.usecase.LoginUseCase
import dev.sonle.androidbasearchitecture.domain.usecase.RefreshUsersUseCase
import dev.sonle.androidbasearchitecture.domain.usecase.UpdateFavoriteStatusUseCase
import dev.sonle.androidbasearchitecture.domain.usecase.UpdateUserUseCase
import dev.sonle.androidbasearchitecture.presentation.features.login.LoginViewModel
import dev.sonle.androidbasearchitecture.presentation.features.profile.ProfileViewModel
import dev.sonle.androidbasearchitecture.presentation.features.userlist.UserListViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import dev.sonle.androidbasearchitecture.core.ai.GeminiNanoManager
import dev.sonle.androidbasearchitecture.core.ai.OnDeviceAiManager
import dev.sonle.androidbasearchitecture.domain.usecase.ai.SummarizeUserUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Main application module for Koin DI
 */
val appModule = module {
    // Data Sources
    singleOf(::UserLocalDataSource)
    singleOf(::UserRemoteDataSource)

    // Managers / Helpers
    singleOf(::AnalyticsManager)
    singleOf(::CrashlyticsManager)
    singleOf(::RemoteConfigManager)
    singleOf(::PerformanceMonitor)
    singleOf(::NotificationHelper)

    // Use Cases
    factoryOf(::GetUserByIdUseCase)
    factoryOf(::GetUsersUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::RefreshUsersUseCase)
    factoryOf(::UpdateFavoriteStatusUseCase)
    factoryOf(::UpdateUserUseCase)


    // AI
    single<OnDeviceAiManager> { GeminiNanoManager(androidContext()) }
    factoryOf(::SummarizeUserUseCase)

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::UserListViewModel)
}
