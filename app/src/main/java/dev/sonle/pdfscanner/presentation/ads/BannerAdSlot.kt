package dev.sonle.pdfscanner.presentation.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import dev.sonle.pdfscanner.core.ads.AdMobInitializer
import dev.sonle.pdfscanner.core.ads.AdPlacementPolicy
import dev.sonle.pdfscanner.core.ads.AdRequestFactory
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Adaptive banner slot with lifecycle-aware pause/resume and destroy.
 * Hidden in preview/selection mode; never placed on scanner or PDF viewer screens.
 */
@Composable
fun BannerAdSlot(
    screen: AdPlacementPolicy.Screen,
    modifier: Modifier = Modifier,
    adUiStateHolder: AdUiStateHolder = koinInject(),
    adMobInitializer: AdMobInitializer = koinInject()
) {
    if (LocalInspectionMode.current) return
    if (!AdPlacementPolicy.allowsBanner(screen)) return

    val adsEnabled = adUiStateHolder.adsEnabled
    val hideForSelection = adUiStateHolder.hideBannerForSelection
    if (!adsEnabled || hideForSelection) return

    val sdkReady by adMobInitializer.sdkReady.collectAsStateWithLifecycle(initialValue = false)
    if (!sdkReady) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val adUnitId = adUiStateHolder.bannerUnitId
    var adView by remember(adUnitId) { mutableStateOf<AdView?>(null) }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView?.pause()
                Lifecycle.Event.ON_RESUME -> adView?.resume()
                Lifecycle.Event.ON_DESTROY -> adView?.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView?.destroy()
            adView = null
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(AdPlacementPolicy.BANNER_SLOT_HEIGHT_DP.dp),
        factory = { ctx ->
            val widthPx = configuration.screenWidthDp
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, widthPx)
            AdView(ctx).apply {
                setAdSize(adSize)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Timber.d("Banner ad loaded ($screen)")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Timber.w("Banner ad failed ($screen): ${error.message} [${error.code}]")
                    }
                }
                adView = this
                loadAd(AdRequestFactory.create())
            }
        },
        update = { view ->
            adView = view
        },
        onRelease = { view ->
            view.destroy()
            if (adView === view) adView = null
        }
    )
}
