package dev.sonle.pdfscanner

import android.app.Application
import android.os.StrictMode
import dev.sonle.pdfscanner.core.analytics.AnalyticsManager
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import dev.sonle.pdfscanner.core.config.RemoteConfigManager
import dev.sonle.pdfscanner.core.crashlytics.CrashlyticsManager
import dev.sonle.pdfscanner.core.di.adsModule
import dev.sonle.pdfscanner.core.di.appModule
import dev.sonle.pdfscanner.core.di.databaseModule
import dev.sonle.pdfscanner.core.di.dispatcherModule
import dev.sonle.pdfscanner.core.di.firebaseModule
import dev.sonle.pdfscanner.core.di.navigationModule
import dev.sonle.pdfscanner.core.di.repositoryModule
import dev.sonle.pdfscanner.core.di.scannerModule
import dev.sonle.pdfscanner.core.firebase.FirebaseInitializer
import dev.sonle.pdfscanner.presentation.ads.AdUiStateHolder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.opencv.android.OpenCVLoader
import timber.log.Timber

class MyApplication : Application() {

    private val remoteConfigManager: RemoteConfigManager by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val crashlyticsManager: CrashlyticsManager by inject()
    private val adUiStateHolder: AdUiStateHolder by inject()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Completes after Remote Config defaults + fetch; MainActivity awaits before AdMob init. */
    private val firebaseManagersReady = CompletableDeferred<Unit>()

    suspend fun awaitFirebaseManagersReady() {
        firebaseManagersReady.await()
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(
                appModule,
                databaseModule,
                dispatcherModule,
                firebaseModule,
                adsModule,
                navigationModule,
                repositoryModule,
                scannerModule
            )
        }

        configureEnvironment()
        initializeLogging()

        val firebaseInitializer = FirebaseInitializer(
            application = this,
            remoteConfigManager = remoteConfigManager,
            analyticsManager = analyticsManager,
            crashlyticsManager = crashlyticsManager
        )
        firebaseInitializer.initialize()

        // Load XML defaults before any screen reads feature flags (avoids adsEnabled=false race).
        runBlocking(Dispatchers.IO) {
            remoteConfigManager.initialize()
        }

        applicationScope.launch {
            try {
                firebaseInitializer.initializeManagers()
                adUiStateHolder.refresh()
            } finally {
                if (!firebaseManagersReady.isCompleted) {
                    firebaseManagersReady.complete(Unit)
                }
            }
        }

        initializeOpenCV()
    }

    private fun initializeOpenCV() {
        if (OpenCVLoader.initDebug()) {
            Timber.d("OpenCV initialized successfully")
        } else {
            Timber.e("OpenCV initialization failed")
        }
    }

    private fun configureEnvironment() {
        if (EnvironmentConfig.enableStrictMode) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    private fun initializeLogging() {
        if (EnvironmentConfig.enableDebugLogging) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("Application started in ${EnvironmentConfig.environment} environment")
    }
}
