package dev.sonle.androidbasearchitecture.core.config

import dev.sonle.androidbasearchitecture.BuildConfig

/**
 * Environment configuration utility that provides environment-specific settings
 * based on the current build variant.
 */
object EnvironmentConfig {
    
    /**
     * Current environment name
     */
    val environment: String = BuildConfig.ENVIRONMENT
    
    /**
     * API base URL for the current environment
     */
    val apiBaseUrl: String = BuildConfig.API_BASE_URL
    
    /**
     * API timeout in seconds
     */
    val apiTimeoutSeconds: Int = BuildConfig.API_TIMEOUT_SECONDS
    
    /**
     * Maximum items per page for pagination
     */
    val maxItemsPerPage: Int = BuildConfig.MAX_ITEMS_PER_PAGE
    
    /**
     * Whether debug logging is enabled
     */
    val enableDebugLogging: Boolean = BuildConfig.ENABLE_DEBUG_LOGGING
    
    /**
     * Whether strict mode is enabled
     */
    val enableStrictMode: Boolean = BuildConfig.ENABLE_STRICT_MODE
    
    /**
     * Whether Crashlytics is enabled
     */
    val crashlyticsEnabled: Boolean = BuildConfig.ENABLE_CRASHLYTICS
    
    /**
     * Whether Analytics is enabled
     */
    val analyticsEnabled: Boolean = BuildConfig.ENABLE_ANALYTICS
    
    /**
     * Whether Performance Monitoring is enabled
     */
    val performanceMonitoringEnabled: Boolean = BuildConfig.ENABLE_PERFORMANCE_MONITORING
    
    /**
     * Whether this is the development environment
     */
    val isDevelopment: Boolean = environment == "development"
    
    /**
     * Whether this is the staging environment
     */
    val isStaging: Boolean = environment == "staging"
    
    /**
     * Whether this is the production environment
     */
    val isProduction: Boolean = environment == "production"
    
    /**
     * Database name for the current environment
     */
    val databaseName: String = "baseapp_${environment}_db"
    
    /**
     * Shared preferences name for the current environment
     */
    val preferencesName: String = "baseapp_${environment}_prefs"
    
    /**
     * Firebase project configuration based on environment
     */
    val firebaseConfig = FirebaseConfig(
        crashlyticsEnabled = crashlyticsEnabled,
        analyticsEnabled = analyticsEnabled,
        performanceMonitoringEnabled = performanceMonitoringEnabled,
        fcmEnabled = isProduction || isStaging,
        firebaseAuthEnabled = true,
        firestoreEnabled = true,
        firebaseStorageEnabled = true,
        remoteConfigEnabled = true
    )
}

/**
 * Firebase configuration for the current environment
 */
data class FirebaseConfig(
    val crashlyticsEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val performanceMonitoringEnabled: Boolean,
    val fcmEnabled: Boolean,
    val firebaseAuthEnabled: Boolean,
    val firestoreEnabled: Boolean,
    val firebaseStorageEnabled: Boolean,
    val remoteConfigEnabled: Boolean
)
