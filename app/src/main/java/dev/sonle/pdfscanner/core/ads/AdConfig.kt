package dev.sonle.pdfscanner.core.ads

import dev.sonle.pdfscanner.BuildConfig
import dev.sonle.pdfscanner.core.config.EnvironmentConfig

/**
 * Build-time and runtime ad configuration.
 */
object AdConfig {

    val adsEnabledByBuild: Boolean = BuildConfig.ENABLE_ADS

    val bannerUnitId: String = BuildConfig.ADMOB_BANNER_UNIT_ID

    val interstitialUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID

    val useTestAdUnits: Boolean = BuildConfig.USE_TEST_AD_UNITS

    fun isAdsEnabled(remoteAdsEnabled: Boolean): Boolean =
        adsEnabledByBuild && remoteAdsEnabled && EnvironmentConfig.firebaseConfig.remoteConfigEnabled

    /** Default interstitial cooldown (seconds) — overridden by Remote Config. */
    const val DEFAULT_INTERSTITIAL_COOLDOWN_SECONDS = 300L
}
