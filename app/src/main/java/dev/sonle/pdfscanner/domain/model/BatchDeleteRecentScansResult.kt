package dev.sonle.pdfscanner.domain.model

/**
 * Aggregated outcome of deleting multiple recent scans.
 */
data class BatchDeleteRecentScansResult(
    val deletedCount: Int = 0,
    val notFoundCount: Int = 0,
    val partialFailureCount: Int = 0
) {
    val hasAnySuccess: Boolean get() = deletedCount > 0
    val hasAnyFailure: Boolean get() = notFoundCount > 0 || partialFailureCount > 0
}
