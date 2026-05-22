package dev.sonle.pdfscanner.core.firebase

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dev.sonle.pdfscanner.BuildConfig
import dev.sonle.pdfscanner.core.analytics.AnalyticsManager
import dev.sonle.pdfscanner.core.analytics.FirebaseAnalyticsEvents
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import dev.sonle.pdfscanner.core.config.RemoteConfigManager
import dev.sonle.pdfscanner.core.crashlytics.CrashlyticsManager
import timber.log.Timber

/**
 * Bootstraps Firebase SDK and app-level managers (Remote Config, Analytics, Crashlytics).
 */
class FirebaseInitializer(
    private val application: Application,
    private val remoteConfigManager: RemoteConfigManager,
    private val analyticsManager: AnalyticsManager,
    private val crashlyticsManager: CrashlyticsManager
) {

    fun initialize() {
        if (FirebaseApp.getApps(application).isEmpty()) {
            FirebaseApp.initializeApp(application)
        }
        configureCollectionFlags()
        installUncaughtExceptionHandler()
    }

    suspend fun initializeManagers() {
        try {
            remoteConfigManager.initialize()
            remoteConfigManager.fetchAndActivate()
            remoteConfigManager.applyRemoteServiceFlags()

            crashlyticsManager.setEnvironmentInfo()
            crashlyticsManager.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlyticsManager.setCustomKey("version_code", BuildConfig.VERSION_CODE)

            analyticsManager.logEvent(
                FirebaseAnalyticsEvents.APP_STARTED,
                mapOf(
                    FirebaseAnalyticsEvents.PARAM_ENVIRONMENT to EnvironmentConfig.environment,
                    FirebaseAnalyticsEvents.PARAM_VERSION to BuildConfig.VERSION_NAME
                )
            )

            Timber.d("Firebase managers initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Firebase managers")
            crashlyticsManager.recordException(e)
        }
    }

    private fun configureCollectionFlags() {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled =
            EnvironmentConfig.crashlyticsEnabled

        FirebasePerformance.getInstance().isPerformanceCollectionEnabled =
            EnvironmentConfig.performanceMonitoringEnabled

        analyticsManager.setCollectionEnabled(EnvironmentConfig.analyticsEnabled)

        Timber.d(
            "Firebase collection flags — Crashlytics: ${EnvironmentConfig.crashlyticsEnabled}, " +
                "Analytics: ${EnvironmentConfig.analyticsEnabled}, " +
                "Performance: ${EnvironmentConfig.performanceMonitoringEnabled}"
        )
    }

    private fun installUncaughtExceptionHandler() {
        if (!EnvironmentConfig.crashlyticsEnabled) return

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            crashlyticsManager.log("Uncaught exception on thread: ${thread.name}")
            crashlyticsManager.recordException(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
