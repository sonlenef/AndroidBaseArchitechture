package dev.sonle.pdfscanner.core.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import dev.sonle.pdfscanner.core.performance.PerformanceMonitor
import dev.sonle.pdfscanner.core.performance.PerformanceTraceHandle
import org.koin.compose.koinInject

/**
 * Logs a Firebase Analytics screen view and starts/stops a performance trace for the screen.
 */
@Composable
fun TrackFirebaseScreen(
    screenName: String,
    screenClass: String? = null,
    analyticsManager: AnalyticsManager = koinInject(),
    performanceMonitor: PerformanceMonitor = koinInject()
) {
    DisposableEffect(screenName, screenClass) {
        analyticsManager.logScreenView(screenName, screenClass)
        val trace: PerformanceTraceHandle? =
            performanceMonitor.startScreenTrace(screenName)
        onDispose {
            performanceMonitor.stopTrace(trace)
        }
    }
}
