package dev.sonle.pdfscanner.core.di

import dev.sonle.pdfscanner.core.ads.AdConsentManager
import dev.sonle.pdfscanner.core.ads.AdMobInitializer
import dev.sonle.pdfscanner.core.ads.InterstitialAdController
import dev.sonle.pdfscanner.presentation.ads.AdUiStateHolder
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val adsModule = module {
    single { AdConsentManager(androidContext()) }
    single {
        AdMobInitializer(
            application = androidApplication(),
            adConsentManager = get(),
            ioDispatcher = get(named("IoDispatcher"))
        )
    }
    singleOf(::InterstitialAdController)
    single { AdUiStateHolder(get()) }
}
