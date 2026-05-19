package dev.sonle.pdfscanner.core.di

import dev.sonle.pdfscanner.data.local.AppSettingsPreferences
import dev.sonle.pdfscanner.data.repository.AppSettingsRepositoryImpl
import dev.sonle.pdfscanner.data.repository.RecentScanRepositoryImpl
import dev.sonle.pdfscanner.data.repository.ScanStorageRepositoryImpl
import dev.sonle.pdfscanner.data.repository.ScannerRepositoryImpl
import dev.sonle.pdfscanner.domain.repository.AppSettingsRepository
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import dev.sonle.pdfscanner.domain.repository.ScanStorageRepository
import dev.sonle.pdfscanner.domain.repository.ScannerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Module providing repository implementations
 */
val repositoryModule = module {
    single { AppSettingsPreferences(androidContext()) }
    single { ScannerRepositoryImpl(androidContext()) } bind ScannerRepository::class
    single { RecentScanRepositoryImpl(get(), androidContext()) } bind RecentScanRepository::class
    single { AppSettingsRepositoryImpl(get()) } bind AppSettingsRepository::class
    single { ScanStorageRepositoryImpl(androidContext(), get()) } bind ScanStorageRepository::class
}
