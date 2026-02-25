package dev.sonle.androidbasearchitechture.core.di

import dev.sonle.androidbasearchitechture.core.analytics.AnalyticsManager
import dev.sonle.androidbasearchitechture.core.config.RemoteConfigManager
import dev.sonle.androidbasearchitechture.core.crashlytics.CrashlyticsManager
import dev.sonle.androidbasearchitechture.core.fcm.NotificationHelper
import dev.sonle.androidbasearchitechture.core.performance.PerformanceMonitor
import dev.sonle.androidbasearchitechture.data.local.UserLocalDataSource
import dev.sonle.androidbasearchitechture.data.remote.UserRemoteDataSource
import dev.sonle.androidbasearchitechture.domain.usecase.GetUserByIdUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.GetUsersUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.LoginUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.RefreshUsersUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.UpdateFavoriteStatusUseCase
import dev.sonle.androidbasearchitechture.domain.usecase.UpdateUserUseCase
import dev.sonle.androidbasearchitechture.ui.features.login.LoginViewModel
import dev.sonle.androidbasearchitechture.ui.features.profile.ProfileViewModel
import dev.sonle.androidbasearchitechture.ui.features.userlist.UserListViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
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

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::UserListViewModel)
}
