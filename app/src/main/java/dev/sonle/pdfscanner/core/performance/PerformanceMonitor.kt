package dev.sonle.pdfscanner.core.performance

import com.google.firebase.perf.FirebasePerformance
import dev.sonle.pdfscanner.core.config.EnvironmentConfig
import timber.log.Timber

/**
 * Performance monitoring manager for custom traces (user actions, network, screens).
 */
class PerformanceMonitor(
    private val firebasePerformance: FirebasePerformance
) {

    fun startUserActionTrace(actionName: String): PerformanceTraceHandle? {
        if (!EnvironmentConfig.performanceMonitoringEnabled) {
            Timber.d("Performance monitoring disabled — user_action_$actionName")
            return null
        }
        return startTrace("user_action_$actionName")
    }

    fun startNetworkTrace(url: String): PerformanceTraceHandle? {
        if (!EnvironmentConfig.performanceMonitoringEnabled) {
            Timber.d("Performance monitoring disabled — network_request")
            return null
        }
        return try {
            val trace = firebasePerformance.newTrace("network_request")
            trace.putAttribute("url", sanitizeUrl(url))
            trace.start()
            PerformanceTraceHandle(trace)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start network trace")
            null
        }
    }

    fun startScreenTrace(screenName: String): PerformanceTraceHandle? {
        if (!EnvironmentConfig.performanceMonitoringEnabled) {
            Timber.d("Performance monitoring disabled — screen_$screenName")
            return null
        }
        return startTrace("screen_$screenName") {
            putAttribute("screen_name", screenName)
        }
    }

    fun stopTrace(handle: PerformanceTraceHandle?) {
        handle?.stop()
    }

    fun addMetric(handle: PerformanceTraceHandle?, metricName: String, value: Long) {
        // Metrics require holding the Trace reference; screen/user traces use attributes only.
        Timber.d("Performance metric (trace stopped separately): $metricName = $value")
    }

    private inline fun startTrace(
        traceName: String,
        configure: com.google.firebase.perf.metrics.Trace.() -> Unit = {}
    ): PerformanceTraceHandle? {
        return try {
            val trace = firebasePerformance.newTrace(traceName)
            trace.configure()
            trace.start()
            PerformanceTraceHandle(trace)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start performance trace: $traceName")
            null
        }
    }

    private fun sanitizeUrl(url: String): String =
        url.take(MAX_URL_ATTRIBUTE_LENGTH)

    companion object {
        private const val MAX_URL_ATTRIBUTE_LENGTH = 100
    }
}
