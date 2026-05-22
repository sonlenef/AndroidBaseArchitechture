package dev.sonle.pdfscanner.core.config

/**
 * Typed accessors for Firebase Remote Config feature flags and app config.
 */
class FeatureFlags(
    private val remoteConfigManager: RemoteConfigManager
) {
    object Keys {
        const val FEATURE_NEW_UI = "feature_new_ui"
        const val FEATURE_ADVANCED_SEARCH = "feature_advanced_search"
        const val FEATURE_DARK_MODE = "feature_dark_mode"
        const val FEATURE_OFFLINE_MODE = "feature_offline_mode"
        const val FEATURE_BIOMETRIC_AUTH = "feature_biometric_auth"
        const val MAINTENANCE_MODE = "maintenance_mode"
        const val MAINTENANCE_MESSAGE = "maintenance_message"
        const val ENABLE_ANALYTICS = "enable_analytics"
        const val ENABLE_CRASHLYTICS = "enable_crashlytics"
        const val ENABLE_PERFORMANCE_MONITORING = "enable_performance_monitoring"
        const val ENABLE_ADS = "enable_ads"
        const val ENABLE_BANNER_ADS = "enable_banner_ads"
        const val ENABLE_INTERSTITIAL_ADS = "enable_interstitial_ads"
        const val INTERSTITIAL_COOLDOWN_SECONDS = "interstitial_cooldown_seconds"
        const val API_TIMEOUT_SECONDS = "api_timeout_seconds"
        const val MAX_ITEMS_PER_PAGE = "max_items_per_page"
    }

    fun isNewUiEnabled(): Boolean = remoteConfigManager.getBoolean(Keys.FEATURE_NEW_UI)

    fun isAdvancedSearchEnabled(): Boolean =
        remoteConfigManager.getBoolean(Keys.FEATURE_ADVANCED_SEARCH)

    fun isDarkModeFeatureEnabled(): Boolean =
        remoteConfigManager.getBoolean(Keys.FEATURE_DARK_MODE)

    fun isOfflineModeEnabled(): Boolean =
        remoteConfigManager.getBoolean(Keys.FEATURE_OFFLINE_MODE)

    fun isBiometricAuthEnabled(): Boolean =
        remoteConfigManager.getBoolean(Keys.FEATURE_BIOMETRIC_AUTH)

    fun isMaintenanceMode(): Boolean =
        remoteConfigManager.getBoolean(Keys.MAINTENANCE_MODE)

    fun maintenanceMessage(): String =
        remoteConfigManager.getString(Keys.MAINTENANCE_MESSAGE)
            .ifBlank { DEFAULT_MAINTENANCE_MESSAGE }

    fun remoteAnalyticsEnabled(): Boolean =
        remoteConfigManager.getBoolean(Keys.ENABLE_ANALYTICS)

    fun remoteCrashlyticsEnabled(): Boolean =
        remoteConfigManager.getBoolean(Keys.ENABLE_CRASHLYTICS)

    fun remotePerformanceMonitoringEnabled(): Boolean =
        remoteConfigManager.getBoolean(Keys.ENABLE_PERFORMANCE_MONITORING)

    fun apiTimeoutSeconds(): Long = remoteConfigManager.getLong(Keys.API_TIMEOUT_SECONDS)

    fun maxItemsPerPage(): Long = remoteConfigManager.getLong(Keys.MAX_ITEMS_PER_PAGE)

    fun areAdsEnabled(): Boolean = remoteConfigManager.getBoolean(Keys.ENABLE_ADS)

    fun isBannerAdsEnabled(): Boolean =
        areAdsEnabled() && remoteConfigManager.getBoolean(Keys.ENABLE_BANNER_ADS)

    fun isInterstitialAdsEnabled(): Boolean =
        areAdsEnabled() && remoteConfigManager.getBoolean(Keys.ENABLE_INTERSTITIAL_ADS)

    fun interstitialCooldownSeconds(): Long =
        remoteConfigManager.getLong(Keys.INTERSTITIAL_COOLDOWN_SECONDS)
            .coerceAtLeast(60L)

    companion object {
        const val DEFAULT_MAINTENANCE_MESSAGE =
            "The app is currently under maintenance. Please try again later."
    }
}
