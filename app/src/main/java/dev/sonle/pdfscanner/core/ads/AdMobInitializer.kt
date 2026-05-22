package dev.sonle.pdfscanner.core.ads

import android.app.Application
import android.app.Activity
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * Initializes the Mobile Ads SDK and requests UMP consent when ads are enabled.
 */
class AdMobInitializer(
    private val application: Application,
    private val adConsentManager: AdConsentManager,
    private val ioDispatcher: CoroutineDispatcher
) {

    private val _sdkReady = MutableStateFlow(false)
    val sdkReady: StateFlow<Boolean> = _sdkReady.asStateFlow()

    suspend fun initialize(activity: Activity?, adsEnabled: Boolean) {
        if (!adsEnabled) {
            Timber.d("AdMob skipped — ads disabled")
            _sdkReady.value = false
            return
        }

        withContext(ioDispatcher) {
            awaitMobileAdsInitialized()
        }

        activity?.let { adConsentManager.gatherConsent(it) }
    }

    private suspend fun awaitMobileAdsInitialized() {
        if (_sdkReady.value) return

        suspendCancellableCoroutine { cont ->
            MobileAds.initialize(application) {
                _sdkReady.value = true
                Timber.d("AdMob SDK initialized")
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }
}
