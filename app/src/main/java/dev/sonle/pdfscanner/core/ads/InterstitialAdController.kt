package dev.sonle.pdfscanner.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dev.sonle.pdfscanner.core.config.FeatureFlags
import timber.log.Timber

/**
 * Loads and shows interstitials with cooldown — only at natural transition points.
 */
class InterstitialAdController(
    private val context: Context,
    private val featureFlags: FeatureFlags
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastShownAtMs: Long = 0L

    fun preload(adUnitId: String, adsEnabled: Boolean) {
        if (!adsEnabled || !featureFlags.isInterstitialAdsEnabled()) return
        if (interstitialAd != null || isLoading) return

        isLoading = true
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Timber.d("Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    Timber.w("Interstitial load failed: ${error.message}")
                }
            }
        )
    }

    fun showIfEligible(
        activity: Activity,
        adUnitId: String,
        adsEnabled: Boolean,
        onFinished: () -> Unit
    ) {
        if (!adsEnabled ||
            !featureFlags.isInterstitialAdsEnabled() ||
            !AdPlacementPolicy.allowsPostScanInterstitial()
        ) {
            onFinished()
            return
        }

        val cooldownMs = featureFlags.interstitialCooldownSeconds() * 1000L
        val now = System.currentTimeMillis()
        if (now - lastShownAtMs < cooldownMs) {
            onFinished()
            preload(adUnitId, adsEnabled)
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            onFinished()
            preload(adUnitId, adsEnabled)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownAtMs = System.currentTimeMillis()
                preload(adUnitId, adsEnabled)
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onFinished()
                preload(adUnitId, adsEnabled)
            }
        }
        ad.show(activity)
    }
}
