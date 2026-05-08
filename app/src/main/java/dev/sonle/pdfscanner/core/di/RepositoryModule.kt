package dev.sonle.pdfscanner.core.di

import dev.sonle.pdfscanner.data.repository.RecentScanRepositoryImpl
import dev.sonle.pdfscanner.data.repository.UserRepositoryImpl
import dev.sonle.pdfscanner.data.repository.ScannerRepositoryImpl
import dev.sonle.pdfscanner.domain.repository.RecentScanRepository
import dev.sonle.pdfscanner.domain.repository.UserRepository
import dev.sonle.pdfscanner.domain.repository.ScannerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Module providing repository implementations
 */
val repositoryModule = module {
    single { UserRepositoryImpl(get(), get(), get(named("IoDispatcher"))) } bind UserRepository::class
    single { ScannerRepositoryImpl(androidContext()) } bind ScannerRepository::class
    single { RecentScanRepositoryImpl(get()) } bind RecentScanRepository::class
}
