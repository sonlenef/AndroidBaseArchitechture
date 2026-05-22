package dev.sonle.pdfscanner.core.performance

import com.google.firebase.perf.metrics.Trace

/**
 * Wrapper around a Firebase Performance [Trace] for safe start/stop from callers.
 */
class PerformanceTraceHandle internal constructor(
    private val trace: Trace?
) {
    fun stop() {
        trace?.stop()
    }
}
