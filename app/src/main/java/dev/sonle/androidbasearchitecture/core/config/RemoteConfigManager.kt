package dev.sonle.androidbasearchitecture.core.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dev.sonle.androidbasearchitecture.core.config.EnvironmentConfig
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Remote Config manager for feature flags and configuration
 */
class RemoteConfigManager(
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) {
    
    companion object {
        private const val DEFAULT_FETCH_TIMEOUT_SECONDS = 60L
        private const val MINIMUM_FETCH_INTERVAL_SECONDS = 3600L // 1 hour
    }
    
    /**
     * Initialize Remote Config with default values
     */
    suspend fun initialize() {
        if (!EnvironmentConfig.firebaseConfig.remoteConfigEnabled) {
            Timber.d("Remote Config disabled")
            return
        }
        
        try {
            // Set default values
            val defaults = mapOf(
                "feature_new_ui" to false,
                "feature_advanced_search" to false,
                "feature_dark_mode" to true,
                "api_timeout_seconds" to EnvironmentConfig.apiTimeoutSeconds,
                "max_items_per_page" to EnvironmentConfig.maxItemsPerPage,
                "enable_debug_logging" to EnvironmentConfig.enableDebugLogging,
                "app_version_required" to "1.0.0",
                "maintenance_mode" to false,
                "maintenance_message" to "The app is currently under maintenance. Please try again later."
            )
            
            firebaseRemoteConfig.setDefaultsAsync(defaults).await()
            
            // Set fetch timeout
            val configSettings = com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder()
                .setFetchTimeoutInSeconds(DEFAULT_FETCH_TIMEOUT_SECONDS)
                .setMinimumFetchIntervalInSeconds(
                    if (EnvironmentConfig.isDevelopment) 0L else MINIMUM_FETCH_INTERVAL_SECONDS
                )
                .build()
            
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings).await()
            
            Timber.d("Remote Config initialized with defaults")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Remote Config")
        }
    }
    
    /**
     * Fetch and activate remote config values
     */
    suspend fun fetchAndActivate(): Boolean {
        if (!EnvironmentConfig.firebaseConfig.remoteConfigEnabled) {
            Timber.d("Remote Config disabled - using defaults")
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
     * Get boolean value from Remote Config
     */
    fun getBoolean(key: String): Boolean {
        return firebaseRemoteConfig.getBoolean(key)
    }
    
    /**
     * Get string value from Remote Config
     */
    fun getString(key: String): String {
        return firebaseRemoteConfig.getString(key)
    }
    
    /**
     * Get long value from Remote Config
     */
    fun getLong(key: String): Long {
        return firebaseRemoteConfig.getLong(key)
    }
    
    /**
     * Get double value from Remote Config
     */
    fun getDouble(key: String): Double {
        return firebaseRemoteConfig.getDouble(key)
    }
    
    /**
     * Check if a feature is enabled
     */
    fun isFeatureEnabled(featureKey: String): Boolean {
        return getBoolean(featureKey)
    }
    
    /**
     * Get all feature flags
     */
    fun getAllFeatureFlags(): Map<String, Boolean> {
        return mapOf(
            "feature_new_ui" to getBoolean("feature_new_ui"),
            "feature_advanced_search" to getBoolean("feature_advanced_search"),
            "feature_dark_mode" to getBoolean("feature_dark_mode")
        )
    }
}
