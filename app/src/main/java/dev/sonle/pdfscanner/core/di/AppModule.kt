package dev.sonle.pdfscanner.core.di

import dev.sonle.pdfscanner.core.analytics.AnalyticsManager
import dev.sonle.pdfscanner.core.config.RemoteConfigManager
import dev.sonle.pdfscanner.core.crashlytics.CrashlyticsManager
import dev.sonle.pdfscanner.core.fcm.NotificationHelper
import dev.sonle.pdfscanner.core.performance.PerformanceMonitor
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveRecentScansUseCase
import dev.sonle.pdfscanner.domain.usecase.DeleteRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.AddRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.ClearAllScanDataUseCase
import dev.sonle.pdfscanner.domain.usecase.GetScanStorageInfoUseCase
import dev.sonle.pdfscanner.domain.usecase.ObserveAppSettingsUseCase
import dev.sonle.pdfscanner.domain.usecase.UpdateAppSettingsUseCase
import dev.sonle.pdfscanner.presentation.features.main.settings.SettingsViewModel
import dev.sonle.pdfscanner.presentation.features.main.home.HomeViewModel
import dev.sonle.pdfscanner.presentation.features.scanner.ScannerSaveCoordinator
import dev.sonle.pdfscanner.presentation.features.scanner.ScannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Main application module for Koin DI
 */
val appModule = module {
    // Managers / Helpers
    singleOf(::AnalyticsManager)
    singleOf(::CrashlyticsManager)
    singleOf(::RemoteConfigManager)
    singleOf(::PerformanceMonitor)
    singleOf(::NotificationHelper)

    // Use Cases
    factoryOf(::SavePdfUseCase)
    factoryOf(::ObserveRecentScansUseCase)
    factoryOf(::DeleteRecentScanUseCase)
    factoryOf(::AddRecentScanUseCase)
    factoryOf(::ObserveAppSettingsUseCase)
    factoryOf(::UpdateAppSettingsUseCase)
    factoryOf(::GetScanStorageInfoUseCase)
    factoryOf(::ClearAllScanDataUseCase)

    // Scanner coordinators
    factoryOf(::ScannerSaveCoordinator)

    // ViewModels
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ScannerViewModel)
}
