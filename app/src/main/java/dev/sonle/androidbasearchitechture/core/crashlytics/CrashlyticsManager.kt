package dev.sonle.androidbasearchitechture.core.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.sonle.androidbasearchitechture.core.config.EnvironmentConfig
import timber.log.Timber

/**
 * Crashlytics manager for crash reporting and custom logging
 */
class CrashlyticsManager(
    private val firebaseCrashlytics: FirebaseCrashlytics
) {
    
    /**
     * Set user ID for crash reporting
     */
    fun setUserId(userId: String) {
        if (!EnvironmentConfig.crashlyticsEnabled) {
            Timber.d("Crashlytics disabled - would set user ID: $userId")
            return
        }
        
        firebaseCrashlytics.setUserId(userId)
        Timber.d("Set Crashlytics user ID: $userId")
    }
    
    /**
     * Set custom key-value pair
     */
    fun setCustomKey(key: String, value: String) {
        if (!EnvironmentConfig.crashlyticsEnabled) {
            Timber.d("Crashlytics disabled - would set custom key: $key = $value")
            return
        }
        
        firebaseCrashlytics.setCustomKey(key, value)
        Timber.d("Set Crashlytics custom key: $key = $value")
    }
    
    /**
     * Set custom key-value pair with boolean value
     */
    fun setCustomKey(key: String, value: Boolean) {
        if (!EnvironmentConfig.crashlyticsEnabled) {
            Timber.d("Crashlytics disabled - would set custom key: $key = $value")
            return
        }
        
        firebaseCrashlytics.setCustomKey(key, value)
        Timber.d("Set Crashlytics custom key: $key = $value")
    }
    
    /**
     * Set custom key-value pair with number value
     */
    fun setCustomKey(key: String, value: Number) {
        if (!EnvironmentConfig.crashlyticsEnabled) {
            Timber.d("Crashlytics disabled - would set custom key: $key = $value")
            return
        }
        
        when (value) {
            is Int -> firebaseCrashlytics.setCustomKey(key, value)
            is Long -> firebaseCrashlytics.setCustomKey(key, value)
            is Float -> firebaseCrashlytics.setCustomKey(key, value)
            is Double -> firebaseCrashlytics.setCustomKey(key, value)
            else -> firebaseCrashlytics.setCustomKey(key, value.toString())
        }
        Timber.d("Set Crashlytics custom key: $key = $value")
    }
    
    /**
     * Record an exception
     */
    fun recordException(throwable: Throwable) {
        if (!EnvironmentConfig.crashlyticsEnabled) {
            Timber.d("Crashlytics disabled - would record exception: ${throwable.message}")
            return
        }
        
        firebaseCrashlytics.recordException(throwable)
        Timber.d("Recorded exception in Crashlytics: ${throwable.message}")
    }
    
    /**
     * Log a message
     */
    fun log(message: String) {
        if (!EnvironmentConfig.crashlyticsEnabled) {
            Timber.d("Crashlytics disabled - would log: $message")
            return
        }
        
        firebaseCrashlytics.log(message)
        Timber.d("Logged to Crashlytics: $message")
    }
    
    /**
     * Set custom keys for environment info
     */
    fun setEnvironmentInfo() {
        setCustomKey("environment", EnvironmentConfig.environment)
        setCustomKey("api_base_url", EnvironmentConfig.apiBaseUrl)
        setCustomKey("api_timeout_seconds", EnvironmentConfig.apiTimeoutSeconds)
        setCustomKey("max_items_per_page", EnvironmentConfig.maxItemsPerPage)
        setCustomKey("debug_logging_enabled", EnvironmentConfig.enableDebugLogging)
        setCustomKey("strict_mode_enabled", EnvironmentConfig.enableStrictMode)
    }
}
