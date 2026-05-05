package dev.sonle.pdfscanner.core.di

import dev.sonle.pdfscanner.core.analytics.AnalyticsManager
import dev.sonle.pdfscanner.core.config.RemoteConfigManager
import dev.sonle.pdfscanner.core.crashlytics.CrashlyticsManager
import dev.sonle.pdfscanner.core.fcm.NotificationHelper
import dev.sonle.pdfscanner.core.performance.PerformanceMonitor
import dev.sonle.pdfscanner.data.local.UserLocalDataSource
import dev.sonle.pdfscanner.data.remote.UserRemoteDataSource
import dev.sonle.pdfscanner.domain.usecase.GetUserByIdUseCase
import dev.sonle.pdfscanner.domain.usecase.GetUsersUseCase
import dev.sonle.pdfscanner.domain.usecase.LoginUseCase
import dev.sonle.pdfscanner.domain.usecase.RefreshUsersUseCase
import dev.sonle.pdfscanner.domain.usecase.UpdateFavoriteStatusUseCase
import dev.sonle.pdfscanner.domain.usecase.UpdateUserUseCase
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import dev.sonle.pdfscanner.presentation.features.login.LoginViewModel
import dev.sonle.pdfscanner.presentation.features.profile.ProfileViewModel
import dev.sonle.pdfscanner.presentation.features.userlist.UserListViewModel
import dev.sonle.pdfscanner.presentation.features.scanner.ScannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import dev.sonle.pdfscanner.core.ai.GeminiNanoManager
import dev.sonle.pdfscanner.core.ai.OnDeviceAiManager
import dev.sonle.pdfscanner.domain.usecase.ai.SummarizeUserUseCase
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
    factoryOf(::SavePdfUseCase)


    // AI
    single<OnDeviceAiManager> { GeminiNanoManager(androidContext()) }
    factoryOf(::SummarizeUserUseCase)

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::UserListViewModel)
    viewModelOf(::ScannerViewModel)
}
