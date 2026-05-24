package dev.sonle.pdfscanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import dev.sonle.pdfscanner.core.ads.AdMobInitializer
import dev.sonle.pdfscanner.core.ads.InterstitialAdController
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import dev.sonle.pdfscanner.presentation.PdfScannerApp
import dev.sonle.pdfscanner.presentation.ads.AdUiStateHolder
import dev.sonle.pdfscanner.presentation.theme.AndroidBaseArchitechtureTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Main Activity with Navigation Compose and Koin dependency injection.
 */
class MainActivity : AppCompatActivity() {

    private val adMobInitializer: AdMobInitializer by inject()
    private val adUiStateHolder: AdUiStateHolder by inject()
    private val interstitialAdController: InterstitialAdController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Timber.d("MainActivity created in ${EnvironmentConfig.environment} environment")

        lifecycleScope.launch {
            (application as MyApplication).awaitFirebaseManagersReady()
            adUiStateHolder.refresh()
            Timber.d("Ads enabled after Remote Config: ${adUiStateHolder.adsEnabled}")
            adMobInitializer.initialize(this@MainActivity, adUiStateHolder.adsEnabled)
            if (adUiStateHolder.adsEnabled) {
                interstitialAdController.preload(
                    adUnitId = adUiStateHolder.interstitialUnitId,
                    adsEnabled = true
                )
            }
        }

        setContent {
            PdfScannerApp()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    AndroidBaseArchitechtureTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Android Base Architecture",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
