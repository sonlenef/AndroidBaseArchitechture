package dev.sonle.pdfscanner.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import timber.log.Timber

/**
 * Analytics manager for tracking user events and screen views
 */
class AnalyticsManager(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    
    /**
     * Log a screen view event
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        if (!EnvironmentConfig.analyticsEnabled) {
            Timber.d("Analytics disabled - would log screen view: $screenName")
            return
        }
        
        val bundle = android.os.Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        Timber.d("Logged screen view: $screenName")
    }
    
    /**
     * Log a user action event
     */
    fun logUserAction(action: String, parameters: Map<String, Any> = emptyMap()) {
        if (!EnvironmentConfig.analyticsEnabled) {
            Timber.d("Analytics disabled - would log user action: $action")
            return
        }
        
        val bundle = android.os.Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, action)
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Long -> putLong(key, value)
                    is Int -> putLong(key, value.toLong())
                    is Double -> putDouble(key, value)
                    is Float -> putDouble(key, value.toDouble())
                    is Boolean -> putString(key, value.toString())
                }
            }
        }
        
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        Timber.d("Logged user action: $action")
    }
    
    /**
     * Log a custom event
     */
    fun logEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        if (!EnvironmentConfig.analyticsEnabled) {
            Timber.d("Analytics disabled - would log event: $eventName")
            return
        }
        
        val bundle = android.os.Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Long -> putLong(key, value)
                    is Int -> putLong(key, value.toLong())
                    is Double -> putDouble(key, value)
                    is Float -> putDouble(key, value.toDouble())
                    is Boolean -> putString(key, value.toString())
                }
            }
        }
        
        firebaseAnalytics.logEvent(eventName, bundle)
        Timber.d("Logged custom event: $eventName")
    }
    
    /**
     * Set user properties
     */
    fun setUserProperty(name: String, value: String) {
        if (!EnvironmentConfig.analyticsEnabled) {
            Timber.d("Analytics disabled - would set user property: $name = $value")
            return
        }
        
        firebaseAnalytics.setUserProperty(name, value)
        Timber.d("Set user property: $name = $value")
    }
    
    /**
     * Set user ID
     */
    fun setUserId(userId: String) {
        if (!EnvironmentConfig.analyticsEnabled) {
            Timber.d("Analytics disabled - would set user ID: $userId")
            return
        }
        
        firebaseAnalytics.setUserId(userId)
        Timber.d("Set user ID: $userId")
    }
}
