package dev.sonle.pdfscanner.core.config

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.config.FeatureFlags.Keys
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Remote Config manager for feature flags and dynamic configuration.
 */
class RemoteConfigManager(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val firebasePerformance: FirebasePerformance
) {

    /**
     * Initialize Remote Config with flavor-specific XML defaults.
     */
    suspend fun initialize() {
        if (!EnvironmentConfig.firebaseConfig.remoteConfigEnabled) {
            Timber.d("Remote Config disabled")
            return
        }

        try {
            firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await()

            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setFetchTimeoutInSeconds(DEFAULT_FETCH_TIMEOUT_SECONDS)
                .setMinimumFetchIntervalInSeconds(
                    if (EnvironmentConfig.isDevelopment) 0L else MINIMUM_FETCH_INTERVAL_SECONDS
                )
                .build()

            firebaseRemoteConfig.setConfigSettingsAsync(configSettings).await()
            Timber.d("Remote Config initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Remote Config")
        }
    }

    /**
     * Fetch and activate remote config values from Firebase.
     */
    suspend fun fetchAndActivate(): Boolean {
        if (!EnvironmentConfig.firebaseConfig.remoteConfigEnabled) {
            Timber.d("Remote Config disabled — using defaults")
            return true
        }

        return try {
            val success = firebaseRemoteConfig.fetchAndActivate().await()
            Timber.d("Remote Config fetch and activate: $success")
            success
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch Remote Config")
            false
        }
    }

    /**
     * Applies Remote Config toggles for Analytics, Crashlytics, and Performance.
     * Build-time flags must be enabled first; Remote Config can only turn collection off.
     */
    fun applyRemoteServiceFlags() {
        if (!EnvironmentConfig.firebaseConfig.remoteConfigEnabled) return

        if (EnvironmentConfig.analyticsEnabled) {
            val enabled = firebaseRemoteConfig.getBoolean(Keys.ENABLE_ANALYTICS)
            firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
            Timber.d("Analytics collection (remote): $enabled")
        }

        if (EnvironmentConfig.crashlyticsEnabled) {
            val enabled = firebaseRemoteConfig.getBoolean(Keys.ENABLE_CRASHLYTICS)
            firebaseCrashlytics.isCrashlyticsCollectionEnabled = enabled
            Timber.d("Crashlytics collection (remote): $enabled")
        }

        if (EnvironmentConfig.performanceMonitoringEnabled) {
            val enabled = firebaseRemoteConfig.getBoolean(Keys.ENABLE_PERFORMANCE_MONITORING)
            firebasePerformance.isPerformanceCollectionEnabled = enabled
            Timber.d("Performance collection (remote): $enabled")
        }
    }

    fun getBoolean(key: String): Boolean = firebaseRemoteConfig.getBoolean(key)

    fun getString(key: String): String = firebaseRemoteConfig.getString(key)

    fun getLong(key: String): Long = firebaseRemoteConfig.getLong(key)

    fun getDouble(key: String): Double = firebaseRemoteConfig.getDouble(key)

    fun isFeatureEnabled(featureKey: String): Boolean = getBoolean(featureKey)

    fun getAllFeatureFlags(): Map<String, Boolean> =
        mapOf(
            Keys.FEATURE_NEW_UI to getBoolean(Keys.FEATURE_NEW_UI),
            Keys.FEATURE_ADVANCED_SEARCH to getBoolean(Keys.FEATURE_ADVANCED_SEARCH),
            Keys.FEATURE_DARK_MODE to getBoolean(Keys.FEATURE_DARK_MODE),
            Keys.FEATURE_OFFLINE_MODE to getBoolean(Keys.FEATURE_OFFLINE_MODE),
            Keys.FEATURE_BIOMETRIC_AUTH to getBoolean(Keys.FEATURE_BIOMETRIC_AUTH)
        )

    companion object {
        private const val DEFAULT_FETCH_TIMEOUT_SECONDS = 60L
        private const val MINIMUM_FETCH_INTERVAL_SECONDS = 3600L
    }
}
