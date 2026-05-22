package dev.sonle.pdfscanner.presentation.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.sonle.pdfscanner.core.ads.AdConfig
import dev.sonle.pdfscanner.core.config.FeatureFlags

/**
 * Shared UI state for ad visibility (Remote Config + selection mode).
 * [adsEnabled] is refreshed after Remote Config fetch — do not cache only at construction.
 */
class AdUiStateHolder(
    private val featureFlags: FeatureFlags
) {
    val bannerUnitId: String = AdConfig.bannerUnitId
    val interstitialUnitId: String = AdConfig.interstitialUnitId

    private var _adsEnabled by mutableStateOf(computeAdsEnabled())
    val adsEnabled: Boolean
        get() = _adsEnabled

    var hideBannerForSelection by mutableStateOf(false)
        private set

    fun refresh() {
        _adsEnabled = computeAdsEnabled()
    }

    fun setSelectionMode(active: Boolean) {
        hideBannerForSelection = active
    }

    private fun computeAdsEnabled(): Boolean =
        AdConfig.isAdsEnabled(featureFlags.isBannerAdsEnabled())
}
